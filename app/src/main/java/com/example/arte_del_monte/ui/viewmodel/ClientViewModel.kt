package com.example.arte_del_monte.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.arte_del_monte.data.db.AppDatabase
import com.example.arte_del_monte.data.db.entity.ClientEntity
import com.example.arte_del_monte.data.repository.ClientRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class ClientViewModel(private val repo: ClientRepository) : ViewModel() {

    val searchQuery = MutableStateFlow("")

    val clients: StateFlow<List<ClientEntity>> = searchQuery
        .debounce(300)
        .flatMapLatest { q ->
            if (q.isBlank()) repo.allClients
            else repo.search(q)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun search(q: String) { searchQuery.value = q }

    fun save(client: ClientEntity, onResult: (Long) -> Unit) {
        viewModelScope.launch { onResult(repo.save(client)) }
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val db = AppDatabase.getInstance(context)
            @Suppress("UNCHECKED_CAST")
            return ClientViewModel(ClientRepository(db.clientDao())) as T
        }
    }
}
