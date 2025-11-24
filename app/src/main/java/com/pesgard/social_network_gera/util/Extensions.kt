package com.pesgard.social_network_gera.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

// ============================================================
// STRING EXTENSIONS
// ============================================================

/**
 * Valida si un String es un email válido
 * @return true si el email es válido, false en caso contrario
 */
fun String.isValidEmail(): Boolean {
    if (this.isBlank() || this.length < Constants.EmailRules.MIN_LENGTH) {
        return false
    }
    
    val emailRegex = Regex(
        "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}\$",
        RegexOption.IGNORE_CASE
    )
    return emailRegex.matches(this)
}

/**
 * Valida si un String cumple con las reglas de contraseña
 * @return true si la contraseña es válida, false en caso contrario
 */
fun String.isValidPassword(): Boolean {
    if (this.length < Constants.PasswordRules.MIN_LENGTH) {
        return false
    }
    
    if (Constants.PasswordRules.REQUIRE_UPPERCASE && !this.any { it.isUpperCase() }) {
        return false
    }
    
    if (Constants.PasswordRules.REQUIRE_LOWERCASE && !this.any { it.isLowerCase() }) {
        return false
    }
    
    if (Constants.PasswordRules.REQUIRE_NUMBER && !this.any { it.isDigit() }) {
        return false
    }
    
    if (Constants.PasswordRules.REQUIRE_SPECIAL_CHAR) {
        val specialChars = "!@#$%^&*()_+-=[]{}|;:,.<>?"
        if (!this.any { it in specialChars }) {
            return false
        }
    }
    
    return true
}

/**
 * Obtiene un mensaje de error detallado para una contraseña inválida
 * @return Mensaje descriptivo de qué falta en la contraseña
 */
fun String.getPasswordValidationMessage(): String {
    val errors = mutableListOf<String>()
    
    if (this.length < Constants.PasswordRules.MIN_LENGTH) {
        errors.add("Debe tener al menos ${Constants.PasswordRules.MIN_LENGTH} caracteres")
    }
    
    if (Constants.PasswordRules.REQUIRE_UPPERCASE && !this.any { it.isUpperCase() }) {
        errors.add("Debe contener al menos una mayúscula")
    }
    
    if (Constants.PasswordRules.REQUIRE_LOWERCASE && !this.any { it.isLowerCase() }) {
        errors.add("Debe contener al menos una minúscula")
    }
    
    if (Constants.PasswordRules.REQUIRE_NUMBER && !this.any { it.isDigit() }) {
        errors.add("Debe contener al menos un número")
    }
    
    return if (errors.isEmpty()) {
        "Contraseña válida"
    } else {
        errors.joinToString(", ")
    }
}

/**
 * Capitaliza la primera letra de un String
 */
fun String.capitalizeFirst(): String {
    return if (this.isEmpty()) {
        this
    } else {
        this[0].uppercaseChar() + this.substring(1).lowercase()
    }
}

/**
 * Trunca un String a una longitud máxima y agrega "..." si es necesario
 * @param maxLength Longitud máxima del String
 * @return String truncado con "..." si excede la longitud
 */
fun String.truncate(maxLength: Int): String {
    return if (this.length <= maxLength) {
        this
    } else {
        this.take(maxLength) + "..."
    }
}

// ============================================================
// LONG EXTENSIONS (Timestamps)
// ============================================================

/**
 * Convierte un timestamp (Long) a una fecha formateada para mostrar
 * @param format Formato de fecha (por defecto DATE_FORMAT_DISPLAY)
 * @return String con la fecha formateada
 */
fun Long.toDateString(format: String = Constants.DATE_FORMAT_DISPLAY): String {
    val sdf = SimpleDateFormat(format, Locale.getDefault())
    return sdf.format(Date(this))
}

/**
 * Convierte un timestamp (Long) a una fecha formateada corta
 * @return String con la fecha formateada en formato corto
 */
fun Long.toShortDateString(): String {
    return this.toDateString(Constants.DATE_FORMAT_SHORT)
}

/**
 * Convierte un timestamp (Long) a formato ISO 8601 para API
 * @return String con la fecha en formato ISO 8601
 */
fun Long.toIsoDateString(): String {
    val sdf = SimpleDateFormat(Constants.DATE_FORMAT_ISO, Locale.getDefault())
    sdf.timeZone = TimeZone.getTimeZone("UTC")
    return sdf.format(Date(this))
}

/**
 * Convierte un timestamp a una representación relativa basada en la fecha actual del dispositivo
 * Compara la fecha del backend (parseada desde ISO 8601) con la fecha/hora actual del dispositivo
 * Ejemplos: "hace 5 minutos", "hace 2 horas", "hace 3 meses", "hace 1 año"
 * @return String con tiempo relativo
 */
fun Long.toRelativeTimeString(): String {
    val now = System.currentTimeMillis() // Fecha/hora actual del dispositivo
    val diff = now - this // Diferencia en milisegundos
    
    // Si la diferencia es negativa (fecha futura o error), mostrar "hace unos segundos"
    if (diff < 0) {
        return "hace unos segundos"
    }
    
    return when {
        diff < 60_000 -> "hace unos segundos"
        diff < 3_600_000 -> {
            val minutes = (diff / 60_000).toInt()
            if (minutes == 1) "hace 1 minuto" else "hace $minutes minutos"
        }
        diff < 86_400_000 -> {
            val hours = (diff / 3_600_000).toInt()
            if (hours == 1) "hace 1 hora" else "hace $hours horas"
        }
        diff < 2_592_000_000 -> { // 30 días
            val days = (diff / 86_400_000).toInt()
            if (days == 1) "hace 1 día" else "hace $days días"
        }
        diff < 31_536_000_000 -> { // Aproximadamente 1 año (365 días)
            val months = (diff / 2_592_000_000).toInt() // Aproximadamente 30 días por mes
            if (months == 1) "hace 1 mes" else "hace $months meses"
        }
        else -> {
            val years = (diff / 31_536_000_000).toInt() // Aproximadamente 365 días por año
            if (years == 1) "hace 1 año" else "hace $years años"
        }
    }
}

// ============================================================
// STRING TO TIMESTAMP
// ============================================================

/**
 * Convierte un String en formato ISO 8601 a timestamp (Long)
 * Soporta formatos con y sin milisegundos: "2025-11-24T05:33:19.000Z" o "2025-11-24T05:33:19Z"
 * @return Long timestamp o null si el formato es inválido
 */
fun String.toTimestamp(): Long? {
    return try {
        // Intentar primero con milisegundos (formato del backend)
        val formatWithMillis = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        formatWithMillis.timeZone = TimeZone.getTimeZone("UTC")
        val result = formatWithMillis.parse(this)
        if (result != null) {
            return result.time
        }
        
        // Si falla, intentar sin milisegundos (formato estándar)
        val formatWithoutMillis = SimpleDateFormat(Constants.DATE_FORMAT_ISO, Locale.getDefault())
        formatWithoutMillis.timeZone = TimeZone.getTimeZone("UTC")
        formatWithoutMillis.parse(this)?.time
    } catch (e: Exception) {
        null
    }
}

// ============================================================
// NULL SAFETY EXTENSIONS
// ============================================================

/**
 * Retorna el String si no es null o vacío, o un valor por defecto
 */
fun String?.orEmpty(default: String = ""): String {
    return if (this.isNullOrBlank()) default else this
}

/**
 * Retorna el String si no es null, o un valor por defecto
 */
fun String?.orDefault(default: String): String {
    return this ?: default
}

// ============================================================
// LIST EXTENSIONS
// ============================================================

/**
 * Verifica si una lista no está vacía
 */
fun <T> List<T>?.isNotEmpty(): Boolean {
    return this != null && this.isNotEmpty()
}

/**
 * Retorna la lista si no es null, o una lista vacía
 */
fun <T> List<T>?.orEmpty(): List<T> {
    return this ?: emptyList()
}



