package com.example.poketool.data.mock

import com.example.poketool.data.model.Pokemon

object MockPokemon {
    val list = listOf(
        Pokemon(1, "Bulbasaur", listOf("Grass", "Poison")),
        Pokemon(4, "Charmander", listOf("Fire")),
        Pokemon(7, "Squirtle", listOf("Water")),
        Pokemon(25, "Pikachu", listOf("Electric")),
        Pokemon(39, "Jigglypuff", listOf("Normal", "Fairy")),
        Pokemon(52, "Meowth", listOf("Normal")),
        Pokemon(133, "Eevee", listOf("Normal")),
        Pokemon(143, "Snorlax", listOf("Normal")),
        Pokemon(150, "Mewtwo", listOf("Psychic")),
        Pokemon(151, "Mew", listOf("Psychic"))
    )
}
