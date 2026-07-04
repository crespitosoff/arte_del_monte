package com.example.arte_del_monte.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "document_counters")
data class DocumentCounter(
    @PrimaryKey
    val type: String,
    val lastNumber: Int = 0
)
