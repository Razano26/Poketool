package com.example.poketool.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    data object Pokedex : BottomNavItem(
        route = "pokedex",
        title = "Pokédex",
        icon = Icons.Default.Home
    )

    data object Team : BottomNavItem(
        route = "team",
        title = "Team",
        icon = Icons.Default.Favorite
    )

    data object Collection : BottomNavItem(
        route = "collection",
        title = "Collection",
        icon = Icons.Default.CheckCircle
    )
}
