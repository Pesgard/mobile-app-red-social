package com.pesgard.social_network_gera.data.local.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.pesgard.social_network_gera.data.local.database.entity.DraftPostEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object para operaciones de borradores en Room Database
 */
@Dao
interface DraftPostDao {
    /**
     * Obtiene todos los borradores de un usuario específico
     * @param userId ID del usuario
     * @return Flow que emite la lista de borradores del usuario
     */
    @Query("SELECT * FROM ${com.pesgard.social_network_gera.util.Constants.Tables.DRAFTS} WHERE userId = :userId ORDER BY updatedAt DESC")
    fun getDraftsByUserId(userId: String): Flow<List<DraftPostEntity>>

    /**
     * Obtiene un borrador por su ID
     * @param id ID del borrador
     * @return Flow que emite el borrador o null si no existe
     */
    @Query("SELECT * FROM ${com.pesgard.social_network_gera.util.Constants.Tables.DRAFTS} WHERE id = :id")
    suspend fun getDraftById(id: Long): DraftPostEntity?

    /**
     * Inserta un borrador en la base de datos
     * @param draft Borrador a insertar
     * @return ID del borrador insertado
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDraft(draft: DraftPostEntity): Long

    /**
     * Actualiza un borrador existente
     * @param draft Borrador a actualizar
     */
    @Update
    suspend fun updateDraft(draft: DraftPostEntity)

    /**
     * Elimina un borrador por su ID
     * @param id ID del borrador a eliminar
     */
    @Delete
    suspend fun deleteDraft(draft: DraftPostEntity)

    /**
     * Elimina un borrador por su ID
     * @param id ID del borrador a eliminar
     */
    @Query("DELETE FROM ${com.pesgard.social_network_gera.util.Constants.Tables.DRAFTS} WHERE id = :id")
    suspend fun deleteDraftById(id: Long)

    /**
     * Elimina todos los borradores de un usuario
     * @param userId ID del usuario
     */
    @Query("DELETE FROM ${com.pesgard.social_network_gera.util.Constants.Tables.DRAFTS} WHERE userId = :userId")
    suspend fun deleteAllDraftsByUserId(userId: String)
}

