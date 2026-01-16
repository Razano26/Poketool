package com.example.poketool.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.poketool.data.model.Pokemon
import com.example.poketool.ui.components.PokemonItem
import com.example.poketool.ui.viewmodel.PokedexViewModel

@Composable
fun PokedexScreen(viewModel: PokedexViewModel) {
    val pokemonList by viewModel.pokemonList.collectAsState()
    val loadingDetails by viewModel.loadingDetails.collectAsState()

    if (pokemonList.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(pokemonList, key = { it.id }) { pokemon ->
                PokemonItemWithLazyLoad(
                    pokemon = pokemon,
                    isLoading = loadingDetails.contains(pokemon.id),
                    onVisible = { viewModel.fetchDetailsIfNeeded(pokemon) }
                )
            }
        }
    }
}

@Composable
private fun PokemonItemWithLazyLoad(
    pokemon: Pokemon,
    isLoading: Boolean,
    onVisible: () -> Unit
) {
    LaunchedEffect(pokemon.id) {
        onVisible()
    }

    PokemonItem(
        pokemon = pokemon,
        isLoadingDetails = isLoading
    )
}
