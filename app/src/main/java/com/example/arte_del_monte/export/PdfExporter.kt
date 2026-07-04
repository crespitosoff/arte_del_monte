package com.example.arte_del_monte.export

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

/**
 * Exporta una lista de Bitmaps (páginas) a un PDF multipágina nativo de Android.
 * Usa android.graphics.pdf.PdfDocument — sin licencias de terceros.
 */
object PdfExporter {

    /**
     * @param pages  Lista de Bitmaps, uno por página del documento
     * @param fileName Nombre del archivo sin extensión (ej. "COT0001")
     * @return URI compartible vía FileProvider
     */
    fun export(context: Context, pages: List<Bitmap>, fileName: String): Uri {
        val pdfDoc = PdfDocument()

        pages.forEachIndexed { index, bitmap ->
            val pageInfo = PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, index + 1).create()
            val page = pdfDoc.startPage(pageInfo)
            page.canvas.drawBitmap(bitmap, 0f, 0f, null)
            pdfDoc.finishPage(page)
        }

        val exportDir = File(context.cacheDir, "exports").also { it.mkdirs() }
        val file = File(exportDir, "$fileName.pdf")

        FileOutputStream(file).use { pdfDoc.writeTo(it) }
        pdfDoc.close()

        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }
}
