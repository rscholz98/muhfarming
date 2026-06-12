package com.mobile.sap.ui.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mobile.sap.ui.screens.PestManagementScreen
import com.mobile.sap.ui.screens.SettingsScreen
import com.mobile.sap.ui.screens.WeatherScreen
import com.mobile.sap.ui.theme.*
import com.mobile.sap.ui.viewmodel.WeatherViewModel

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val weatherViewModel: WeatherViewModel = viewModel(
        factory = androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.getInstance(
            context.applicationContext as android.app.Application
        )
    )

    val screens = listOf(
        Screen.Weather,
        Screen.PestManagement,
        Screen.Settings
    )

    Scaffold(
        containerColor = FioriLightGray,
        contentWindowInsets = WindowInsets(0.dp),
        bottomBar = {
            NavigationBar(
                containerColor = FioriWhite,
                tonalElevation = NavigationBarDefaults.Elevation
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                screens.forEach { screen ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = screen.title
                            )
                        },
                        label = {
                            Text(
                                text = screen.title,
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp
                            )
                        },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = FioriBlue,
                            selectedTextColor = FioriBlue,
                            unselectedIconColor = FioriDarkGray,
                            unselectedTextColor = FioriDarkGray,
                            indicatorColor = FioriBlue.copy(alpha = 0.1f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Weather.route,
            modifier = Modifier.padding(innerPadding),
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None }
        ) {
            composable(Screen.Weather.route) {
                WeatherScreen(viewModel = weatherViewModel)
            }
            composable(Screen.PestManagement.route) {
                PestManagementScreen()
            }
            composable(Screen.Settings.route) {
                SettingsScreen(weatherViewModel = weatherViewModel)
            }
        }
    }
}
