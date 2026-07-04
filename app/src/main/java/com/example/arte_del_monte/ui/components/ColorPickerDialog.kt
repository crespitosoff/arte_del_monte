package com.example.arte_del_monte.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun ColorPickerDialog(
    initialColor: Int,
    onColorSelected: (Int) -> Unit,
    onDismiss: () -> Unit,
    title: String = "Seleccionar color"
) {
    var red by remember { mutableFloatStateOf(android.graphics.Color.red(initialColor).toFloat()) }
    var green by remember { mutableFloatStateOf(android.graphics.Color.green(initialColor).toFloat()) }
    var blue by remember { mutableFloatStateOf(android.graphics.Color.blue(initialColor).toFloat()) }

    val currentColor = Color(red / 255f, green / 255f, blue / 255f)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(title, style = MaterialTheme.typography.titleMedium)

                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(currentColor)
                )

                SliderRow("R", red, 0f, 255f) { red = it }
                SliderRow("G", green, 0f, 255f) { green = it }
                SliderRow("B", blue, 0f, 255f) { blue = it }

                Text(
                    "#${Integer.toHexString(currentColor.toArgb()).uppercase().drop(2)}",
                    style = MaterialTheme.typography.bodySmall
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancelar") }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = {
                        onColorSelected(currentColor.toArgb())
                        onDismiss()
                    }) { Text("Aceptar") }
                }
            }
        }
    }
}

@Composable
private fun SliderRow(label: String, value: Float, min: Float, max: Float, onChanged: (Float) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.labelLarge, modifier = Modifier.width(24.dp))
        Slider(
            value = value,
            onValueChange = onChanged,
            valueRange = min..max,
            modifier = Modifier.weight(1f)
        )
        Text(value.toInt().toString(), modifier = Modifier.width(36.dp))
    }
}
