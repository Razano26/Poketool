package com.example.poketool.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.poketool.data.repository.PokemonRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class SyncState {
    data object Idle : SyncState()
    data object Loading : SyncState()
    data class Success(val count: Int) : SyncState()
    data class Error(val message: String) : SyncState()
}

class SyncViewModel(
    private val repository: PokemonRepository
) : ViewModel() {

    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    private val _needsSync = MutableStateFlow(true)
    val needsSync: StateFlow<Boolean> = _needsSync.asStateFlow()

    init {
        checkIfSyncNeeded()
    }

    private fun checkIfSyncNeeded() {
        viewModelScope.launch {
            _needsSync.value = repository.isDatabaseEmpty()
        }
    }

    fun startSync() {
        viewModelScope.launch {
            _syncState.value = SyncState.Loading
            repository.syncPokemonList()
                .onSuccess { count ->
                    _syncState.value = SyncState.Success(count)
                    _needsSync.value = false
                }
                .onFailure { error ->
                    _syncState.value = SyncState.Error(error.message ?: "Unknown error")
                }
        }
    }

    class Factory(private val repository: PokemonRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SyncViewModel(repository) as T
        }
    }
}
