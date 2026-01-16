package com.example.poketool.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PokemonDetailResponse(
    val id: Int,
    val name: String,
    val height: Int,
    val weight: Int,
    val types: List<PokemonTypeSlot>,
    val stats: List<PokemonStatSlot>,
    val abilities: List<PokemonAbilitySlot>,
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

@Serializable
data class PokemonStatSlot(
    @SerialName("base_stat")
    val baseStat: Int,
    val effort: Int,
    val stat: PokemonStat
)

@Serializable
data class PokemonStat(
    val name: String,
    val url: String
)

@Serializable
data class PokemonAbilitySlot(
    val ability: PokemonAbility,
    @SerialName("is_hidden")
    val isHidden: Boolean,
    val slot: Int
)

@Serializable
data class PokemonAbility(
    val name: String,
    val url: String
)
