package com.example.poketool.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.poketool.data.model.Pokemon

@Entity(tableName = "pokemon")
data class PokemonEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val types: String? = null,
    val imageUrl: String? = null
) {
    fun toDomain(): Pokemon = Pokemon(
        id = id,
        name = name,
        types = types?.split(",") ?: emptyList(),
        imageUrl = imageUrl
    )

    companion object {
        fun fromDomain(pokemon: Pokemon): PokemonEntity = PokemonEntity(
            id = pokemon.id,
            name = pokemon.name,
            types = pokemon.types.takeIf { it.isNotEmpty() }?.joinToString(","),
            imageUrl = pokemon.imageUrl
        )
    }
}
