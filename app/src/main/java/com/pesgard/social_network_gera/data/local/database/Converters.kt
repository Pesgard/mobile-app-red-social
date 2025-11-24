package com.pesgard.social_network_gera.data.local.database

import androidx.room.TypeConverter
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types

/**
 * TypeConverters para Room Database
 * Convierte tipos complejos a tipos primitivos que Room puede almacenar
 */
class Converters {
    private val moshi = Moshi.Builder().build()
    
    private val listStringAdapter: JsonAdapter<List<String>> = moshi.adapter(
        Types.newParameterizedType(List::class.java, String::class.java)
    )

    /**
     * Convierte List<String> a JSON string para almacenar en Room
     */
    @TypeConverter
    fun fromStringList(value: String?): List<String> {
        if (value.isNullOrBlank()) return emptyList()
        return try {
            listStringAdapter.fromJson(value) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Convierte JSON string a List<String> para leer de Room
     */
    @TypeConverter
    fun toStringList(value: List<String>?): String {
        if (value.isNullOrEmpty()) return "[]"
        return try {
            listStringAdapter.toJson(value) ?: "[]"
        } catch (e: Exception) {
            "[]"
        }
    }
}



