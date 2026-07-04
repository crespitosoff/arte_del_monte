package com.example.arte_del_monte.data.settings

data class BrandSettings(
    val businessName: String = "Arte de Monte",
    val slogan: String = "Mobiliario macizo de alta gama",
    val phone: String = "",
    val location: String = "",
    val nit: String = "",
    val logoPath: String = "",
    val colorPrimary: Int = 0xFF3C2619.toInt(),   // Roble Oscuro
    val colorSecondary: Int = 0xFF745038.toInt(), // Café Monte
    val colorEmphasis: Int = 0xFF201E1D.toInt(),  // Negro Mate
    val colorBackground: Int = 0xFFE9DECE.toInt(), // Crema Papel
    val taxEnabled: Boolean = false,
    val taxRate: Double = 0.0
)
