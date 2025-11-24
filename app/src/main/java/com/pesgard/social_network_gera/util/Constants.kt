package com.pesgard.social_network_gera.util

/**
 * Constantes globales de la aplicación
 */
object Constants {
    // ============================================================
    // API CONFIGURATION
    // ============================================================
    
    /**
     * Base URL de la API
     * Puede cambiar según el entorno: /dev, /staging, /prod
     */
    const val BASE_URL = "https://social.esgardpeinado.dev/api/"
    
    /**
     * Timeout para conexiones HTTP (en segundos)
     */
    const val HTTP_CONNECT_TIMEOUT = 30L
    const val HTTP_READ_TIMEOUT = 30L
    const val HTTP_WRITE_TIMEOUT = 30L

    // ============================================================
    // DATASTORE KEYS
    // ============================================================
    
    /**
     * Nombre del DataStore de preferencias
     */
    const val DATASTORE_NAME = "connecta_preferences"
    
    /**
     * Keys para almacenar datos en DataStore
     */
    object DataStoreKeys {
        const val AUTH_TOKEN = "auth_token"
        const val USER_ID = "user_id"
        const val USER_EMAIL = "user_email"
        const val IS_LOGGED_IN = "is_logged_in"
        const val LAST_SYNC_TIMESTAMP = "last_sync_timestamp"
    }

    // ============================================================
    // DATABASE
    // ============================================================
    
    /**
     * Nombre de la base de datos Room
     */
    const val DATABASE_NAME = "connecta_database"
    
    /**
     * Versión de la base de datos
     */
    const val DATABASE_VERSION = 1
    
    /**
     * Nombres de tablas
     */
    object Tables {
        const val USERS = "users"
        const val POSTS = "posts"
        const val COMMENTS = "comments"
        const val FAVORITES = "favorites"
    }

    // ============================================================
    // HTTP STATUS CODES
    // ============================================================
    
    object HttpStatus {
        const val OK = 200
        const val CREATED = 201
        const val NO_CONTENT = 204
        const val BAD_REQUEST = 400
        const val UNAUTHORIZED = 401
        const val FORBIDDEN = 403
        const val NOT_FOUND = 404
        const val CONFLICT = 409
        const val INTERNAL_SERVER_ERROR = 500
        const val SERVICE_UNAVAILABLE = 503
    }

    // ============================================================
    // ERROR MESSAGES
    // ============================================================
    
    object ErrorMessages {
        const val NETWORK_ERROR = "Error de conexión. Verifica tu internet."
        const val SERVER_ERROR = "Error del servidor. Intenta más tarde."
        const val UNAUTHORIZED = "Sesión expirada. Por favor inicia sesión nuevamente."
        const val NOT_FOUND = "Recurso no encontrado."
        const val VALIDATION_ERROR = "Por favor verifica los datos ingresados."
        const val UNKNOWN_ERROR = "Ocurrió un error inesperado."
        const val OFFLINE_ERROR = "Sin conexión a internet. Los cambios se guardarán localmente."
    }

    // ============================================================
    // VALIDATION RULES
    // ============================================================
    
    /**
     * Reglas de validación para contraseñas
     */
    object PasswordRules {
        const val MIN_LENGTH = 10
        const val REQUIRE_UPPERCASE = true
        const val REQUIRE_LOWERCASE = true
        const val REQUIRE_NUMBER = true
        const val REQUIRE_SPECIAL_CHAR = false // Opcional según requerimientos
    }
    
    /**
     * Reglas de validación para emails
     */
    object EmailRules {
        const val MIN_LENGTH = 5
        const val MAX_LENGTH = 255
    }

    // ============================================================
    // SYNC CONFIGURATION
    // ============================================================
    
    /**
     * Intervalo de sincronización en minutos
     */
    const val SYNC_INTERVAL_MINUTES = 15L
    
    /**
     * Nombre del Worker de sincronización
     */
    const val SYNC_WORKER_NAME = "sync_worker"
    
    /**
     * Tag para identificar trabajos de sincronización
     */
    const val SYNC_WORK_TAG = "sync_work"

    // ============================================================
    // PAGINATION
    // ============================================================
    
    /**
     * Tamaño de página por defecto para paginación
     */
    const val DEFAULT_PAGE_SIZE = 20

    // ============================================================
    // DATE FORMATS
    // ============================================================
    
    /**
     * Formato de fecha ISO 8601 para comunicación con API
     */
    const val DATE_FORMAT_ISO = "yyyy-MM-dd'T'HH:mm:ss'Z'"
    
    /**
     * Formato de fecha para mostrar en la UI
     */
    const val DATE_FORMAT_DISPLAY = "dd MMM yyyy, HH:mm"
    
    /**
     * Formato de fecha corto para mostrar en la UI
     */
    const val DATE_FORMAT_SHORT = "dd MMM yyyy"

    // ============================================================
    // IMAGE CONFIGURATION
    // ============================================================
    
    /**
     * Tamaño máximo de imagen en MB
     */
    const val MAX_IMAGE_SIZE_MB = 5
    
    /**
     * Número máximo de imágenes por post
     */
    const val MAX_IMAGES_PER_POST = 5
    
    /**
     * Calidad de compresión de imágenes (0-100)
     */
    const val IMAGE_COMPRESSION_QUALITY = 85

    // ============================================================
    // NETWORK MONITOR
    // ============================================================
    
    /**
     * Intervalo de verificación de conexión en milisegundos
     */
    const val NETWORK_CHECK_INTERVAL_MS = 5000L
}
