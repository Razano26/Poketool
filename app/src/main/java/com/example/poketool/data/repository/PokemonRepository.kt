package com.example.poketool.data.repository

import com.example.poketool.data.local.PokemonDao
import com.example.poketool.data.local.PokemonEntity
import com.example.poketool.data.model.Pokemon
import com.example.poketool.data.remote.PokeApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PokemonRepository(
    private val pokemonDao: PokemonDao,
    private val pokeApiService: PokeApiService
) {

    fun getAllPokemon(): Flow<List<Pokemon>> {
        return pokemonDao.getAllPokemon().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun getPokemonCount(): Int {
        return pokemonDao.getPokemonCount()
    }

    suspend fun isDatabaseEmpty(): Boolean {
        return pokemonDao.getPokemonCount() == 0
    }

    suspend fun syncPokemonList(): Result<Int> {
        return try {
            val response = pokeApiService.getPokemonList(limit = 1500)
            val entities = response.results.map { item ->
                PokemonEntity(
                    id = item.id,
                    name = item.name.replaceFirstChar { it.uppercase() },
                    types = null,
                    imageUrl = null
                )
            }
            pokemonDao.insertAll(entities)
            Result.success(entities.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchPokemonDetails(pokemonId: Int): Result<Pokemon> {
        return try {
            val cached = pokemonDao.getPokemonById(pokemonId)
            if (cached?.types != null) {
                return Result.success(cached.toDomain())
            }

            val response = pokeApiService.getPokemonDetail(pokemonId)
            val types = response.types
                .sortedBy { it.slot }
                .joinToString(",") { it.type.name.replaceFirstChar { c -> c.uppercase() } }
            val imageUrl = response.sprites.frontDefault
                ?: "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/$pokemonId.png"

            pokemonDao.updateDetails(pokemonId, types, imageUrl)

            val updated = pokemonDao.getPokemonById(pokemonId)
            Result.success(updated?.toDomain() ?: throw Exception("Pokemon not found"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
