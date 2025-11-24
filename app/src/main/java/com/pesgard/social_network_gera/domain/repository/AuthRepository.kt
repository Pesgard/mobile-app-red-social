package com.pesgard.social_network_gera.domain.repository

import com.pesgard.social_network_gera.domain.model.User
import com.pesgard.social_network_gera.util.Resource
import kotlinx.coroutines.flow.Flow

/**
 * Interfaz del repositorio de autenticación en la capa de dominio
 * Define las operaciones de autenticación sin depender de implementaciones concretas
 */
interface AuthRepository {
    /**
     * Inicia sesión con email y contraseña
     * @param email Email del usuario
     * @param password Contraseña del usuario
     * @return Resource con el usuario autenticado o error
     */
    suspend fun login(email: String, password: String): Resource<User>
    
    /**
     * Registra un nuevo usuario
     * @param user Datos del usuario a registrar
     * @param password Contraseña del usuario
     * @return Resource con el usuario registrado o error
     */
    suspend fun register(user: User, password: String): Resource<User>
    
    /**
     * Obtiene el usuario actual logueado
     * @return Flow que emite el usuario actual o null si no hay sesión
     */
    fun getCurrentUser(): Flow<User?>
    
    /**
     * Actualiza el perfil del usuario actual
     * @param user Datos actualizados del usuario
     * @return Resource con el usuario actualizado o error
     */
    suspend fun updateProfile(user: User): Resource<User>
    
    /**
     * Cambia la contraseña del usuario actual
     * @param currentPassword Contraseña actual
     * @param newPassword Nueva contraseña
     * @return Resource indicando éxito o error
     */
    suspend fun changePassword(currentPassword: String, newPassword: String): Resource<Unit>
    
    /**
     * Cierra la sesión del usuario actual
     */
    suspend fun logout()
    
    /**
     * Verifica si hay un usuario logueado
     * @return Flow que emite true si hay sesión activa, false en caso contrario
     */
    fun isLoggedIn(): Flow<Boolean>
    
    /**
     * Refresca el perfil del usuario actual desde el servidor
     * @return Resource con el usuario actualizado o error
     */
    suspend fun refreshUserProfile(): Resource<User>
}
