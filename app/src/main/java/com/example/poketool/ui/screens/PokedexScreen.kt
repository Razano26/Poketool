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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.poketool.data.model.Pokemon
import com.example.poketool.ui.components.AdBanner
import com.example.poketool.ui.components.PokemonDetailBottomSheet
import com.example.poketool.ui.components.PokemonItem
import com.example.poketool.ui.components.PokemonSearchBar
import com.example.poketool.ui.viewmodel.PokedexViewModel
import com.example.poketool.ui.viewmodel.TeamViewModel
import kotlinx.coroutines.launch
import kotlin.random.Random

private sealed class PokedexListItem {
    data class PokemonListItem(val pokemon: Pokemon) : PokedexListItem()
    data class AdItem(val id: Int) : PokedexListItem()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PokedexScreen(
    viewModel: PokedexViewModel,
    teamViewModel: TeamViewModel
) {
    val pokemonList by viewModel.pokemonList.collectAsState()
    val loadingDetails by viewModel.loadingDetails.collectAsState()
    val selectedPokemon by viewModel.selectedPokemon.collectAsState()
    val loadingFullDetails by viewModel.loadingFullDetails.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchMode by viewModel.searchMode.collectAsState()

    val allTeams by teamViewModel.allTeams.collectAsState()
    val selectedTeamId by teamViewModel.selectedTeamId.collectAsState()

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
            // Create list with ads inserted at random positions
            val listWithAds = remember(pokemonList) {
                insertAdsRandomly(pokemonList)
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = listWithAds,
                    key = { item ->
                        when (item) {
                            is PokedexListItem.PokemonListItem -> "pokemon_${item.pokemon.id}"
                            is PokedexListItem.AdItem -> "ad_${item.id}"
                        }
                    }
                ) { item ->
                    when (item) {
                        is PokedexListItem.PokemonListItem -> {
                            PokemonItemWithLazyLoad(
                                pokemon = item.pokemon,
                                isLoading = loadingDetails.contains(item.pokemon.id),
                                onVisible = { viewModel.fetchDetailsIfNeeded(item.pokemon) },
                                onClick = { viewModel.selectPokemon(item.pokemon) }
                            )
                        }
                        is PokedexListItem.AdItem -> {
                            AdBanner()
                        }
                    }
                }
            }
        }
    }

    selectedPokemon?.let { pokemon ->
        PokemonDetailBottomSheet(
            pokemon = pokemon,
            isLoading = loadingFullDetails,
            sheetState = sheetState,
            teams = allTeams,
            selectedTeamId = selectedTeamId,
            onAddToTeam = { teamId ->
                teamViewModel.selectTeam(teamId)
                teamViewModel.addPokemonToTeam(pokemon.id)
            },
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

/**
 * Inserts ads at random positions in the Pokemon list.
 * Ads are inserted approximately every 5-10 Pokemon cards.
 */
private fun insertAdsRandomly(pokemonList: List<Pokemon>): List<PokedexListItem> {
    if (pokemonList.isEmpty()) return emptyList()

    val result = mutableListOf<PokedexListItem>()
    var adCounter = 0
    var nextAdPosition = Random.nextInt(5, 11) // First ad after 5-10 items

    pokemonList.forEachIndexed { index, pokemon ->
        result.add(PokedexListItem.PokemonListItem(pokemon))

        // Insert ad at random intervals (every 5-10 Pokemon)
        if (index + 1 == nextAdPosition && index < pokemonList.size - 1) {
            result.add(PokedexListItem.AdItem(adCounter++))
            nextAdPosition += Random.nextInt(5, 11)
        }
    }

    return result
}
