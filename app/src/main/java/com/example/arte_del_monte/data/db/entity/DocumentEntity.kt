package com.example.arte_del_monte.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "documents")
data class DocumentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    /** Stored as the enum name string, e.g. "COTIZACION" */
    val type: String,

    /** Human-readable document number, e.g. "COT0001" */
    val number: String,

    /** Issue date as epoch milliseconds */
    val date: Long,

    /** FK reference to clients table */
    val clientId: Long,

    /** Snapshot of client data at the time of document creation */
    val clientName: String,
    val clientPhone: String,
    val clientAddress: String,

    val notes: String = "",

    /** Global discount applied on the document subtotal (percentage, e.g. 10.0 = 10 %) */
    val discountGlobal: Double = 0.0,

    /** Tax rate percentage (e.g. 16.0 = 16 %) */
    val taxRate: Double = 0.0,

    /** Whether tax is applied to this document */
    val taxEnabled: Boolean = false,

    /** DocumentStatus enum name stored as String, default "BORRADOR" */
    val status: String = "BORRADOR",

    /** Creation timestamp as epoch milliseconds */
    val createdAt: Long,

    /** Last update timestamp as epoch milliseconds */
    val updatedAt: Long
)
