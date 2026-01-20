package com.example.poketool.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.poketool.data.model.Team
import com.example.poketool.data.repository.PokemonRepository
import com.example.poketool.data.repository.TeamRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TeamViewModel(
    private val teamRepository: TeamRepository,
    private val pokemonRepository: PokemonRepository
) : ViewModel() {

    val allTeams: StateFlow<List<Team>> = teamRepository.getAllTeams()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _selectedTeamId = MutableStateFlow<Long?>(null)
    val selectedTeamId: StateFlow<Long?> = _selectedTeamId

    val selectedTeam: StateFlow<Team?> = _selectedTeamId
        .flatMapLatest { id ->
            if (id != null) {
                teamRepository.getTeamById(id)
            } else {
                flowOf(null)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    private val _isCreatingTeam = MutableStateFlow(false)
    val isCreatingTeam: StateFlow<Boolean> = _isCreatingTeam

    private val _isEditingName = MutableStateFlow(false)
    val isEditingName: StateFlow<Boolean> = _isEditingName

    fun selectTeam(teamId: Long) {
        _selectedTeamId.value = teamId
        loadTeamPokemonDetails(teamId)
    }

    fun clearSelectedTeam() {
        _selectedTeamId.value = null
    }

    fun showCreateTeamDialog() {
        _isCreatingTeam.value = true
    }

    fun hideCreateTeamDialog() {
        _isCreatingTeam.value = false
    }

    fun showEditNameDialog() {
        _isEditingName.value = true
    }

    fun hideEditNameDialog() {
        _isEditingName.value = false
    }

    fun createTeam(name: String) {
        viewModelScope.launch {
            val teamId = teamRepository.createTeam(name)
            _selectedTeamId.value = teamId
            _isCreatingTeam.value = false
        }
    }

    fun updateTeamName(name: String) {
        val teamId = _selectedTeamId.value ?: return
        viewModelScope.launch {
            teamRepository.updateTeamName(teamId, name)
            _isEditingName.value = false
        }
    }

    fun deleteTeam(teamId: Long) {
        viewModelScope.launch {
            teamRepository.deleteTeam(teamId)
            if (_selectedTeamId.value == teamId) {
                _selectedTeamId.value = null
            }
        }
    }

    fun addPokemonToTeam(pokemonId: Int) {
        val teamId = _selectedTeamId.value ?: return
        viewModelScope.launch {
            val added = teamRepository.addPokemonToTeam(teamId, pokemonId)
            if (added) {
                pokemonRepository.fetchPokemonFullDetails(pokemonId)
            }
        }
    }

    fun removePokemonAtIndex(index: Int) {
        val teamId = _selectedTeamId.value ?: return
        viewModelScope.launch {
            teamRepository.removePokemonAtIndex(teamId, index)
        }
    }

    fun reorderPokemon(fromIndex: Int, toIndex: Int) {
        val teamId = _selectedTeamId.value ?: return
        viewModelScope.launch {
            teamRepository.reorderPokemon(teamId, fromIndex, toIndex)
        }
    }

    private fun loadTeamPokemonDetails(teamId: Long) {
        viewModelScope.launch {
            val teams = allTeams.value
            val team = teams.find { it.id == teamId } ?: return@launch
            team.pokemon.forEach { pokemon ->
                if (!pokemon.hasFullDetails) {
                    pokemonRepository.fetchPokemonFullDetails(pokemon.id)
                }
            }
        }
    }

    class Factory(
        private val teamRepository: TeamRepository,
        private val pokemonRepository: PokemonRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TeamViewModel(teamRepository, pokemonRepository) as T
        }
    }
}
