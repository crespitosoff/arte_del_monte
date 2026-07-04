package com.example.arte_del_monte.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.arte_del_monte.data.db.AppDatabase
import com.example.arte_del_monte.data.db.entity.ClientEntity
import com.example.arte_del_monte.data.db.entity.DocumentEntity
import com.example.arte_del_monte.data.db.entity.DocumentItemEntity
import com.example.arte_del_monte.data.model.DocumentType
import com.example.arte_del_monte.data.model.DocumentStatus
import com.example.arte_del_monte.data.repository.ClientRepository
import com.example.arte_del_monte.data.repository.DocumentRepository
import com.example.arte_del_monte.domain.NumberingService
import com.example.arte_del_monte.domain.PricingCalculator
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.util.UUID

data class DocumentEditorUiState(
    val documentId: Long = 0L,
    val documentNumber: String = "",
    val documentType: DocumentType = DocumentType.COTIZACION,
    val date: Long = System.currentTimeMillis(),
    val clientId: Long = 0L,
    val clientName: String = "",
    val clientPhone: String = "",
    val clientAddress: String = "",
    val items: List<ItemUiState> = emptyList(),
    val globalDiscountPercent: Double = 0.0,
    val taxEnabled: Boolean = false,
    val taxRate: Double = 0.0,
    val notes: String = "",
    val status: DocumentStatus = DocumentStatus.BORRADOR,
    // Calculated
    val subtotal: Double = 0.0,
    val taxAmount: Double = 0.0,
    val total: Double = 0.0,
    val isSaving: Boolean = false,
    val savedDocumentId: Long? = null
)

class DocumentEditorViewModel(
    private val docRepo: DocumentRepository,
    private val clientRepo: ClientRepository,
    private val numberingService: NumberingService
) : ViewModel() {

    private val _uiState = MutableStateFlow(DocumentEditorUiState())
    val uiState: StateFlow<DocumentEditorUiState> = _uiState.asStateFlow()

    fun loadDocument(id: Long) {
        viewModelScope.launch {
            val (doc, items) = docRepo.getDocumentWithItems(id)
            if (doc != null) {
                _uiState.value = DocumentEditorUiState(
                    documentId = doc.id,
                    documentNumber = doc.number,
                    documentType = DocumentType.valueOf(doc.type),
                    date = doc.date,
                    clientId = doc.clientId,
                    clientName = doc.clientName,
                    clientPhone = doc.clientPhone,
                    clientAddress = doc.clientAddress,
                    items = items.map { it.toUiState() },
                    globalDiscountPercent = doc.discountGlobal,
                    taxEnabled = doc.taxEnabled,
                    taxRate = doc.taxRate,
                    notes = doc.notes,
                    status = DocumentStatus.valueOf(doc.status)
                ).recalculate()
            }
        }
    }

    fun initNew(type: DocumentType) {
        viewModelScope.launch {
            val number = numberingService.next(type)
            _uiState.update { it.copy(documentType = type, documentNumber = number) }
        }
    }

    fun setType(type: DocumentType) {
        viewModelScope.launch {
            val number = numberingService.next(type)
            _uiState.update { it.copy(documentType = type, documentNumber = number).recalculate() }
        }
    }

    fun setClient(client: ClientEntity) {
        _uiState.update {
            it.copy(
                clientId = client.id,
                clientName = client.name,
                clientPhone = client.phone,
                clientAddress = client.address
            )
        }
    }

    fun setClientName(name: String) = _uiState.update { it.copy(clientName = name) }
    fun setClientPhone(phone: String) = _uiState.update { it.copy(clientPhone = phone) }
    fun setClientAddress(addr: String) = _uiState.update { it.copy(clientAddress = addr) }
    fun setDate(date: Long) = _uiState.update { it.copy(date = date) }
    fun setNotes(notes: String) = _uiState.update { it.copy(notes = notes) }
    fun setGlobalDiscount(d: Double) = _uiState.update { it.copy(globalDiscountPercent = d).recalculate() }
    fun setTaxEnabled(enabled: Boolean) = _uiState.update { it.copy(taxEnabled = enabled).recalculate() }
    fun setTaxRate(rate: Double) = _uiState.update { it.copy(taxRate = rate).recalculate() }

    fun addItem() {
        _uiState.update { it.copy(items = it.items + ItemUiState()).recalculate() }
    }

    fun removeItem(id: String) {
        _uiState.update { it.copy(items = it.items.filter { item -> item.id != id }).recalculate() }
    }

    fun updateItem(updated: ItemUiState) {
        _uiState.update {
            it.copy(
                items = it.items.map { item -> if (item.id == updated.id) updated else item }
            ).recalculate()
        }
    }

    fun addPhoto(itemId: String, path: String) {
        _uiState.update {
            it.copy(items = it.items.map { item ->
                if (item.id == itemId) item.copy(photoPaths = item.photoPaths + path)
                else item
            })
        }
    }

    fun removePhoto(itemId: String, path: String) {
        _uiState.update {
            it.copy(items = it.items.map { item ->
                if (item.id == itemId) item.copy(photoPaths = item.photoPaths - path)
                else item
            })
        }
    }

    fun save(onSuccess: (Long) -> Unit) {
        val state = _uiState.value
        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            try {
                // Create client on the fly if needed
                val clientId = if (state.clientId == 0L && state.clientName.isNotBlank()) {
                    clientRepo.save(
                        ClientEntity(
                            name = state.clientName,
                            phone = state.clientPhone,
                            address = state.clientAddress
                        )
                    )
                } else state.clientId

                val doc = DocumentEntity(
                    id = state.documentId,
                    type = state.documentType.name,
                    number = state.documentNumber,
                    date = state.date,
                    clientId = clientId,
                    clientName = state.clientName,
                    clientPhone = state.clientPhone,
                    clientAddress = state.clientAddress,
                    notes = state.notes,
                    discountGlobal = state.globalDiscountPercent,
                    taxRate = state.taxRate,
                    taxEnabled = state.taxEnabled,
                    status = state.status.name,
                    createdAt = if (state.documentId == 0L) System.currentTimeMillis() else 0L,
                    updatedAt = System.currentTimeMillis()
                )

                val items = state.items.mapIndexed { idx, item ->
                    DocumentItemEntity(
                        id = item.dbId,
                        documentId = state.documentId,
                        sortOrder = idx,
                        description = item.description,
                        quantity = item.quantity,
                        unit = item.unit,
                        unitPrice = item.unitPrice,
                        discountPercent = item.discountPercent,
                        photoPaths = JSONArray(item.photoPaths).toString()
                    )
                }

                val savedId = docRepo.saveDocument(doc, items)
                _uiState.update {
                    it.copy(isSaving = false, savedDocumentId = savedId, documentId = savedId)
                }
                onSuccess(savedId)
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false) }
            }
        }
    }

    private fun DocumentEditorUiState.recalculate(): DocumentEditorUiState {
        val (afterDiscount, tax, total) = PricingCalculator.grandTotal(
            items, globalDiscountPercent, taxRate, taxEnabled
        )
        return copy(subtotal = afterDiscount, taxAmount = tax, total = total)
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val db = AppDatabase.getInstance(context)
            val docRepo = DocumentRepository(db, db.documentDao(), db.documentItemDao(), db.documentCounterDao())
            val clientRepo = ClientRepository(db.clientDao())
            val numbering = NumberingService(db.documentCounterDao())
            @Suppress("UNCHECKED_CAST")
            return DocumentEditorViewModel(docRepo, clientRepo, numbering) as T
        }
    }
}

// Extension to convert entity → UI state
fun DocumentItemEntity.toUiState(): ItemUiState {
    val paths = try {
        val arr = JSONArray(photoPaths)
        (0 until arr.length()).map { arr.getString(it) }
    } catch (e: Exception) {
        emptyList()
    }
    return ItemUiState(
        id = UUID.randomUUID().toString(),
        dbId = id,
        description = description,
        quantity = quantity,
        unit = unit,
        unitPrice = unitPrice,
        discountPercent = discountPercent,
        photoPaths = paths
    )
}
