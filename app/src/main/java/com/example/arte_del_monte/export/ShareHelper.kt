package com.example.arte_del_monte.export

import android.content.Context
import android.content.Intent
import android.net.Uri

/**
 * Crea y lanza Intents para compartir documentos exportados.
 * Intenta abrir directamente en WhatsApp si está instalado;
 * si no, usa el selector de sistema estándar.
 */
object ShareHelper {

    private const val WHATSAPP_PACKAGE = "com.whatsapp"
    private const val WHATSAPP_BUSINESS_PACKAGE = "com.whatsapp.w4b"
    private const val MIME_PDF = "application/pdf"
    private const val MIME_IMAGE = "image/png"
    private const val MIME_ANY = "*/*"

    /** Compartir un solo archivo (PDF o imagen única) */
    fun shareFile(context: Context, uri: Uri, mimeType: String = MIME_PDF) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        // Intentar abrir directamente en WhatsApp
        val whatsappIntent = Intent(intent).apply { `package` = WHATSAPP_PACKAGE }
        val whatsappBizIntent = Intent(intent).apply { `package` = WHATSAPP_BUSINESS_PACKAGE }

        val pm = context.packageManager
        when {
            pm.resolveActivity(whatsappIntent, 0) != null ->
                context.startActivity(whatsappIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            pm.resolveActivity(whatsappBizIntent, 0) != null ->
                context.startActivity(whatsappBizIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            else ->
                context.startActivity(
                    Intent.createChooser(intent, "Compartir documento")
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
        }
    }

    /** Compartir múltiples imágenes (páginas individuales como PNG) */
    fun shareMultipleImages(context: Context, uris: List<Uri>) {
        if (uris.isEmpty()) return
        if (uris.size == 1) {
            shareFile(context, uris.first(), MIME_IMAGE)
            return
        }

        val intent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
            type = MIME_IMAGE
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(uris))
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(
            Intent.createChooser(intent, "Compartir páginas del documento")
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }
}
