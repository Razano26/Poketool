package com.example.poketool.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.poketool.data.model.Pokemon
import com.example.poketool.data.repository.PokemonRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class PokedexViewModel(
    private val repository: PokemonRepository
) : ViewModel() {

    val pokemonList: StateFlow<List<Pokemon>> = repository.getAllPokemon()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _loadingDetails = MutableStateFlow<Set<Int>>(emptySet())
    val loadingDetails: StateFlow<Set<Int>> = _loadingDetails

    private val fetchMutex = Mutex()
    private val fetchedIds = mutableSetOf<Int>()

    fun fetchDetailsIfNeeded(pokemon: Pokemon) {
        if (pokemon.hasDetails) return

        viewModelScope.launch {
            val shouldFetch = fetchMutex.withLock {
                if (fetchedIds.contains(pokemon.id)) {
                    false
                } else {
                    fetchedIds.add(pokemon.id)
                    true
                }
            }

            if (shouldFetch) {
                _loadingDetails.value = _loadingDetails.value + pokemon.id
                repository.fetchPokemonDetails(pokemon.id)
                _loadingDetails.value = _loadingDetails.value - pokemon.id
            }
        }
    }

    class Factory(private val repository: PokemonRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PokedexViewModel(repository) as T
        }
    }
}
