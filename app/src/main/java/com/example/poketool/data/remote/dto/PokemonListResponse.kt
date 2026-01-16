package com.example.poketool.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class PokemonListResponse(
    val count: Int,
    val results: List<PokemonListItem>
)

@Serializable
data class PokemonListItem(
    val name: String,
    val url: String
) {
    val id: Int
        get() = url.trimEnd('/').substringAfterLast('/').toInt()
}
