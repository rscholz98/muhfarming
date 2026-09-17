package com.mobile.sap.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Agriculture
import androidx.compose.material.icons.outlined.Login
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Login : Screen("login", "Login", Icons.Outlined.Login)
    object Weather : Screen("weather", "Weather", Icons.Outlined.WbSunny)
    object Fields : Screen("fields", "Fields", Icons.Outlined.Map)
    object Farms : Screen("farms", "Farms", Icons.Outlined.Agriculture)
    object CultivationGuide : Screen("guide", "Guide", Icons.Outlined.MenuBook)
    object Alerts : Screen("alerts", "Alerts", Icons.Outlined.Notifications)
    object Settings : Screen("settings", "Settings", Icons.Outlined.Settings)
}
