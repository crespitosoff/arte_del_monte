package com.example.arte_del_monte.data.db

import androidx.room.TypeConverter
import com.example.arte_del_monte.data.model.DocumentType

class Converters {

    @TypeConverter
    fun fromDocumentType(value: DocumentType): String = value.name

    @TypeConverter
    fun toDocumentType(value: String): DocumentType = DocumentType.valueOf(value)
}
