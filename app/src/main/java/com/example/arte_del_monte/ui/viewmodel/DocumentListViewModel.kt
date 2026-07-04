package com.example.arte_del_monte.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.arte_del_monte.data.db.AppDatabase
import com.example.arte_del_monte.data.db.entity.DocumentEntity
import com.example.arte_del_monte.data.model.DocumentType
import com.example.arte_del_monte.data.repository.DocumentRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class DocumentFilter(
    val type: DocumentType? = null,
    val clientQuery: String = ""
)

@OptIn(ExperimentalCoroutinesApi::class)
class DocumentListViewModel(private val repo: DocumentRepository) : ViewModel() {

    val filter = MutableStateFlow(DocumentFilter())

    val documents: StateFlow<List<DocumentEntity>> = filter
        .flatMapLatest { f ->
            when {
                f.type != null && f.clientQuery.isBlank() -> repo.getByType(f.type)
                f.clientQuery.isNotBlank() -> repo.searchByClient(f.clientQuery)
                else -> repo.allDocuments
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setTypeFilter(type: DocumentType?) {
        filter.value = filter.value.copy(type = type)
    }

    fun setClientQuery(q: String) {
        filter.value = filter.value.copy(clientQuery = q)
    }

    fun deleteDocument(id: Long) {
        viewModelScope.launch { repo.deleteDocument(id) }
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val db = AppDatabase.getInstance(context)
            @Suppress("UNCHECKED_CAST")
            val repo = DocumentRepository(db, db.documentDao(), db.documentItemDao(), db.documentCounterDao())
            return DocumentListViewModel(repo) as T
        }
    }
}
