package com.mobile.sap.ui.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.mobile.sap.ui.screens.AlertsScreen
import com.mobile.sap.ui.screens.CultivationGuideScreen
import com.mobile.sap.ui.screens.FarmsScreen
import com.mobile.sap.ui.screens.FieldsScreen
import com.mobile.sap.ui.screens.LoginScreen
import com.mobile.sap.ui.screens.SettingsScreen
import com.mobile.sap.ui.screens.WeatherScreen
import com.mobile.sap.data.auth.SessionManager
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

    val session = remember { SessionManager.get(context.applicationContext) }

    // Restore any persisted session so a returning user skips the login screen.
    var isLoggedIn by remember { mutableStateOf(session.isLoggedIn) }
    var isAdmin by remember { mutableStateOf(session.isAdmin) }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Clears the session and returns to the login screen. Used by the manual
    // Settings logout and by the forced logout when the backend rejects the token.
    val performLogout: () -> Unit = {
        session.clear()
        isLoggedIn = false
        isAdmin = false
        navController.navigate(Screen.Login.route) {
            popUpTo(navController.graph.findStartDestination().id) {
                inclusive = true
            }
            launchSingleTop = true
        }
    }

    // Force a logout when the network layer reports an unauthorized (401)
    // response, e.g. an expired or revoked token. Only act while logged in so a
    // 401 already on the login screen doesn't loop.
    LaunchedEffect(Unit) {
        com.mobile.sap.data.event.AuthEvents.unauthorized.collect {
            if (isLoggedIn) performLogout()
        }
    }

    // Only show bottom bar if logged in and not on login screen
    val showBottomBar = isLoggedIn && currentRoute != Screen.Login.route

    val screens = listOf(
        Screen.Weather,
        Screen.Fields,
        Screen.CultivationGuide,
        Screen.Alerts,
        Screen.Settings
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0.dp),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 3.dp
                ) {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentDestination = navBackStackEntry?.destination

                    screens.forEach { screen ->
                        val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
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
                                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                                    fontSize = 12.sp
                                )
                            },
                            selected = selected,
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
                                selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = if (isLoggedIn) Screen.Weather.route else Screen.Login.route,
            modifier = Modifier.padding(innerPadding),
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None }
        ) {
            composable(Screen.Login.route) {
                LoginScreen(
                    onLoginSuccess = { role ->
                        isLoggedIn = true
                        isAdmin = role.equals("Admin", ignoreCase = true)
                        navController.navigate(Screen.Weather.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.Weather.route) {
                WeatherScreen(viewModel = weatherViewModel)
            }
            composable(Screen.Fields.route) {
                FieldsScreen(
                    weatherViewModel = weatherViewModel,
                    isAdmin = isAdmin,
                    onOpenFarms = { navController.navigate(Screen.Farms.route) }
                )
            }
            composable(Screen.Farms.route) {
                FarmsScreen(onBack = { navController.popBackStack() })
            }
            composable(Screen.CultivationGuide.route) {
                CultivationGuideScreen(isAdmin = isAdmin)
            }
            composable(Screen.Alerts.route) {
                AlertsScreen(isAdmin = isAdmin)
            }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    weatherViewModel = weatherViewModel,
                    onLogout = performLogout
                )
            }
        }
    }
}
