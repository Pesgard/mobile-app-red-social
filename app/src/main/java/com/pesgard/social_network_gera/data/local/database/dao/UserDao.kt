package com.pesgard.social_network_gera.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.pesgard.social_network_gera.data.local.database.entity.UserEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object para operaciones de usuario en Room Database
 */
@Dao
interface UserDao {
    /**
     * Obtiene un usuario por su ID
     * @param id ID del usuario
     * @return Flow que emite el usuario o null si no existe
     */
    @Query("SELECT * FROM ${com.pesgard.social_network_gera.util.Constants.Tables.USERS} WHERE id = :id")
    fun getUserById(id: String): Flow<UserEntity?>

    /**
     * Obtiene el usuario actual (el primero en la base de datos)
     * En una app real, esto debería usar un campo isCurrentUser o similar
     * Por ahora, asumimos que solo hay un usuario logueado a la vez
     */
    @Query("SELECT * FROM ${com.pesgard.social_network_gera.util.Constants.Tables.USERS} LIMIT 1")
    fun getCurrentUser(): Flow<UserEntity?>

    /**
     * Obtiene un usuario por email
     * @param email Email del usuario
     * @return Flow que emite el usuario o null si no existe
     */
    @Query("SELECT * FROM ${com.pesgard.social_network_gera.util.Constants.Tables.USERS} WHERE email = :email LIMIT 1")
    fun getUserByEmail(email: String): Flow<UserEntity?>?

    /**
     * Inserta un usuario en la base de datos
     * @param user Usuario a insertar
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    /**
     * Actualiza un usuario existente
     * @param user Usuario a actualizar
     */
    @Update
    suspend fun updateUser(user: UserEntity)

    /**
     * Elimina un usuario por su ID
     * @param id ID del usuario a eliminar
     */
    @Query("DELETE FROM ${com.pesgard.social_network_gera.util.Constants.Tables.USERS} WHERE id = :id")
    suspend fun deleteUser(id: String)

    /**
     * Elimina todos los usuarios (útil para logout)
     */
    @Query("DELETE FROM ${com.pesgard.social_network_gera.util.Constants.Tables.USERS}")
    suspend fun deleteAllUsers()

    /**
     * Obtiene múltiples usuarios por sus IDs
     * @param ids Lista de IDs de usuarios
     * @return Flow que emite la lista de usuarios encontrados
     */
    @Query("SELECT * FROM ${com.pesgard.social_network_gera.util.Constants.Tables.USERS} WHERE id IN (:ids)")
    fun getUsersByIds(ids: List<String>): Flow<List<UserEntity>>
}
