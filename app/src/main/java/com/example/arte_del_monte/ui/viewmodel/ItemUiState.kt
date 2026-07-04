package com.example.arte_del_monte.ui.viewmodel

import java.util.UUID

data class ItemUiState(
    val id: String = UUID.randomUUID().toString(), // local UI id (stable list key)
    val dbId: Long = 0L,
    val description: String = "",
    val quantity: Double = 1.0,
    val unit: String = "unidad",
    val unitPrice: Double = 0.0,
    val discountPercent: Double = 0.0,
    val photoPaths: List<String> = emptyList()
)
