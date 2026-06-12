package com.mobile.sap.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mobile.sap.data.model.*
import com.mobile.sap.ui.theme.*
import com.mobile.sap.ui.viewmodel.WeatherViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    weatherViewModel: WeatherViewModel = viewModel()
) {
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showCityDialog by remember { mutableStateOf(false) }
    var showTemperatureDialog by remember { mutableStateOf(false) }
    var showWindSpeedDialog by remember { mutableStateOf(false) }
    var showPrecipitationDialog by remember { mutableStateOf(false) }

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
                        fontWeight = FontWeight.Medium,
                        fontSize = 20.sp,
                        letterSpacing = 0.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = FioriBlue,
                    titleContentColor = FioriWhite
                )
            )
        },
        containerColor = FioriLightGray
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            item {
                SectionHeader("Location")
            }

            item {
                SwitchSettingCard(
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
            }

            if (settings.useAutoLocation) {
                item {
                    LocationRefreshCard(
                        currentLocation = currentLocation,
                        lastUpdateTime = settings.lastUpdateTime,
                        onRefresh = { weatherViewModel.refreshAutoLocation() }
                    )
                }
            } else {
                item {
                    SettingCard(
                        icon = "🏙️",
                        title = "City",
                        subtitle = currentLocation,
                        onClick = { showCityDialog = true }
                    )
                }
            }

            item {
                SectionHeader("Units")
            }

            item {
                SettingCard(
                    icon = "🌡",
                    title = "Temperature",
                    subtitle = settings.temperatureUnit.symbol,
                    onClick = { showTemperatureDialog = true }
                )
            }

            item {
                SettingCard(
                    icon = "💨",
                    title = "Wind Speed",
                    subtitle = settings.windSpeedUnit.symbol,
                    onClick = { showWindSpeedDialog = true }
                )
            }

            item {
                SettingCard(
                    icon = "🌧",
                    title = "Precipitation",
                    subtitle = settings.precipitationUnit.symbol,
                    onClick = { showPrecipitationDialog = true }
                )
            }

            item {
                SectionHeader("Data Options")
            }

            item {
                SwitchSettingCard(
                    icon = "🌱",
                    title = "Soil Data",
                    subtitle = "Show soil temperature & moisture",
                    checked = settings.showSoilData,
                    onCheckedChange = { weatherViewModel.toggleSoilData(it) }
                )
            }

            item {
                SwitchSettingCard(
                    icon = "⏱",
                    title = "Current Weather",
                    subtitle = "Show current conditions",
                    checked = settings.showCurrentWeather,
                    onCheckedChange = { weatherViewModel.toggleCurrentWeather(it) }
                )
            }

            item {
                SectionHeader("App")
            }

            item {
                SettingCard(
                    icon = "🌍",
                    title = "Language",
                    subtitle = selectedLanguage,
                    onClick = { showLanguageDialog = true }
                )
            }

            item {
                InfoCard(
                    icon = "ℹ️",
                    title = "App Version",
                    value = "1.0.0"
                )
            }

            item {
                InfoCard(
                    icon = "📱",
                    title = "Build",
                    value = "2026.06.12"
                )
            }

            item { Spacer(modifier = Modifier.height(4.dp)) }
        }
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
fun LocationRefreshCard(
    currentLocation: String,
    lastUpdateTime: Long?,
    onRefresh: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = FioriWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🔄",
                fontSize = 22.sp,
                modifier = Modifier.padding(end = 16.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = currentLocation,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Normal,
                    color = FioriBlack,
                    fontSize = 15.sp,
                    letterSpacing = 0.sp
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = if (lastUpdateTime != null) {
                        "Updated: ${formatLastUpdate(lastUpdateTime)}"
                    } else {
                        "Not yet updated"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = FioriDarkGray.copy(alpha = 0.7f),
                    fontSize = 13.sp
                )
            }
            Button(
                onClick = onRefresh,
                colors = ButtonDefaults.buttonColors(containerColor = FioriBlue),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Refresh",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
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
fun SectionHeader(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Medium,
        color = FioriDarkGray.copy(alpha = 0.7f),
        fontSize = 11.sp,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(start = 2.dp, top = 8.dp, bottom = 8.dp)
    )
}

@Composable
fun SettingCard(
    icon: String,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = FioriWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = icon,
                fontSize = 22.sp,
                modifier = Modifier.padding(end = 16.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Normal,
                    color = FioriBlack,
                    fontSize = 15.sp,
                    letterSpacing = 0.sp
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = FioriDarkGray.copy(alpha = 0.7f),
                    fontSize = 13.sp
                )
            }
            Text(
                text = "›",
                fontSize = 20.sp,
                color = FioriDarkGray.copy(alpha = 0.3f)
            )
        }
    }
}

@Composable
fun SwitchSettingCard(
    icon: String,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = FioriWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = icon,
                fontSize = 22.sp,
                modifier = Modifier.padding(end = 16.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Normal,
                    color = FioriBlack,
                    fontSize = 15.sp,
                    letterSpacing = 0.sp
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = FioriDarkGray.copy(alpha = 0.7f),
                    fontSize = 13.sp
                )
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = FioriWhite,
                    checkedTrackColor = FioriBlue,
                    uncheckedThumbColor = FioriWhite,
                    uncheckedTrackColor = FioriGray
                )
            )
        }
    }
}

@Composable
fun InfoCard(
    icon: String,
    title: String,
    value: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = FioriWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = icon,
                fontSize = 22.sp,
                modifier = Modifier.padding(end = 16.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Normal,
                    color = FioriBlack,
                    fontSize = 15.sp,
                    letterSpacing = 0.sp
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = FioriDarkGray.copy(alpha = 0.6f),
                fontWeight = FontWeight.Normal,
                fontSize = 13.sp
            )
        }
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
                fontWeight = FontWeight.SemiBold,
                color = FioriBlack,
                fontSize = 18.sp
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
                                selectedColor = FioriBlue
                            )
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = option,
                            style = MaterialTheme.typography.bodyLarge,
                            color = FioriBlack,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    "Close",
                    color = FioriBlue,
                    fontWeight = FontWeight.Medium
                )
            }
        },
        containerColor = FioriWhite,
        shape = RoundedCornerShape(16.dp)
    )
}
