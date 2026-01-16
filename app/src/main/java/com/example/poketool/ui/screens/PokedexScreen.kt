package com.example.poketool.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.poketool.data.model.Pokemon
import com.example.poketool.ui.components.PokemonDetailBottomSheet
import com.example.poketool.ui.components.PokemonItem
import com.example.poketool.ui.components.PokemonSearchBar
import com.example.poketool.ui.viewmodel.PokedexViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PokedexScreen(viewModel: PokedexViewModel) {
    val pokemonList by viewModel.pokemonList.collectAsState()
    val loadingDetails by viewModel.loadingDetails.collectAsState()
    val selectedPokemon by viewModel.selectedPokemon.collectAsState()
    val loadingFullDetails by viewModel.loadingFullDetails.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchMode by viewModel.searchMode.collectAsState()

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize()) {
        PokemonSearchBar(
            query = searchQuery,
            onQueryChange = { viewModel.updateSearchQuery(it) },
            searchMode = searchMode,
            onSearchModeChange = { viewModel.updateSearchMode(it) },
            modifier = Modifier.padding(16.dp)
        )

        if (pokemonList.isEmpty() && searchQuery.isBlank()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (pokemonList.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No Pokemon found",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(pokemonList, key = { it.id }) { pokemon ->
                    PokemonItemWithLazyLoad(
                        pokemon = pokemon,
                        isLoading = loadingDetails.contains(pokemon.id),
                        onVisible = { viewModel.fetchDetailsIfNeeded(pokemon) },
                        onClick = { viewModel.selectPokemon(pokemon) }
                    )
                }
            }
        }
    }

    selectedPokemon?.let { pokemon ->
        PokemonDetailBottomSheet(
            pokemon = pokemon,
            isLoading = loadingFullDetails,
            sheetState = sheetState,
            onDismiss = {
                scope.launch {
                    sheetState.hide()
                    viewModel.clearSelectedPokemon()
                }
            }
        )
    }
}

@Composable
private fun PokemonItemWithLazyLoad(
    pokemon: Pokemon,
    isLoading: Boolean,
    onVisible: () -> Unit,
    onClick: () -> Unit
) {
    LaunchedEffect(pokemon.id) {
        onVisible()
    }

    PokemonItem(
        pokemon = pokemon,
        isLoadingDetails = isLoading,
        modifier = Modifier.clickable { onClick() }
    )
}
