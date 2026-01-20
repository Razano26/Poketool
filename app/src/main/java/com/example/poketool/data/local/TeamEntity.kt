package com.example.poketool.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "teams")
data class TeamEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val pokemonIds: String = ""
) {
    fun getPokemonIdList(): List<Int> {
        return if (pokemonIds.isBlank()) {
            emptyList()
        } else {
            pokemonIds.split(",").mapNotNull { it.toIntOrNull() }
        }
    }

    fun withPokemon(pokemonId: Int): TeamEntity {
        val currentIds = getPokemonIdList()
        if (currentIds.size >= 6) return this
        val newIds = currentIds + pokemonId
        return copy(pokemonIds = newIds.joinToString(","))
    }

    fun withoutPokemonAtIndex(index: Int): TeamEntity {
        val currentIds = getPokemonIdList().toMutableList()
        if (index !in currentIds.indices) return this
        currentIds.removeAt(index)
        return copy(pokemonIds = currentIds.joinToString(","))
    }

    fun withReorderedPokemon(fromIndex: Int, toIndex: Int): TeamEntity {
        val currentIds = getPokemonIdList().toMutableList()
        if (fromIndex !in currentIds.indices || toIndex !in currentIds.indices) return this
        val item = currentIds.removeAt(fromIndex)
        currentIds.add(toIndex, item)
        return copy(pokemonIds = currentIds.joinToString(","))
    }

    companion object {
        const val MAX_TEAM_SIZE = 6
    }
}
