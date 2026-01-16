package com.example.poketool.data.remote

import com.example.poketool.data.remote.dto.PokemonDetailResponse
import com.example.poketool.data.remote.dto.PokemonListResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface PokeApiService {

    @GET("pokemon")
    suspend fun getPokemonList(
        @Query("limit") limit: Int = 1500,
        @Query("offset") offset: Int = 0
    ): PokemonListResponse

    @GET("pokemon/{id}")
    suspend fun getPokemonDetail(
        @Path("id") id: Int
    ): PokemonDetailResponse

    companion object {
        const val BASE_URL = "https://pokeapi.co/api/v2/"
    }
}
