package com.example.poketool.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.poketool.data.model.Pokemon
import com.example.poketool.data.model.PokemonStatInfo
import com.example.poketool.data.model.Team

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PokemonDetailBottomSheet(
    pokemon: Pokemon,
    isLoading: Boolean,
    sheetState: SheetState,
    teams: List<Team> = emptyList(),
    selectedTeamId: Long? = null,
    onAddToTeam: (Long) -> Unit = {},
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AsyncImage(
                model = pokemon.displayImageUrl,
                contentDescription = pokemon.name,
                modifier = Modifier.size(120.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = pokemon.formattedId,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = pokemon.name,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                pokemon.types.forEach { type ->
                    TypeChip(type = type)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(32.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Loading details...",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else if (pokemon.hasFullDetails) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    InfoColumn(label = "Height", value = pokemon.heightInMeters)
                    InfoColumn(label = "Weight", value = pokemon.weightInKg)
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Base Stats",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                pokemon.stats.forEach { stat ->
                    StatRow(stat = stat)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Abilities",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    pokemon.abilities.forEach { ability ->
                        AbilityChip(ability = ability)
                    }
                }
            }

            if (teams.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))

                AddToTeamSection(
                    pokemon = pokemon,
                    teams = teams,
                    selectedTeamId = selectedTeamId,
                    onAddToTeam = onAddToTeam
                )
            }
        }
    }
}

@Composable
private fun AddToTeamSection(
    pokemon: Pokemon,
    teams: List<Team>,
    selectedTeamId: Long?,
    onAddToTeam: (Long) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedTeam = teams.find { it.id == selectedTeamId }
    val isInSelectedTeam = selectedTeam?.contains(pokemon.id) == true
    val isTeamFull = selectedTeam?.isFull == true

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Add to Team",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.weight(1f)) {
                OutlinedButton(
                    onClick = { expanded = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = selectedTeam?.name ?: "Select a team",
                        maxLines = 1
                    )
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    teams.forEach { team ->
                        val alreadyInTeam = team.contains(pokemon.id)
                        DropdownMenuItem(
                            text = {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(team.name)
                                    Text(
                                        text = "(${team.size}/6)",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    if (alreadyInTeam) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Already in team",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            },
                            onClick = {
                                expanded = false
                                if (!alreadyInTeam && !team.isFull) {
                                    onAddToTeam(team.id)
                                }
                            },
                            enabled = !alreadyInTeam && !team.isFull
                        )
                    }
                }
            }

            Button(
                onClick = {
                    selectedTeamId?.let { onAddToTeam(it) }
                },
                enabled = selectedTeamId != null && !isInSelectedTeam && !isTeamFull,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isInSelectedTeam)
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    else
                        MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = if (isInSelectedTeam) Icons.Default.Check else Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = when {
                        isInSelectedTeam -> "Added"
                        isTeamFull -> "Full"
                        else -> "Add"
                    }
                )
            }
        }

        if (selectedTeamId != null && isTeamFull && !isInSelectedTeam) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "This team is full (6/6)",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun TypeChip(type: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(getTypeColor(type))
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Text(
            text = type,
            fontSize = 14.sp,
            color = Color.White,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun AbilityChip(ability: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = ability,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun InfoColumn(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun StatRow(stat: PokemonStatInfo) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stat.displayName,
            fontSize = 14.sp,
            modifier = Modifier.width(80.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = stat.baseStat.toString(),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.width(40.dp)
        )

        LinearProgressIndicator(
            progress = { (stat.baseStat / 255f).coerceIn(0f, 1f) },
            modifier = Modifier
                .weight(1f)
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = getStatColor(stat.baseStat),
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

private fun getStatColor(value: Int): Color {
    return when {
        value < 50 -> Color(0xFFE53935)
        value < 80 -> Color(0xFFFFA726)
        value < 100 -> Color(0xFFFFEB3B)
        value < 130 -> Color(0xFF66BB6A)
        else -> Color(0xFF42A5F5)
    }
}

private fun getTypeColor(type: String): Color {
    return when (type.lowercase()) {
        "grass" -> Color(0xFF78C850)
        "poison" -> Color(0xFFA040A0)
        "fire" -> Color(0xFFF08030)
        "water" -> Color(0xFF6890F0)
        "electric" -> Color(0xFFF8D030)
        "psychic" -> Color(0xFFF85888)
        "normal" -> Color(0xFFA8A878)
        "fairy" -> Color(0xFFEE99AC)
        "fighting" -> Color(0xFFC03028)
        "flying" -> Color(0xFFA890F0)
        "bug" -> Color(0xFFA8B820)
        "rock" -> Color(0xFFB8A038)
        "ground" -> Color(0xFFE0C068)
        "ghost" -> Color(0xFF705898)
        "dark" -> Color(0xFF705848)
        "steel" -> Color(0xFFB8B8D0)
        "ice" -> Color(0xFF98D8D8)
        "dragon" -> Color(0xFF7038F8)
        else -> Color(0xFF68A090)
    }
}
