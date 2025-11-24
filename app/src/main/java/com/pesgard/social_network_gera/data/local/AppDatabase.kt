package com.pesgard.social_network_gera.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.pesgard.social_network_gera.data.local.database.Converters
import com.pesgard.social_network_gera.data.local.database.dao.CommentDao
import com.pesgard.social_network_gera.data.local.database.dao.DraftPostDao
import com.pesgard.social_network_gera.data.local.database.dao.FavoriteDao
import com.pesgard.social_network_gera.data.local.database.dao.PostDao
import com.pesgard.social_network_gera.data.local.database.dao.UserDao
import com.pesgard.social_network_gera.data.local.database.entity.CommentEntity
import com.pesgard.social_network_gera.data.local.database.entity.DraftPostEntity
import com.pesgard.social_network_gera.data.local.database.entity.FavoriteEntity
import com.pesgard.social_network_gera.data.local.database.entity.PostEntity
import com.pesgard.social_network_gera.data.local.database.entity.UserEntity
import com.pesgard.social_network_gera.util.Constants

/**
 * Base de datos Room principal de la aplicación
 * 
 * Incluye todas las entidades y DAOs necesarios para el funcionamiento offline-first
 */
@Database(
    entities = [
        UserEntity::class,
        PostEntity::class,
        CommentEntity::class,
        FavoriteEntity::class,
        DraftPostEntity::class
    ],
    version = Constants.DATABASE_VERSION,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    
    /**
     * DAO para operaciones de usuario
     */
    abstract fun userDao(): UserDao
    
    /**
     * DAO para operaciones de publicaciones
     */
    abstract fun postDao(): PostDao
    
    /**
     * DAO para operaciones de comentarios
     */
    abstract fun commentDao(): CommentDao
    
    /**
     * DAO para operaciones de favoritos
     */
    abstract fun favoriteDao(): FavoriteDao
    
    /**
     * DAO para operaciones de borradores
     */
    abstract fun draftPostDao(): DraftPostDao
}
