package com.xallery.common.repository.db.converter

import androidx.room.TypeConverter

class StringListConverter {

    @TypeConverter
    fun fromList(list: List<String>?): String {
        return list?.joinToString(",") ?: ""
    }

    @TypeConverter
    fun toList(str: String?): List<String> {
        return str?.split(",") ?: listOf()
    }
}