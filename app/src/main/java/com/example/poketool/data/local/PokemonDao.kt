package com.example.poketool.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PokemonDao {

    @Query("SELECT * FROM pokemon ORDER BY id ASC")
    fun getAllPokemon(): Flow<List<PokemonEntity>>

    @Query("SELECT * FROM pokemon WHERE id = :id")
    suspend fun getPokemonById(id: Int): PokemonEntity?

    @Query("SELECT COUNT(*) FROM pokemon")
    suspend fun getPokemonCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(pokemon: List<PokemonEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(pokemon: PokemonEntity)

    @Update
    suspend fun update(pokemon: PokemonEntity)

    @Query("UPDATE pokemon SET types = :types, imageUrl = :imageUrl WHERE id = :id")
    suspend fun updateDetails(id: Int, types: String, imageUrl: String)

    @Query("DELETE FROM pokemon")
    suspend fun deleteAll()
}
