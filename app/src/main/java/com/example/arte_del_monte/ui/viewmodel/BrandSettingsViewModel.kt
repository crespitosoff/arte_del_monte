package com.example.arte_del_monte.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.arte_del_monte.data.settings.BrandSettings
import com.example.arte_del_monte.data.settings.BrandSettingsDataStore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BrandSettingsViewModel(private val store: BrandSettingsDataStore) : ViewModel() {

    val settings: StateFlow<BrandSettings> = store.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BrandSettings())

    fun save(s: BrandSettings) {
        viewModelScope.launch { store.save(s) }
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return BrandSettingsViewModel(BrandSettingsDataStore(context)) as T
        }
    }
}
