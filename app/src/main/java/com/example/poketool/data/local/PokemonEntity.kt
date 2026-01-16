package com.example.poketool.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.poketool.data.model.Pokemon
import com.example.poketool.data.model.PokemonStatInfo

@Entity(tableName = "pokemon")
data class PokemonEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val types: String? = null,
    val imageUrl: String? = null,
    val height: Int? = null,
    val weight: Int? = null,
    val stats: String? = null,
    val abilities: String? = null
) {
    fun toDomain(): Pokemon = Pokemon(
        id = id,
        name = name,
        types = types?.split(",") ?: emptyList(),
        imageUrl = imageUrl,
        height = height,
        weight = weight,
        stats = stats?.split(";")?.mapNotNull { statStr ->
            val parts = statStr.split(":")
            if (parts.size == 2) {
                PokemonStatInfo(name = parts[0], baseStat = parts[1].toIntOrNull() ?: 0)
            } else null
        } ?: emptyList(),
        abilities = abilities?.split(",") ?: emptyList()
    )

    companion object {
        fun fromDomain(pokemon: Pokemon): PokemonEntity = PokemonEntity(
            id = pokemon.id,
            name = pokemon.name,
            types = pokemon.types.takeIf { it.isNotEmpty() }?.joinToString(","),
            imageUrl = pokemon.imageUrl,
            height = pokemon.height,
            weight = pokemon.weight,
            stats = pokemon.stats.takeIf { it.isNotEmpty() }
                ?.joinToString(";") { "${it.name}:${it.baseStat}" },
            abilities = pokemon.abilities.takeIf { it.isNotEmpty() }?.joinToString(",")
        )
    }
}
