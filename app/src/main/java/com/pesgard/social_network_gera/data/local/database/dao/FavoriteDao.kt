package com.pesgard.social_network_gera.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pesgard.social_network_gera.data.local.database.entity.FavoriteEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object para operaciones de favoritos en Room Database
 */
@Dao
interface FavoriteDao {
    /**
     * Obtiene todos los favoritos de un usuario específico
     * @param userId ID del usuario
     * @return Flow que emite la lista de favoritos del usuario
     */
    @Query("SELECT * FROM ${com.pesgard.social_network_gera.util.Constants.Tables.FAVORITES} WHERE userId = :userId ORDER BY createdAt DESC")
    fun getFavoritesByUserId(userId: String): Flow<List<FavoriteEntity>>

    /**
     * Verifica si un post está marcado como favorito por un usuario
     * @param userId ID del usuario
     * @param postId ID del post
     * @return Flow que emite true si es favorito, false en caso contrario
     */
    @Query("SELECT EXISTS(SELECT 1 FROM ${com.pesgard.social_network_gera.util.Constants.Tables.FAVORITES} WHERE userId = :userId AND postId = :postId)")
    fun isFavorite(userId: String, postId: Long): Flow<Boolean>

    /**
     * Obtiene un favorito específico
     * @param userId ID del usuario
     * @param postId ID del post
     * @return Flow que emite el favorito o null si no existe
     */
    @Query("SELECT * FROM ${com.pesgard.social_network_gera.util.Constants.Tables.FAVORITES} WHERE userId = :userId AND postId = :postId LIMIT 1")
    fun getFavorite(userId: String, postId: Long): Flow<FavoriteEntity?>

    /**
     * Inserta un favorito en la base de datos
     * @param favorite Favorito a insertar
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteEntity)

    /**
     * Elimina un favorito (quita de favoritos)
     * @param userId ID del usuario
     * @param postId ID del post
     */
    @Query("DELETE FROM ${com.pesgard.social_network_gera.util.Constants.Tables.FAVORITES} WHERE userId = :userId AND postId = :postId")
    suspend fun deleteFavorite(userId: String, postId: Long)

    /**
     * Elimina todos los favoritos de un usuario
     * @param userId ID del usuario
     */
    @Query("DELETE FROM ${com.pesgard.social_network_gera.util.Constants.Tables.FAVORITES} WHERE userId = :userId")
    suspend fun deleteAllFavoritesByUserId(userId: String)
}



