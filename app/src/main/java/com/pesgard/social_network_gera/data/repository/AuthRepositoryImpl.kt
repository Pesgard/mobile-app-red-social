package com.pesgard.social_network_gera.data.repository

import com.pesgard.social_network_gera.data.local.database.dao.UserDao
import com.pesgard.social_network_gera.data.local.database.entity.toDomain
import com.pesgard.social_network_gera.data.local.database.entity.toEntity
import com.pesgard.social_network_gera.data.local.datastore.SessionManager
import com.pesgard.social_network_gera.data.remote.api.ApiService
import com.pesgard.social_network_gera.data.remote.dto.LoginRequest
import com.pesgard.social_network_gera.data.remote.dto.RegisterRequest
import com.pesgard.social_network_gera.data.remote.dto.UpdateProfileRequest
import com.pesgard.social_network_gera.data.remote.dto.ChangePasswordRequest
import com.pesgard.social_network_gera.data.remote.dto.toDomain
import com.pesgard.social_network_gera.domain.model.User
import com.pesgard.social_network_gera.domain.repository.AuthRepository
import com.pesgard.social_network_gera.util.Constants
import com.pesgard.social_network_gera.util.Constants.ErrorMessages
import com.pesgard.social_network_gera.util.Resource
import com.pesgard.social_network_gera.util.toTimestamp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Implementación del repositorio de autenticación
 * Maneja la lógica offline-first: guarda datos localmente y sincroniza con el servidor
 */
class AuthRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val userDao: UserDao,
    private val sessionManager: SessionManager
) : AuthRepository {

    override suspend fun login(email: String, password: String): Resource<User> {
        return try {
            // 1. Intentar login en API
            val response = apiService.login(LoginRequest(email, password))
            
            if (response.isSuccessful && response.body() != null) {
                val loginResponse = response.body()!!
                val user = loginResponse.user.toDomain()
                
                // 2. Guardar usuario en DB local
                userDao.insertUser(user.toEntity())
                
                // 3. Guardar token y datos de sesión
                sessionManager.saveToken(loginResponse.token)
                sessionManager.saveUserId(user.id)
                sessionManager.saveUserEmail(user.email)
                
                Resource.Success(user)
            } else {
                val errorMessage = when (response.code()) {
                    Constants.HttpStatus.UNAUTHORIZED -> "Email o contraseña incorrectos"
                    Constants.HttpStatus.BAD_REQUEST -> "Datos inválidos"
                    else -> ErrorMessages.NETWORK_ERROR
                }
                Resource.Error(errorMessage)
            }
        } catch (e: Exception) {
            Resource.Error(ErrorMessages.NETWORK_ERROR, e)
        }
    }

    override suspend fun register(user: User, password: String): Resource<User> {
        return try {
            // 1. Intentar registro en API
            val request = RegisterRequest(
                firstName = user.firstName,
                lastName = user.lastName,
                email = user.email,
                password = password,
                phone = user.phone,
                address = user.address,
                alias = user.alias,
                avatarUrl = user.avatarUrl
            )
            
            val response = apiService.register(request)
            
            if (response.isSuccessful && response.body() != null) {
                val registerResponse = response.body()!!
                val registeredUser = User(
                    id = registerResponse.id,
                    email = registerResponse.email,
                    firstName = registerResponse.firstName,
                    lastName = registerResponse.lastName,
                    alias = registerResponse.alias,
                    avatarUrl = registerResponse.avatarUrl,
                    createdAt = registerResponse.createdAt?.toTimestamp() ?: System.currentTimeMillis()
                )
                
                // 2. Guardar usuario en DB local
                userDao.insertUser(registeredUser.toEntity())
                
                // 3. Guardar token y datos de sesión
                sessionManager.saveToken(registerResponse.token)
                sessionManager.saveUserId(registeredUser.id)
                sessionManager.saveUserEmail(registeredUser.email)
                
                Resource.Success(registeredUser)
            } else {
                val errorMessage = when (response.code()) {
                    Constants.HttpStatus.BAD_REQUEST -> "Datos inválidos o email ya registrado"
                    Constants.HttpStatus.CONFLICT -> "El email o alias ya está en uso"
                    else -> ErrorMessages.NETWORK_ERROR
                }
                Resource.Error(errorMessage)
            }
        } catch (e: Exception) {
            Resource.Error(ErrorMessages.NETWORK_ERROR, e)
        }
    }

    override fun getCurrentUser(): Flow<User?> {
        return sessionManager.userId.flatMapLatest { userId ->
            if (userId != null) {
                userDao.getUserById(userId).map { it?.toDomain() }
            } else {
                kotlinx.coroutines.flow.flowOf(null)
            }
        }
    }

    override suspend fun updateProfile(user: User): Resource<User> {
        return try {
            // 1. Intentar actualizar en API
            val request = UpdateProfileRequest(
                alias = user.alias,
                avatarUrl = user.avatarUrl,
                phone = user.phone,
                address = user.address
            )
            
            val response = apiService.updateProfile(request)
            
            if (response.isSuccessful) {
                // 2. Actualizar en DB local
                val updatedUser = user.copy(updatedAt = System.currentTimeMillis())
                userDao.updateUser(updatedUser.toEntity())
                
                Resource.Success(updatedUser)
            } else {
                val errorMessage = when (response.code()) {
                    Constants.HttpStatus.UNAUTHORIZED -> ErrorMessages.UNAUTHORIZED
                    Constants.HttpStatus.BAD_REQUEST -> ErrorMessages.VALIDATION_ERROR
                    else -> ErrorMessages.NETWORK_ERROR
                }
                Resource.Error(errorMessage)
            }
        } catch (e: Exception) {
            // En caso de error, guardar localmente de todas formas (offline-first)
            val updatedUser = user.copy(updatedAt = System.currentTimeMillis())
            userDao.updateUser(updatedUser.toEntity())
            Resource.Success(updatedUser)
        }
    }

    override suspend fun changePassword(
        currentPassword: String,
        newPassword: String
    ): Resource<Unit> {
        return try {
            val request = ChangePasswordRequest(
                currentPassword = currentPassword,
                newPassword = newPassword
            )
            
            val response = apiService.changePassword(request)
            
            if (response.isSuccessful) {
                Resource.Success(Unit)
            } else {
                val errorMessage = when (response.code()) {
                    Constants.HttpStatus.UNAUTHORIZED -> "Contraseña actual incorrecta"
                    Constants.HttpStatus.BAD_REQUEST -> ErrorMessages.VALIDATION_ERROR
                    else -> ErrorMessages.NETWORK_ERROR
                }
                Resource.Error(errorMessage)
            }
        } catch (e: Exception) {
            Resource.Error(ErrorMessages.NETWORK_ERROR, e)
        }
    }

    override suspend fun logout() {
        // Limpiar sesión
        sessionManager.clearSession()
        
        // Opcional: eliminar usuario de la DB local (o mantenerlo para acceso offline)
        // userDao.deleteAllUsers()
    }

    override fun isLoggedIn(): Flow<Boolean> {
        return sessionManager.isLoggedIn
    }

    override suspend fun refreshUserProfile(): Resource<User> {
        return try {
            val response = apiService.getCurrentUser()
            
            if (response.isSuccessful && response.body() != null) {
                val userDto = response.body()!!
                val user = userDto.toDomain()
                
                // Actualizar usuario en DB local
                userDao.insertUser(user.toEntity())
                
                android.util.Log.d("AuthRepository", "Perfil refrescado desde servidor: ${user.alias}")
                Resource.Success(user)
            } else {
                val errorMessage = when (response.code()) {
                    Constants.HttpStatus.UNAUTHORIZED -> ErrorMessages.UNAUTHORIZED
                    Constants.HttpStatus.NOT_FOUND -> "Usuario no encontrado"
                    else -> ErrorMessages.NETWORK_ERROR
                }
                Resource.Error(errorMessage)
            }
        } catch (e: Exception) {
            android.util.Log.e("AuthRepository", "Error refrescando perfil: ${e.message}", e)
            Resource.Error(ErrorMessages.NETWORK_ERROR, e)
        }
    }
}
