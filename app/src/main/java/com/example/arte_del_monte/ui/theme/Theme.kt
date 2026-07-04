package com.example.arte_del_monte.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ArteDelMonteColorScheme = lightColorScheme(
    primary             = CafeMonte,
    onPrimary           = CremaPapel,
    primaryContainer    = CremaPapelDark,
    onPrimaryContainer  = RobleOscuro,
    secondary           = RobleOscuro,
    onSecondary         = CremaPapel,
    secondaryContainer  = CremaPapelLight,
    onSecondaryContainer= RobleOscuro,
    tertiary            = NegroMate,
    onTertiary          = CremaPapel,
    background          = CremaPapelLight,
    onBackground        = NegroMate,
    surface             = CremaPapel,
    onSurface           = NegroMate,
    surfaceVariant      = CremaPapelLight,
    onSurfaceVariant    = RobleOscuro,
    error               = ErrorRed,
    onError             = Color.White,
    outline             = CafeMonte,
    outlineVariant      = CremaPapelDark
)

@Composable
fun Arte_del_monteTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ArteDelMonteColorScheme,
        typography  = ArteDelMonteTypography,
        content     = content
    )
}