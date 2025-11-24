package com.pesgard.social_network_gera.di

import com.pesgard.social_network_gera.data.local.datastore.SessionManager
import com.pesgard.social_network_gera.data.repository.AuthRepositoryImpl
import com.pesgard.social_network_gera.data.repository.CommentRepositoryImpl
import com.pesgard.social_network_gera.data.repository.PostRepositoryImpl
import com.pesgard.social_network_gera.domain.repository.AuthRepository
import com.pesgard.social_network_gera.domain.repository.CommentRepository
import com.pesgard.social_network_gera.domain.repository.PostRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Módulo de Hilt para proporcionar instancias de repositorios
 * Usa @Binds para inyectar las implementaciones como interfaces
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    /**
     * Proporciona AuthRepositoryImpl como AuthRepository
     */
    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    /**
     * Proporciona PostRepositoryImpl como PostRepository
     */
    @Binds
    @Singleton
    abstract fun bindPostRepository(
        postRepositoryImpl: PostRepositoryImpl
    ): PostRepository

    /**
     * Proporciona CommentRepositoryImpl como CommentRepository
     */
    @Binds
    @Singleton
    abstract fun bindCommentRepository(
        commentRepositoryImpl: CommentRepositoryImpl
    ): CommentRepository
}

/**
 * SessionManager ya está inyectado con @Inject constructor,
 * no necesita binding adicional en este módulo
 */
