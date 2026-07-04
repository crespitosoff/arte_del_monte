package com.example.arte_del_monte.export

import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.arte_del_monte.data.db.entity.DocumentEntity
import com.example.arte_del_monte.data.db.entity.DocumentItemEntity
import com.example.arte_del_monte.data.model.DocumentType
import com.example.arte_del_monte.data.settings.BrandSettings
import com.example.arte_del_monte.domain.PricingCalculator
import com.example.arte_del_monte.ui.viewmodel.ItemUiState
import org.json.JSONArray
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.min

/**
 * Renderiza el documento a una lista de Bitmaps (una por página).
 * Página 0: datos del documento (encabezado, tabla de ítems, totales, notas)
 * Páginas 1..N: una foto grande por página con etiqueta
 */
object DocumentRenderer {

    private const val PAGE_WIDTH = 1240   // ~A4 a 150 DPI
    private const val PAGE_HEIGHT = 1754
    private const val MARGIN = 72
    private const val LOGO_SIZE = 160

    fun render(
        context: Context,
        doc: DocumentEntity,
        items: List<DocumentItemEntity>,
        brand: BrandSettings
    ): List<Bitmap> {

        val pages = mutableListOf<Bitmap>()

        // ── Colores de marca ───────────────────────────────────────────────
        val colorPrimary   = brand.colorPrimary
        val colorSecondary = brand.colorSecondary
        val colorEmphasis  = brand.colorEmphasis
        val colorBackground = brand.colorBackground
        val colorCream     = brand.colorBackground

        // ── Página 1: documento ────────────────────────────────────────────
        val page1 = Bitmap.createBitmap(PAGE_WIDTH, PAGE_HEIGHT, Bitmap.Config.ARGB_8888)
        val canvas1 = Canvas(page1)
        canvas1.drawColor(colorBackground)

        val y = Ref(MARGIN.toFloat())

        // Header
        drawHeader(canvas1, context, doc, brand, y)
        // Tipo y número
        drawDocumentTitle(canvas1, doc, colorPrimary, colorSecondary, y)
        // Datos del cliente
        drawClientSection(canvas1, doc, colorPrimary, colorSecondary, y)
        // Línea divisoria
        drawDivider(canvas1, colorSecondary, y)
        // Tabla de ítems
        val itemUiStates = items.map { it.toSimpleUiState() }
        drawItemsTable(canvas1, itemUiStates, colorPrimary, colorSecondary, colorBackground, colorEmphasis, y)
        // Totales
        drawTotals(canvas1, doc, itemUiStates, colorEmphasis, colorCream, y)
        // Notas
        if (doc.notes.isNotBlank()) drawNotes(canvas1, doc.notes, colorPrimary, colorSecondary, y)
        // Pie de página
        val allPhotos = items.flatMap { item ->
            try {
                val arr = JSONArray(item.photoPaths)
                (0 until arr.length()).map { arr.getString(it) }
            } catch (e: Exception) { emptyList() }
        }
        val totalPages = 1 + allPhotos.size
        drawFooter(canvas1, brand, 1, totalPages, colorSecondary)

        pages.add(page1)

        // ── Páginas de fotos ───────────────────────────────────────────────
        allPhotos.forEachIndexed { idx, photoPath ->
            val photoBitmap = BitmapFactory.decodeFile(photoPath)
            val page = Bitmap.createBitmap(PAGE_WIDTH, PAGE_HEIGHT, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(page)
            canvas.drawColor(colorBackground)

            if (photoBitmap != null) {
                val scaled = scaleBitmap(photoBitmap, PAGE_WIDTH - MARGIN * 2, PAGE_HEIGHT - MARGIN * 3 - 120)
                val left = (PAGE_WIDTH - scaled.width) / 2f
                canvas.drawBitmap(scaled, left, MARGIN.toFloat(), null)

                // Etiqueta de foto
                val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = colorPrimary
                    textSize = 36f
                    typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
                }
                canvas.drawText("Foto ${idx + 1}", MARGIN.toFloat(), PAGE_HEIGHT - MARGIN - 80f, labelPaint)
            }

            drawFooter(canvas, brand, idx + 2, totalPages, colorSecondary)
            pages.add(page)
        }

        return pages
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Secciones de dibujo
    // ─────────────────────────────────────────────────────────────────────────

    private fun drawHeader(canvas: Canvas, context: Context, doc: DocumentEntity,
                           brand: BrandSettings, y: Ref<Float>) {
        val top = y.value

        // Logo (izquierda)
        val logoBitmap: Bitmap? = if (brand.logoPath.isNotBlank()) {
            BitmapFactory.decodeFile(brand.logoPath)
        } else null

        if (logoBitmap != null) {
            val scaled = Bitmap.createScaledBitmap(logoBitmap, LOGO_SIZE, LOGO_SIZE, true)
            canvas.drawBitmap(scaled, MARGIN.toFloat(), top, null)
        } else {
            // Placeholder montaña (triángulo simple)
            drawMountainIcon(canvas, MARGIN.toFloat(), top, LOGO_SIZE, brand.colorSecondary)
        }

        // Datos del negocio (derecha)
        val rightX = PAGE_WIDTH - MARGIN.toFloat()
        val namePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = brand.colorPrimary
            textSize = 44f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
        }
        val detailPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = brand.colorSecondary
            textSize = 28f
            typeface = Typeface.DEFAULT
            textAlign = Paint.Align.RIGHT
        }

        canvas.drawText(brand.businessName, rightX, top + 44f, namePaint)
        canvas.drawText(brand.slogan, rightX, top + 80f, detailPaint)
        if (brand.phone.isNotBlank())
            canvas.drawText("Tel: ${brand.phone}", rightX, top + 112f, detailPaint)
        if (brand.location.isNotBlank())
            canvas.drawText(brand.location, rightX, top + 144f, detailPaint)

        y.value = top + LOGO_SIZE + 32f
    }

    private fun drawMountainIcon(canvas: Canvas, x: Float, y: Float, size: Int, color: Int) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color; style = Paint.Style.FILL }
        val path = Path()
        path.moveTo(x, y + size)
        path.lineTo(x + size / 2f, y)
        path.lineTo(x + size.toFloat(), y + size)
        path.close()
        canvas.drawPath(path, paint)
        // Snow cap
        val snowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = Color.WHITE; style = Paint.Style.FILL }
        val snowPath = Path()
        snowPath.moveTo(x + size * 0.35f, y + size * 0.35f)
        snowPath.lineTo(x + size / 2f, y)
        snowPath.lineTo(x + size * 0.65f, y + size * 0.35f)
        snowPath.close()
        canvas.drawPath(snowPath, snowPaint)
    }

    private fun drawDocumentTitle(canvas: Canvas, doc: DocumentEntity,
                                  colorPrimary: Int, colorSecondary: Int, y: Ref<Float>) {
        val dateStr = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            .format(Date(doc.date))

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colorPrimary
            textSize = 52f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        }
        val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colorSecondary
            textSize = 30f
        }

        canvas.drawText(DocumentType.valueOf(doc.type).label.uppercase(), MARGIN.toFloat(), y.value + 52f, titlePaint)
        canvas.drawText("N° ${doc.number}   |   Fecha: $dateStr", MARGIN.toFloat(), y.value + 92f, subPaint)
        y.value += 120f
    }

    private fun drawClientSection(canvas: Canvas, doc: DocumentEntity,
                                  colorPrimary: Int, colorSecondary: Int, y: Ref<Float>) {
        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colorSecondary; textSize = 26f; typeface = Typeface.DEFAULT_BOLD
        }
        val valuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colorPrimary; textSize = 28f
        }

        canvas.drawText("CLIENTE", MARGIN.toFloat(), y.value + 26f, labelPaint)
        canvas.drawText(doc.clientName, MARGIN.toFloat(), y.value + 58f, valuePaint)
        if (doc.clientPhone.isNotBlank())
            canvas.drawText("Tel: ${doc.clientPhone}", MARGIN.toFloat(), y.value + 86f, labelPaint)
        if (doc.clientAddress.isNotBlank())
            canvas.drawText(doc.clientAddress, MARGIN.toFloat(), y.value + 112f, labelPaint)

        y.value += 128f
    }

    private fun drawDivider(canvas: Canvas, color: Int, y: Ref<Float>) {
        val paint = Paint().apply { this.color = color; strokeWidth = 2f }
        canvas.drawLine(MARGIN.toFloat(), y.value, (PAGE_WIDTH - MARGIN).toFloat(), y.value, paint)
        y.value += 24f
    }

    private fun drawItemsTable(canvas: Canvas, items: List<ItemUiState>,
                               colorPrimary: Int, colorSecondary: Int,
                               colorBackground: Int, colorEmphasis: Int, y: Ref<Float>) {
        // Header de tabla
        val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colorSecondary; textSize = 26f; typeface = Typeface.DEFAULT_BOLD
        }
        val rowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colorPrimary; textSize = 26f
        }
        val altRowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(30, Color.red(colorSecondary), Color.green(colorSecondary), Color.blue(colorSecondary))
            style = Paint.Style.FILL
        }

        val colDesc = MARGIN.toFloat()
        val colQty = PAGE_WIDTH * 0.55f
        val colPrice = PAGE_WIDTH * 0.70f
        val colTotal = PAGE_WIDTH * 0.85f

        canvas.drawText("Descripción", colDesc, y.value + 26f, headerPaint)
        canvas.drawText("Cant.", colQty, y.value + 26f, headerPaint)
        canvas.drawText("Precio", colPrice, y.value + 26f, headerPaint)
        canvas.drawText("Importe", colTotal, y.value + 26f, headerPaint)
        y.value += 36f

        val divPaint = Paint().apply { color = colorSecondary; strokeWidth = 1f; alpha = 80 }
        canvas.drawLine(MARGIN.toFloat(), y.value, (PAGE_WIDTH - MARGIN).toFloat(), y.value, divPaint)
        y.value += 12f

        items.forEachIndexed { idx, item ->
            val rowHeight = measureTextHeight(item.description, PAGE_WIDTH * 0.50f, 26f) + 40f

            if (idx % 2 == 1) {
                canvas.drawRect(MARGIN.toFloat(), y.value - 6f,
                    (PAGE_WIDTH - MARGIN).toFloat(), y.value + rowHeight - 6f, altRowPaint)
            }

            drawMultilineText(canvas, item.description, colDesc, y.value + 26f, PAGE_WIDTH * 0.50f, 26f, rowPaint)
            canvas.drawText("${formatQty(item.quantity)} ${item.unit}", colQty, y.value + 26f, rowPaint)
            canvas.drawText(PricingCalculator.formatCurrency(item.unitPrice), colPrice, y.value + 26f, rowPaint)
            val subtotal = PricingCalculator.lineSubtotal(item.quantity, item.unitPrice, item.discountPercent)
            canvas.drawText(PricingCalculator.formatCurrency(subtotal), colTotal, y.value + 26f, rowPaint)

            y.value += rowHeight
        }
        y.value += 16f
    }

    private fun drawTotals(canvas: Canvas, doc: DocumentEntity, items: List<ItemUiState>,
                           colorEmphasis: Int, colorCream: Int, y: Ref<Float>) {
        val (afterDiscount, tax, total) = PricingCalculator.grandTotal(
            items, doc.discountGlobal, doc.taxRate, doc.taxEnabled
        )

        val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colorEmphasis; textSize = 28f
        }
        val right = PAGE_WIDTH - MARGIN.toFloat()

        if (doc.discountGlobal > 0) {
            val subtotal = PricingCalculator.documentSubtotal(items)
            canvas.drawText("Subtotal:", MARGIN.toFloat(), y.value + 28f, subPaint)
            canvas.drawText(PricingCalculator.formatCurrency(subtotal), right, y.value + 28f,
                subPaint.apply { textAlign = Paint.Align.RIGHT })
            y.value += 40f
            canvas.drawText("Descuento (${doc.discountGlobal}%):", MARGIN.toFloat(), y.value + 28f, subPaint)
            canvas.drawText(PricingCalculator.formatCurrency(subtotal - afterDiscount), right, y.value + 28f,
                subPaint.apply { textAlign = Paint.Align.RIGHT; color = Color.RED })
            subPaint.color = colorEmphasis
            y.value += 40f
        }
        if (doc.taxEnabled && tax > 0) {
            canvas.drawText("IVA/Impuesto (${doc.taxRate}%):", MARGIN.toFloat(), y.value + 28f, subPaint)
            canvas.drawText(PricingCalculator.formatCurrency(tax), right, y.value + 28f,
                subPaint.apply { textAlign = Paint.Align.RIGHT })
            y.value += 40f
        }

        // Caja total (fondo Negro Mate)
        val boxHeight = 100f
        val boxPaint = Paint().apply { color = colorEmphasis; style = Paint.Style.FILL }
        canvas.drawRoundRect(
            MARGIN.toFloat(), y.value, (PAGE_WIDTH - MARGIN).toFloat(), y.value + boxHeight,
            12f, 12f, boxPaint
        )
        val totalLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colorCream; textSize = 36f; typeface = Typeface.DEFAULT_BOLD
        }
        val totalValuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colorCream; textSize = 42f; typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
        }
        canvas.drawText("TOTAL", MARGIN + 32f, y.value + 60f, totalLabelPaint)
        canvas.drawText(PricingCalculator.formatCurrency(total), right - 32f, y.value + 65f, totalValuePaint)
        y.value += boxHeight + 32f
    }

    private fun drawNotes(canvas: Canvas, notes: String,
                          colorPrimary: Int, colorSecondary: Int, y: Ref<Float>) {
        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colorSecondary; textSize = 26f; typeface = Typeface.DEFAULT_BOLD
        }
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colorPrimary; textSize = 25f
        }

        canvas.drawText("Condiciones y observaciones:", MARGIN.toFloat(), y.value + 26f, labelPaint)
        y.value += 44f

        drawMultilineText(canvas, notes, MARGIN.toFloat(), y.value, (PAGE_WIDTH - MARGIN * 2).toFloat(), 25f, textPaint)
        y.value += measureTextHeight(notes, (PAGE_WIDTH - MARGIN * 2).toFloat(), 25f) + 16f
    }

    private fun drawFooter(canvas: Canvas, brand: BrandSettings,
                           currentPage: Int, totalPages: Int, colorSecondary: Int) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colorSecondary; textSize = 24f; alpha = 160
        }
        val rightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colorSecondary; textSize = 24f; alpha = 160; textAlign = Paint.Align.RIGHT
        }
        val y = (PAGE_HEIGHT - 40).toFloat()
        canvas.drawText("Gracias por su confianza — ${brand.businessName}", MARGIN.toFloat(), y, paint)
        canvas.drawText("$currentPage / $totalPages", (PAGE_WIDTH - MARGIN).toFloat(), y, rightPaint)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Utilidades
    // ─────────────────────────────────────────────────────────────────────────

    private fun drawMultilineText(canvas: Canvas, text: String, x: Float, y: Float,
                                  maxWidth: Float, textSize: Float, paint: Paint) {
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var current = ""
        val tmpPaint = Paint(paint).apply { this.textSize = textSize }
        for (word in words) {
            val candidate = if (current.isEmpty()) word else "$current $word"
            if (tmpPaint.measureText(candidate) <= maxWidth) current = candidate
            else { if (current.isNotEmpty()) lines.add(current); current = word }
        }
        if (current.isNotEmpty()) lines.add(current)
        lines.forEachIndexed { i, line -> canvas.drawText(line, x, y + i * (textSize + 8f), paint) }
    }

    private fun measureTextHeight(text: String, maxWidth: Float, textSize: Float): Float {
        val words = text.split(" ")
        var lines = 1; var current = ""
        val tmpPaint = Paint().apply { this.textSize = textSize }
        for (word in words) {
            val candidate = if (current.isEmpty()) word else "$current $word"
            if (tmpPaint.measureText(candidate) <= maxWidth) current = candidate
            else { lines++; current = word }
        }
        return lines * (textSize + 8f)
    }

    private fun scaleBitmap(src: Bitmap, maxW: Int, maxH: Int): Bitmap {
        val ratio = min(maxW.toFloat() / src.width, maxH.toFloat() / src.height)
        return Bitmap.createScaledBitmap(src, (src.width * ratio).toInt(), (src.height * ratio).toInt(), true)
    }

    private fun formatQty(q: Double): String =
        if (q == q.toLong().toDouble()) q.toLong().toString() else "%.2f".format(q)

    private class Ref<T>(var value: T)
}

// Extension para convertir entidad a estado simple de cálculo
private fun DocumentItemEntity.toSimpleUiState() = ItemUiState(
    description = description,
    quantity = quantity,
    unit = unit,
    unitPrice = unitPrice,
    discountPercent = discountPercent,
    photoPaths = try {
        val arr = JSONArray(photoPaths)
        (0 until arr.length()).map { arr.getString(it) }
    } catch (e: Exception) { emptyList() }
)
