package com.example.arte_del_monte.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.arte_del_monte.ui.theme.*
import java.io.File
import java.io.FileOutputStream

@Composable
fun PhotoPickerRow(
    photos: List<String>,
    onPhotoAdded: (String) -> Unit,
    onPhotoRemoved: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val galleryPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val destDir = File(context.filesDir, "photos").also { d -> d.mkdirs() }
            val dest = File(destDir, "photo_${System.currentTimeMillis()}.jpg")
            context.contentResolver.openInputStream(it)?.use { input ->
                FileOutputStream(dest).use { output -> input.copyTo(output) }
            }
            onPhotoAdded(dest.absolutePath)
        }
    }

    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(end = 8.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .border(2.dp, CafeMonte.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .clickable { galleryPicker.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.AddAPhoto, null, tint = CafeMonte, modifier = Modifier.size(28.dp))
                    Text("Foto", style = MaterialTheme.typography.bodySmall, color = CafeMonte)
                }
            }
        }
        items(photos) { path ->
            Box(modifier = Modifier.size(80.dp)) {
                AsyncImage(
                    model = File(path),
                    contentDescription = "Foto del ítem",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
                IconButton(
                    onClick = { onPhotoRemoved(path) },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(24.dp)
                        .background(Color.White.copy(alpha = 0.7f), CircleShape)
                ) {
                    Icon(
                        Icons.Default.Cancel,
                        contentDescription = "Quitar foto",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
