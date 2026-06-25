package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.ViewModelProvider
import com.example.data.DeliveryDatabase
import com.example.data.DeliveryRepository
import com.example.ui.DeliveryViewModel
import com.example.ui.DeliveryViewModelFactory
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme

sealed class Screen {
    object Login : Screen()
    object Dashboard : Screen()
    object Parcels : Screen()
    object Route : Screen()
    object Learning : Screen()
    data class ParcelDetail(val parcelId: Int) : Screen()
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize DB and Repository
        val database = DeliveryDatabase.getDatabase(applicationContext)
        val repository = DeliveryRepository(database)
        val factory = DeliveryViewModelFactory(application, repository)
        val viewModel = ViewModelProvider(this, factory)[DeliveryViewModel::class.java]

        setContent {
            MyApplicationTheme {
                val screenHistory = remember { mutableStateListOf<Screen>(Screen.Login) }
                val currentScreen = screenHistory.lastOrNull() ?: Screen.Login

                fun navigateTo(screen: Screen) {
                    if (screen is Screen.Login) {
                        screenHistory.clear()
                    }
                    screenHistory.add(screen)
                }

                fun navigateBack() {
                    if (screenHistory.size > 1) {
                        screenHistory.removeAt(screenHistory.size - 1)
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = com.example.ui.theme.CyberDark, // Matches background
                    bottomBar = {
                        // Display bottom bar only if we are logged in and not on detail screen
                        if (currentScreen !is Screen.Login && currentScreen !is Screen.ParcelDetail) {
                            NavigationBar(
                                modifier = Modifier
                                    .windowInsetsPadding(WindowInsets.navigationBars)
                                    .testTag("app_bottom_nav"),
                                containerColor = com.example.ui.theme.CyberSurface,
                                contentColor = com.example.ui.theme.TextSecondary
                            ) {
                                NavigationBarItem(
                                    selected = currentScreen is Screen.Dashboard,
                                    onClick = {
                                        if (currentScreen !is Screen.Dashboard) {
                                            navigateTo(Screen.Dashboard)
                                        }
                                    },
                                    icon = { Icon(imageVector = Icons.Default.QueryStats, contentDescription = "Dashboard") },
                                    label = { Text("Dashboard") },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = com.example.ui.theme.NeonCyan,
                                        selectedTextColor = com.example.ui.theme.NeonCyan,
                                        indicatorColor = com.example.ui.theme.CyberSurfaceGlass,
                                        unselectedIconColor = com.example.ui.theme.TextMuted,
                                        unselectedTextColor = com.example.ui.theme.TextMuted
                                    ),
                                    modifier = Modifier.testTag("nav_dashboard")
                                )

                                NavigationBarItem(
                                    selected = currentScreen is Screen.Parcels,
                                    onClick = {
                                        if (currentScreen !is Screen.Parcels) {
                                            navigateTo(Screen.Parcels)
                                        }
                                    },
                                    icon = { Icon(imageVector = Icons.Default.Inventory, contentDescription = "Parcels") },
                                    label = { Text("Parcels") },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = com.example.ui.theme.NeonCyan,
                                        selectedTextColor = com.example.ui.theme.NeonCyan,
                                        indicatorColor = com.example.ui.theme.CyberSurfaceGlass,
                                        unselectedIconColor = com.example.ui.theme.TextMuted,
                                        unselectedTextColor = com.example.ui.theme.TextMuted
                                    ),
                                    modifier = Modifier.testTag("nav_parcels")
                                )

                                NavigationBarItem(
                                    selected = currentScreen is Screen.Route,
                                    onClick = {
                                        if (currentScreen !is Screen.Route) {
                                            navigateTo(Screen.Route)
                                        }
                                    },
                                    icon = { Icon(imageVector = Icons.Default.Map, contentDescription = "Route") },
                                    label = { Text("Map Route") },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = com.example.ui.theme.NeonCyan,
                                        selectedTextColor = com.example.ui.theme.NeonCyan,
                                        indicatorColor = com.example.ui.theme.CyberSurfaceGlass,
                                        unselectedIconColor = com.example.ui.theme.TextMuted,
                                        unselectedTextColor = com.example.ui.theme.TextMuted
                                    ),
                                    modifier = Modifier.testTag("nav_route")
                                )

                                NavigationBarItem(
                                    selected = currentScreen is Screen.Learning,
                                    onClick = {
                                        if (currentScreen !is Screen.Learning) {
                                            navigateTo(Screen.Learning)
                                        }
                                    },
                                    icon = { Icon(imageVector = Icons.Default.Hub, contentDescription = "AI Intel") },
                                    label = { Text("AI Intel") },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = com.example.ui.theme.NeonCyan,
                                        selectedTextColor = com.example.ui.theme.NeonCyan,
                                        indicatorColor = com.example.ui.theme.CyberSurfaceGlass,
                                        unselectedIconColor = com.example.ui.theme.TextMuted,
                                        unselectedTextColor = com.example.ui.theme.TextMuted
                                    ),
                                    modifier = Modifier.testTag("nav_learning")
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (currentScreen) {
                            is Screen.Login -> {
                                LoginScreen(
                                    onLoginSuccess = { navigateTo(Screen.Dashboard) }
                                )
                            }
                            is Screen.Dashboard -> {
                                DashboardScreen(
                                    viewModel = viewModel,
                                    onNavigateToParcels = { navigateTo(Screen.Parcels) },
                                    onNavigateToRoute = { navigateTo(Screen.Route) },
                                    onNavigateToLearning = { navigateTo(Screen.Learning) }
                                )
                            }
                            is Screen.Parcels -> {
                                ParcelsScreen(
                                    viewModel = viewModel,
                                    onNavigateToParcelDetail = { id -> navigateTo(Screen.ParcelDetail(id)) }
                                )
                            }
                            is Screen.Route -> {
                                ActiveRouteScreen(
                                    viewModel = viewModel,
                                    onNavigateToParcelDetail = { id -> navigateTo(Screen.ParcelDetail(id)) }
                                )
                            }
                            is Screen.Learning -> {
                                LearningIntelligenceScreen(
                                    viewModel = viewModel
                                )
                            }
                            is Screen.ParcelDetail -> {
                                ParcelDetailScreen(
                                    parcelId = (currentScreen as Screen.ParcelDetail).parcelId,
                                    viewModel = viewModel,
                                    onBack = { navigateBack() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
