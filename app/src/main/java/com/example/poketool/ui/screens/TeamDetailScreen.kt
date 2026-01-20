package com.example.poketool.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.poketool.data.model.Pokemon
import com.example.poketool.data.model.Team
import com.example.poketool.data.model.TeamStats
import com.example.poketool.data.model.TypeCoverage
import com.example.poketool.ui.viewmodel.TeamViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamDetailScreen(
    teamId: Long,
    viewModel: TeamViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val selectedTeam by viewModel.selectedTeam.collectAsState()
    val isEditingName by viewModel.isEditingName.collectAsState()

    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(teamId) {
        viewModel.selectTeam(teamId)
    }

    val team = selectedTeam

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(team?.name ?: "Team") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            team?.let { t ->
                                val shareText = formatTeamForSharing(t)
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, shareText)
                                    type = "text/plain"
                                }
                                val shareIntent = Intent.createChooser(sendIntent, "Share Team")
                                context.startActivity(shareIntent)
                            }
                        },
                        enabled = team != null && team.pokemon.isNotEmpty()
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share team")
                    }
                    IconButton(onClick = { viewModel.showEditNameDialog() }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit name")
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete team",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        if (team == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("Loading...")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    TeamSlotsGrid(
                        team = team,
                        onRemoveAtIndex = { index -> viewModel.removePokemonAtIndex(index) }
                    )
                }

                if (team.pokemon.isEmpty()) {
                    item {
                        EmptyTeamMessage()
                    }
                } else {
                    item {
                        TypeCoverageSection(coverage = team.typeCoverage)
                    }

                    if (team.pokemon.any { it.hasFullDetails }) {
                        item {
                            StatsSection(stats = team.totalStats)
                        }
                    }
                }
            }
        }
    }

    if (isEditingName && team != null) {
        EditNameDialog(
            currentName = team.name,
            onDismiss = { viewModel.hideEditNameDialog() },
            onConfirm = { name -> viewModel.updateTeamName(name) }
        )
    }

    if (showDeleteDialog) {
        DeleteTeamDialog(
            teamName = team?.name ?: "",
            onDismiss = { showDeleteDialog = false },
            onConfirm = {
                viewModel.deleteTeam(teamId)
                showDeleteDialog = false
                onNavigateBack()
            }
        )
    }
}

@Composable
private fun TeamSlotsGrid(
    team: Team,
    onRemoveAtIndex: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        for (row in 0..1) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (col in 0..2) {
                    val index = row * 3 + col
                    val pokemon = team.pokemon.getOrNull(index)
                    PokemonSlot(
                        pokemon = pokemon,
                        slotIndex = index,
                        onRemove = if (pokemon != null) {{ onRemoveAtIndex(index) }} else null,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun PokemonSlot(
    pokemon: Pokemon?,
    slotIndex: Int,
    onRemove: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.aspectRatio(0.85f),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (pokemon != null)
                MaterialTheme.colorScheme.surface
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (pokemon != null) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(8.dp)
                ) {
                    Box {
                        AsyncImage(
                            model = pokemon.displayImageUrl,
                            contentDescription = pokemon.name,
                            modifier = Modifier.size(64.dp)
                        )
                        if (onRemove != null) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.error)
                                    .clickable(onClick = onRemove),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remove",
                                    tint = MaterialTheme.colorScheme.onError,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = pokemon.name,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        pokemon.types.take(2).forEach { type ->
                            TypeBadge(type = type)
                        }
                    }
                }
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Empty slot",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                        modifier = Modifier.size(32.dp)
                    )
                    Text(
                        text = "Slot ${slotIndex + 1}",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

@Composable
private fun TypeBadge(type: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(getTypeColor(type))
            .padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        Text(
            text = type.take(3).uppercase(),
            fontSize = 8.sp,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun EmptyTeamMessage() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "No Pokemon in this team",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Go to the Pokedex and tap a Pokemon\nto add it to this team",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun TypeCoverageSection(coverage: TypeCoverage) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Type Coverage",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (coverage.weaknesses.isNotEmpty()) {
                Text(
                    text = "Weaknesses",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(4.dp))
                TypeChipsRow(types = coverage.weaknesses.keys.toList())
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (coverage.strengths.isNotEmpty()) {
                Text(
                    text = "Resistances",
                    fontSize = 12.sp,
                    color = Color(0xFF4CAF50)
                )
                Spacer(modifier = Modifier.height(4.dp))
                TypeChipsRow(types = coverage.strengths.keys.toList())
            }

            if (coverage.immunities.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Immunities",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                TypeChipsRow(types = coverage.immunities.toList())
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TypeChipsRow(types: List<String>) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        types.forEach { type ->
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(getTypeColor(type))
                    .padding(horizontal = 6.dp, vertical = 3.dp)
            ) {
                Text(
                    text = type,
                    fontSize = 10.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun StatsSection(stats: TeamStats) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Average Stats",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(12.dp))

            StatBar("HP", stats.averageHp, Color(0xFFE53935))
            StatBar("Attack", stats.averageAttack, Color(0xFFFFA726))
            StatBar("Defense", stats.averageDefense, Color(0xFFFFEB3B))
            StatBar("Sp. Atk", stats.averageSpAtk, Color(0xFF42A5F5))
            StatBar("Sp. Def", stats.averageSpDef, Color(0xFF66BB6A))
            StatBar("Speed", stats.averageSpeed, Color(0xFFAB47BC))
        }
    }
}

@Composable
private fun StatBar(name: String, value: Int, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = name,
            fontSize = 12.sp,
            modifier = Modifier.width(55.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value.toString(),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.width(30.dp)
        )
        LinearProgressIndicator(
            progress = { (value / 150f).coerceIn(0f, 1f) },
            modifier = Modifier
                .weight(1f)
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
private fun EditNameDialog(
    currentName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var name by remember { mutableStateOf(currentName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Team Name") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Team Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(name.ifBlank { "My Team" }) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun DeleteTeamDialog(
    teamName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Team?") },
        text = { Text("Are you sure you want to delete \"$teamName\"? This cannot be undone.") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Delete", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
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

private fun formatTeamForSharing(team: Team): String {
    val sb = StringBuilder()
    sb.appendLine("My Pokemon Team: ${team.name}")
    sb.appendLine()

    team.pokemon.forEachIndexed { index, pokemon ->
        val types = pokemon.types.joinToString("/") { it.replaceFirstChar { c -> c.uppercase() } }
        sb.appendLine("${index + 1}. ${pokemon.name.replaceFirstChar { it.uppercase() }} ($types)")
    }

    if (team.pokemon.size < Team.MAX_SIZE) {
        sb.appendLine()
        sb.appendLine("(${team.pokemon.size}/${Team.MAX_SIZE} Pokemon)")
    }

    return sb.toString().trim()
}
