package com.mobile.sap.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mobile.sap.data.model.*
import com.mobile.sap.ui.components.*
import com.mobile.sap.ui.theme.*
import com.mobile.sap.ui.viewmodel.WeatherViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    weatherViewModel: WeatherViewModel = viewModel(),
    onLogout: () -> Unit = {}
) {
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showCityDialog by remember { mutableStateOf(false) }
    var showTemperatureDialog by remember { mutableStateOf(false) }
    var showWindSpeedDialog by remember { mutableStateOf(false) }
    var showPrecipitationDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    var selectedLanguage by remember { mutableStateOf("English") }
    val currentLocation by weatherViewModel.location.collectAsState()
    val settings by weatherViewModel.settings.collectAsState()
    val locationPermissionGranted by weatherViewModel.locationPermissionGranted.collectAsState()

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.values.any { it }
        weatherViewModel.updateLocationPermission(granted)
        if (granted && settings.useAutoLocation) {
            weatherViewModel.refreshAutoLocation()
        }
    }

    fun requestLocationPermission() {
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        )
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0.dp),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            item {
                SectionLabel("Location")
                Spacer(modifier = Modifier.height(8.dp))
                SectionCard(contentPadding = PaddingValues(0.dp)) {
                    SwitchSettingRow(
                        icon = "📍",
                        title = "Auto Geolocation",
                        subtitle = if (settings.useAutoLocation) "Using current location" else "Manual city selection",
                        checked = settings.useAutoLocation,
                        onCheckedChange = { enabled ->
                            if (enabled && !locationPermissionGranted) {
                                requestLocationPermission()
                            } else {
                                weatherViewModel.updateLocationMode(enabled)
                            }
                        }
                    )
                    if (settings.useAutoLocation) {
                        RowDivider()
                        LocationRefreshRow(
                            currentLocation = currentLocation,
                            lastUpdateTime = settings.lastUpdateTime,
                            onRefresh = { weatherViewModel.refreshAutoLocation() }
                        )
                    } else {
                        RowDivider()
                        SettingRow(
                            icon = "🏙️",
                            title = "City",
                            subtitle = currentLocation,
                            onClick = { showCityDialog = true }
                        )
                    }
                }
            }

            item {
                SectionLabel("Units")
                Spacer(modifier = Modifier.height(8.dp))
                SectionCard(contentPadding = PaddingValues(0.dp)) {
                    SettingRow(
                        icon = "🌡",
                        title = "Temperature",
                        subtitle = settings.temperatureUnit.symbol,
                        onClick = { showTemperatureDialog = true }
                    )
                    RowDivider()
                    SettingRow(
                        icon = "💨",
                        title = "Wind Speed",
                        subtitle = settings.windSpeedUnit.symbol,
                        onClick = { showWindSpeedDialog = true }
                    )
                    RowDivider()
                    SettingRow(
                        icon = "🌧",
                        title = "Precipitation",
                        subtitle = settings.precipitationUnit.symbol,
                        onClick = { showPrecipitationDialog = true }
                    )
                }
            }

            item {
                SectionLabel("Data Options")
                Spacer(modifier = Modifier.height(8.dp))
                SectionCard(contentPadding = PaddingValues(0.dp)) {
                    SwitchSettingRow(
                        icon = "🌱",
                        title = "Soil Data",
                        subtitle = "Show soil temperature & moisture",
                        checked = settings.showSoilData,
                        onCheckedChange = { weatherViewModel.toggleSoilData(it) }
                    )
                    RowDivider()
                    SwitchSettingRow(
                        icon = "⏱",
                        title = "Current Weather",
                        subtitle = "Show current conditions",
                        checked = settings.showCurrentWeather,
                        onCheckedChange = { weatherViewModel.toggleCurrentWeather(it) }
                    )
                }
            }

            item {
                SectionLabel("About")
                Spacer(modifier = Modifier.height(8.dp))
                SectionCard(contentPadding = PaddingValues(0.dp)) {
                    SettingRow(
                        icon = "🌍",
                        title = "Language",
                        subtitle = selectedLanguage,
                        onClick = { showLanguageDialog = true }
                    )
                    RowDivider()
                    InfoRow(
                        icon = "ℹ️",
                        title = "App Version",
                        value = "1.0.0"
                    )
                    RowDivider()
                    InfoRow(
                        icon = "📱",
                        title = "Build",
                        value = "2026.06.12"
                    )
                }
            }

            item {
                SectionLabel("Account")
                Spacer(modifier = Modifier.height(8.dp))
                SectionCard(contentPadding = PaddingValues(0.dp)) {
                    LogoutRow(onClick = { showLogoutDialog = true })
                }
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = {
                Text(
                    text = "Log Out",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to log out?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    onLogout()
                }) {
                    Text(
                        "Log Out",
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text(
                        "Cancel",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = MaterialTheme.shapes.large
        )
    }

    if (showLanguageDialog) {
        OptionDialog(
            title = "Select Language",
            options = listOf("English", "French", "Arabic", "Spanish"),
            currentSelection = selectedLanguage,
            onSelect = {
                selectedLanguage = it
                showLanguageDialog = false
            },
            onDismiss = { showLanguageDialog = false }
        )
    }

    if (showCityDialog) {
        OptionDialog(
            title = "Select City",
            options = CameroonCities.cities.map { it.name },
            currentSelection = currentLocation,
            onSelect = { selectedCity ->
                weatherViewModel.updateManualCity(selectedCity)
                showCityDialog = false
            },
            onDismiss = { showCityDialog = false }
        )
    }

    if (showTemperatureDialog) {
        OptionDialog(
            title = "Temperature Unit",
            options = TemperatureUnit.values().map { it.symbol },
            currentSelection = settings.temperatureUnit.symbol,
            onSelect = { selected ->
                TemperatureUnit.values().find { it.symbol == selected }?.let {
                    weatherViewModel.updateTemperatureUnit(it)
                }
                showTemperatureDialog = false
            },
            onDismiss = { showTemperatureDialog = false }
        )
    }

    if (showWindSpeedDialog) {
        OptionDialog(
            title = "Wind Speed Unit",
            options = WindSpeedUnit.values().map { it.symbol },
            currentSelection = settings.windSpeedUnit.symbol,
            onSelect = { selected ->
                WindSpeedUnit.values().find { it.symbol == selected }?.let {
                    weatherViewModel.updateWindSpeedUnit(it)
                }
                showWindSpeedDialog = false
            },
            onDismiss = { showWindSpeedDialog = false }
        )
    }

    if (showPrecipitationDialog) {
        OptionDialog(
            title = "Precipitation Unit",
            options = PrecipitationUnit.values().map { it.symbol },
            currentSelection = settings.precipitationUnit.symbol,
            onSelect = { selected ->
                PrecipitationUnit.values().find { it.symbol == selected }?.let {
                    weatherViewModel.updatePrecipitationUnit(it)
                }
                showPrecipitationDialog = false
            },
            onDismiss = { showPrecipitationDialog = false }
        )
    }
}

@Composable
private fun RowDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = 46.dp),
        color = MaterialTheme.colorScheme.outlineVariant
    )
}

@Composable
private fun RowIcon(icon: String) {
    Text(
        text = icon,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(end = 14.dp)
    )
}

@Composable
fun LocationRefreshRow(
    currentLocation: String,
    lastUpdateTime: Long?,
    onRefresh: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RowIcon("🔄")
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = currentLocation,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = if (lastUpdateTime != null) {
                    "Updated: ${formatLastUpdate(lastUpdateTime)}"
                } else {
                    "Not yet updated"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Button(
            onClick = onRefresh,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = MaterialTheme.shapes.small,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = "Refresh",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

private fun formatLastUpdate(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    return when {
        diff < 60_000 -> "Just now"
        diff < 3600_000 -> "${diff / 60_000}m ago"
        diff < 86400_000 -> "${diff / 3600_000}h ago"
        else -> {
            val dateFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
            dateFormat.format(Date(timestamp))
        }
    }
}

@Composable
fun SettingRow(
    icon: String,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .heightIn(min = 52.dp)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RowIcon(icon)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = "›",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
    }
}

@Composable
fun SwitchSettingRow(
    icon: String,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RowIcon(icon)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = MaterialTheme.colorScheme.surface,
                uncheckedTrackColor = MaterialTheme.colorScheme.outline
            )
        )
    }
}

@Composable
fun InfoRow(
    icon: String,
    title: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RowIcon(icon)
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun LogoutRow(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .heightIn(min = 52.dp)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "🚪",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(end = 14.dp)
        )
        Text(
            text = "Log Out",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun OptionDialog(
    title: String,
    options: List<String>,
    currentSelection: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column {
                options.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(option) }
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = option == currentSelection,
                            onClick = { onSelect(option) },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = MaterialTheme.colorScheme.primary
                            )
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = option,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    "Close",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.large
    )
}
