package com.example.poketool.data.repository

import com.example.poketool.data.local.PokemonDao
import com.example.poketool.data.local.TeamDao
import com.example.poketool.data.local.TeamEntity
import com.example.poketool.data.model.Team
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TeamRepository(
    private val teamDao: TeamDao,
    private val pokemonDao: PokemonDao
) {

    fun getAllTeams(): Flow<List<Team>> {
        return teamDao.getAllTeams().map { teams ->
            teams.map { entity -> entityToTeam(entity) }
        }
    }

    fun getTeamById(id: Long): Flow<Team?> {
        return teamDao.getTeamByIdFlow(id).map { entity ->
            entity?.let { entityToTeam(it) }
        }
    }

    suspend fun createTeam(name: String): Long {
        val entity = TeamEntity(name = name)
        return teamDao.insert(entity)
    }

    suspend fun updateTeamName(teamId: Long, name: String) {
        val team = teamDao.getTeamById(teamId) ?: return
        teamDao.update(team.copy(name = name))
    }

    suspend fun deleteTeam(teamId: Long) {
        teamDao.deleteById(teamId)
    }

    suspend fun addPokemonToTeam(teamId: Long, pokemonId: Int): Boolean {
        val team = teamDao.getTeamById(teamId) ?: return false
        val updatedTeam = team.withPokemon(pokemonId)
        if (updatedTeam == team) return false
        teamDao.update(updatedTeam)
        return true
    }

    suspend fun removePokemonAtIndex(teamId: Long, index: Int) {
        val team = teamDao.getTeamById(teamId) ?: return
        val updatedTeam = team.withoutPokemonAtIndex(index)
        teamDao.update(updatedTeam)
    }

    suspend fun reorderPokemon(teamId: Long, fromIndex: Int, toIndex: Int) {
        val team = teamDao.getTeamById(teamId) ?: return
        val updatedTeam = team.withReorderedPokemon(fromIndex, toIndex)
        teamDao.update(updatedTeam)
    }

    suspend fun getTeamCount(): Int {
        return teamDao.getTeamCount()
    }

    private suspend fun entityToTeam(entity: TeamEntity): Team {
        val pokemonIds = entity.getPokemonIdList()
        val pokemonList = pokemonIds.mapNotNull { id ->
            pokemonDao.getPokemonById(id)?.toDomain()
        }
        return Team(
            id = entity.id,
            name = entity.name,
            pokemon = pokemonList
        )
    }
}
