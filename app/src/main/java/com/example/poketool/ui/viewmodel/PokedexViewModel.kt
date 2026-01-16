package com.example.poketool.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.poketool.data.model.Pokemon
import com.example.poketool.data.repository.PokemonRepository
import com.example.poketool.ui.components.SearchMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class PokedexViewModel(
    private val repository: PokemonRepository
) : ViewModel() {

    private val allPokemon: StateFlow<List<Pokemon>> = repository.getAllPokemon()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _searchMode = MutableStateFlow(SearchMode.NAME)
    val searchMode: StateFlow<SearchMode> = _searchMode

    val pokemonList: StateFlow<List<Pokemon>> = combine(
        allPokemon,
        _searchQuery,
        _searchMode
    ) { pokemon, query, mode ->
        if (query.isBlank()) {
            pokemon
        } else {
            pokemon.filter { p ->
                when (mode) {
                    SearchMode.ID -> p.id.toString().contains(query.trim())
                    SearchMode.NAME -> p.name.contains(query.trim(), ignoreCase = true)
                }
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _loadingDetails = MutableStateFlow<Set<Int>>(emptySet())
    val loadingDetails: StateFlow<Set<Int>> = _loadingDetails

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateSearchMode(mode: SearchMode) {
        _searchMode.value = mode
        _searchQuery.value = ""
    }

    private val _selectedPokemon = MutableStateFlow<Pokemon?>(null)
    val selectedPokemon: StateFlow<Pokemon?> = _selectedPokemon

    private val _loadingFullDetails = MutableStateFlow(false)
    val loadingFullDetails: StateFlow<Boolean> = _loadingFullDetails

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

    fun selectPokemon(pokemon: Pokemon) {
        _selectedPokemon.value = pokemon
        if (!pokemon.hasFullDetails) {
            fetchFullDetails(pokemon.id)
        }
    }

    fun clearSelectedPokemon() {
        _selectedPokemon.value = null
    }

    private fun fetchFullDetails(pokemonId: Int) {
        viewModelScope.launch {
            _loadingFullDetails.value = true
            val result = repository.fetchPokemonFullDetails(pokemonId)
            result.onSuccess { pokemon ->
                _selectedPokemon.value = pokemon
            }
            _loadingFullDetails.value = false
        }
    }

    class Factory(private val repository: PokemonRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PokedexViewModel(repository) as T
        }
    }
}
