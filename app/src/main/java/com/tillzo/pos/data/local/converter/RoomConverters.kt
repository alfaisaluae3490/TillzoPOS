package com.tillzo.pos.data.local.converter

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Room TypeConverters — shared across all entities.
 * Handles types that Room can't store natively.
 */
class RoomConverters {

    private val gson = Gson()

    // List<String> ↔ JSON string
    @TypeConverter
    fun fromStringList(value: List<String>?): String? =
        value?.let { gson.toJson(it) }

    @TypeConverter
    fun toStringList(value: String?): List<String>? =
        value?.let {
            val type = object : TypeToken<List<String>>() {}.type
            gson.fromJson(it, type)
        }

    // Map<String, String> ↔ JSON string (used for permissions_json etc.)
    @TypeConverter
    fun fromStringMap(value: Map<String, String>?): String? =
        value?.let { gson.toJson(it) }

    @TypeConverter
    fun toStringMap(value: String?): Map<String, String>? =
        value?.let {
            val type = object : TypeToken<Map<String, String>>() {}.type
            gson.fromJson(it, type)
        }
}
