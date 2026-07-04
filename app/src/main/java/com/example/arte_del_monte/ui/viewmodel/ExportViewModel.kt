package com.example.arte_del_monte.ui.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.arte_del_monte.data.db.AppDatabase
import com.example.arte_del_monte.data.db.entity.DocumentEntity
import com.example.arte_del_monte.data.db.entity.DocumentItemEntity
import com.example.arte_del_monte.data.repository.DocumentRepository
import com.example.arte_del_monte.data.settings.BrandSettings
import com.example.arte_del_monte.data.settings.BrandSettingsDataStore
import com.example.arte_del_monte.export.DocumentRenderer
import com.example.arte_del_monte.export.ImageExporter
import com.example.arte_del_monte.export.PdfExporter
import com.example.arte_del_monte.export.ShareHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed class ExportState {
    object Idle : ExportState()
    object Rendering : ExportState()
    data class Ready(val pages: List<Bitmap>, val pdfUri: Uri?, val imageUris: List<Uri>) : ExportState()
    data class Error(val message: String) : ExportState()
}

class ExportViewModel(
    private val docRepo: DocumentRepository,
    private val brandStore: BrandSettingsDataStore,
    private val context: Context
) : ViewModel() {

    private val _exportState = MutableStateFlow<ExportState>(ExportState.Idle)
    val exportState: StateFlow<ExportState> = _exportState.asStateFlow()

    /** Renderiza el documento y genera PDF e imágenes en background */
    fun prepareExport(documentId: Long) {
        _exportState.value = ExportState.Rendering
        viewModelScope.launch {
            try {
                val (doc, items) = docRepo.getDocumentWithItems(documentId)
                if (doc == null) {
                    _exportState.value = ExportState.Error("Documento no encontrado")
                    return@launch
                }
                val brand = brandStore.settings.first()

                val pages = withContext(Dispatchers.Default) {
                    DocumentRenderer.render(context, doc, items, brand)
                }

                val docNumber = doc.number
                val pdfUri = withContext(Dispatchers.IO) {
                    PdfExporter.export(context, pages, docNumber)
                }
                val imageUris = withContext(Dispatchers.IO) {
                    ImageExporter.export(context, pages, docNumber)
                }

                _exportState.value = ExportState.Ready(pages, pdfUri, imageUris)
            } catch (e: Exception) {
                _exportState.value = ExportState.Error(e.message ?: "Error al exportar")
            }
        }
    }

    fun sharePdf(pdfUri: Uri) {
        ShareHelper.shareFile(context, pdfUri, "application/pdf")
    }

    fun shareImages(uris: List<Uri>) {
        ShareHelper.shareMultipleImages(context, uris)
    }

    fun reset() {
        _exportState.value = ExportState.Idle
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val db = AppDatabase.getInstance(context)
            val repo = DocumentRepository(db, db.documentDao(), db.documentItemDao(), db.documentCounterDao())
            @Suppress("UNCHECKED_CAST")
            return ExportViewModel(repo, BrandSettingsDataStore(context), context) as T
        }
    }
}
