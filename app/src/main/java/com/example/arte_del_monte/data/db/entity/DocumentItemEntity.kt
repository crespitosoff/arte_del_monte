package com.example.arte_del_monte.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "document_items",
    foreignKeys = [
        ForeignKey(
            entity = DocumentEntity::class,
            parentColumns = ["id"],
            childColumns = ["documentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["documentId"])]
)
data class DocumentItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    /** FK to documents(id) — cascades delete */
    val documentId: Long,

    /** Display order within the document */
    val sortOrder: Int,

    val description: String,

    val quantity: Double,

    /** Unit of measure label, e.g. "unidad", "m²", "hr" */
    val unit: String = "unidad",

    val unitPrice: Double,

    /** Per-line discount percentage (e.g. 5.0 = 5 %) */
    val discountPercent: Double = 0.0,

    /**
     * JSON array of absolute file paths stored as a plain String.
     * Example: '["\/storage\/emulated\/0\/...\/photo1.jpg","...\/photo2.jpg"]'
     * Defaults to empty array representation "".
     */
    val photoPaths: String = ""
)
