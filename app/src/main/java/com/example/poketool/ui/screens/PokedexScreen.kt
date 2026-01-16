package com.example.poketool.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.poketool.data.mock.MockPokemon
import com.example.poketool.ui.components.PokemonItem
import com.example.poketool.ui.theme.PoketoolTheme

@Composable
fun PokedexScreen() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(MockPokemon.list) { pokemon ->
            PokemonItem(pokemon = pokemon)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PokedexScreenPreview() {
    PoketoolTheme {
        PokedexScreen()
    }
}
