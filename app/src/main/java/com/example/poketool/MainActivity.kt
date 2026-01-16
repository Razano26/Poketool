package com.example.poketool

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.poketool.data.local.PokemonDatabase
import com.example.poketool.data.remote.RetrofitClient
import com.example.poketool.data.repository.PokemonRepository
import com.example.poketool.navigation.BottomNavItem
import com.example.poketool.ui.screens.CollectionScreen
import com.example.poketool.ui.screens.PokedexScreen
import com.example.poketool.ui.screens.SyncScreen
import com.example.poketool.ui.screens.TeamScreen
import com.example.poketool.ui.theme.PoketoolTheme
import com.example.poketool.ui.viewmodel.PokedexViewModel
import com.example.poketool.ui.viewmodel.SyncViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PoketoolTheme {
                PoketoolApp()
            }
        }
    }
}

@Composable
fun PoketoolApp() {
    val context = LocalContext.current
    val database = PokemonDatabase.getDatabase(context)
    val repository = PokemonRepository(
        pokemonDao = database.pokemonDao(),
        pokeApiService = RetrofitClient.pokeApiService
    )

    val syncViewModel: SyncViewModel = viewModel(
        factory = SyncViewModel.Factory(repository)
    )
    val pokedexViewModel: PokedexViewModel = viewModel(
        factory = PokedexViewModel.Factory(repository)
    )

    val needsSync by syncViewModel.needsSync.collectAsState()
    val navController = rememberNavController()

    val startDestination = if (needsSync) "sync" else BottomNavItem.Pokedex.route

    LaunchedEffect(needsSync) {
        if (!needsSync) {
            val currentRoute = navController.currentDestination?.route
            if (currentRoute == "sync") {
                navController.navigate(BottomNavItem.Pokedex.route) {
                    popUpTo("sync") { inclusive = true }
                }
            }
        }
    }

    val items = listOf(
        BottomNavItem.Pokedex,
        BottomNavItem.Team,
        BottomNavItem.Collection
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute != "sync"

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    val currentDestination = navBackStackEntry?.destination

                    items.forEach { item ->
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.title) },
                            label = { Text(item.title) },
                            selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("sync") {
                SyncScreen(
                    viewModel = syncViewModel,
                    onSyncComplete = {
                        navController.navigate(BottomNavItem.Pokedex.route) {
                            popUpTo("sync") { inclusive = true }
                        }
                    }
                )
            }
            composable(BottomNavItem.Pokedex.route) {
                PokedexScreen(viewModel = pokedexViewModel)
            }
            composable(BottomNavItem.Team.route) { TeamScreen() }
            composable(BottomNavItem.Collection.route) { CollectionScreen() }
        }
    }
}
