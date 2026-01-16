package com.example.poketool.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PokemonDetailResponse(
    val id: Int,
    val name: String,
    val types: List<PokemonTypeSlot>,
    val sprites: PokemonSprites
)

@Serializable
data class PokemonTypeSlot(
    val slot: Int,
    val type: PokemonType
)

@Serializable
data class PokemonType(
    val name: String,
    val url: String
)

@Serializable
data class PokemonSprites(
    @SerialName("front_default")
    val frontDefault: String?
)
