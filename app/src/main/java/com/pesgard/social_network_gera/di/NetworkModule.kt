package com.pesgard.social_network_gera.di

import com.pesgard.social_network_gera.data.local.datastore.SessionManager
import com.pesgard.social_network_gera.data.remote.api.ApiService
import com.pesgard.social_network_gera.util.Constants
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import okhttp3.logging.HttpLoggingInterceptor

/**
 * Módulo de Hilt para proporcionar instancias de Retrofit y ApiService
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    /**
     * Proporciona una instancia de Moshi para serialización JSON
     */
    @Provides
    @Singleton
    fun provideMoshi(): Moshi {
        return Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
    }

    /**
     * Interceptor para agregar el token JWT a las peticiones autenticadas
     */
    @Provides
    @Singleton
    fun provideAuthInterceptor(sessionManager: SessionManager): Interceptor {
        return Interceptor { chain ->
            val originalRequest = chain.request()
            
            // Obtener token de forma bloqueante (solo para interceptores)
            val token = sessionManager.getTokenBlocking()
            
            val newRequest = if (token != null) {
                originalRequest.newBuilder()
                    .header("Authorization", "Bearer $token")
                    .build()
            } else {
                originalRequest
            }
            
            chain.proceed(newRequest)
        }
    }

    /**
     * Interceptor para logging de peticiones HTTP (solo en modo debug)
     */
    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            // En desarrollo, siempre mostrar logs. En producción se puede cambiar
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    /**
     * Proporciona una instancia de OkHttpClient configurada
     */
    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: Interceptor,
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(Constants.HTTP_CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(Constants.HTTP_READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(Constants.HTTP_WRITE_TIMEOUT, TimeUnit.SECONDS)
            .build()
    }

    /**
     * Proporciona una instancia de Retrofit configurada
     */
    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        moshi: Moshi
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    /**
     * Proporciona una instancia de ApiService
     */
    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }
}
