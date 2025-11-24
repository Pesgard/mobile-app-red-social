package com.pesgard.social_network_gera.di

import android.content.Context
import androidx.room.Room
import com.pesgard.social_network_gera.data.local.AppDatabase
import com.pesgard.social_network_gera.data.local.database.dao.CommentDao
import com.pesgard.social_network_gera.data.local.database.dao.DraftPostDao
import com.pesgard.social_network_gera.data.local.database.dao.FavoriteDao
import com.pesgard.social_network_gera.data.local.database.dao.PostDao
import com.pesgard.social_network_gera.data.local.database.dao.UserDao
import com.pesgard.social_network_gera.util.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Módulo de Hilt para proporcionar instancias de la base de datos Room y DAOs
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    /**
     * Proporciona una instancia singleton de AppDatabase
     */
    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            Constants.DATABASE_NAME
        )
            .fallbackToDestructiveMigration() // En desarrollo, elimina y recrea en cambios de schema
            .build()
    }

    /**
     * Proporciona una instancia de UserDao
     */
    @Provides
    fun provideUserDao(database: AppDatabase): UserDao {
        return database.userDao()
    }

    /**
     * Proporciona una instancia de PostDao
     */
    @Provides
    fun providePostDao(database: AppDatabase): PostDao {
        return database.postDao()
    }

    /**
     * Proporciona una instancia de CommentDao
     */
    @Provides
    fun provideCommentDao(database: AppDatabase): CommentDao {
        return database.commentDao()
    }

    /**
     * Proporciona una instancia de FavoriteDao
     */
    @Provides
    fun provideFavoriteDao(database: AppDatabase): FavoriteDao {
        return database.favoriteDao()
    }
    
    /**
     * Proporciona una instancia de DraftPostDao
     */
    @Provides
    fun provideDraftPostDao(database: AppDatabase): DraftPostDao {
        return database.draftPostDao()
    }
}



