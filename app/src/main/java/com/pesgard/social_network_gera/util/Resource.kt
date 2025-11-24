package com.pesgard.social_network_gera.util

/**
 * Sealed class para manejar estados de recursos en la aplicación.
 * Utilizado para representar estados de carga, éxito y error de manera type-safe.
 *
 * @param T Tipo de dato que contiene el recurso en caso de éxito
 */
sealed class Resource<out T> {
    /**
     * Estado de carga - indica que una operación está en progreso
     */
    object Loading : Resource<Nothing>()

    /**
     * Estado de éxito - contiene los datos resultantes
     * @param data Los datos del recurso
     */
    data class Success<T>(val data: T) : Resource<T>()

    /**
     * Estado de error - contiene información sobre el error ocurrido
     * @param message Mensaje de error amigable para el usuario
     * @param exception Excepción original (opcional) para logging/debugging
     */
    data class Error(val message: String, val exception: Exception? = null) : Resource<Nothing>()

    /**
     * Verifica si el recurso está en estado de carga
     */
    val isLoading: Boolean
        get() = this is Loading

    /**
     * Verifica si el recurso es exitoso
     */
    val isSuccess: Boolean
        get() = this is Success

    /**
     * Verifica si el recurso es un error
     */
    val isError: Boolean
        get() = this is Error

    /**
     * Obtiene los datos si el recurso es exitoso, null en caso contrario
     */
    fun getDataOrNull(): T? = when (this) {
        is Success -> data
        else -> null
    }

    /**
     * Obtiene el mensaje de error si existe, null en caso contrario
     */
    fun getErrorMessageOrNull(): String? = when (this) {
        is Error -> message
        else -> null
    }
}
