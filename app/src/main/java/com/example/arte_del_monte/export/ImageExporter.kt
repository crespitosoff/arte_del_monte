package com.example.arte_del_monte.export

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

/**
 * Exporta páginas renderizadas como imágenes PNG.
 * - 1 página  → un solo archivo PNG
 * - N páginas → N archivos PNG (nombres: COT0001_p1.png, COT0001_p2.png, ...)
 */
object ImageExporter {

    /**
     * @return Lista de URIs (una por imagen exportada)
     */
    fun export(context: Context, pages: List<Bitmap>, fileName: String): List<Uri> {
        val exportDir = File(context.cacheDir, "exports").also { it.mkdirs() }
        return pages.mapIndexed { index, bitmap ->
            val name = if (pages.size == 1) "$fileName.png" else "${fileName}_p${index + 1}.png"
            val file = File(exportDir, name)
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 95, out)
            }
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        }
    }
}
