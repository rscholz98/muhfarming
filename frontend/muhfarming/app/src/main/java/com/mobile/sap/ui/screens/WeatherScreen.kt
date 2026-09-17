package com.mobile.sap.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mobile.sap.data.model.CurrentWeatherInfo
import com.mobile.sap.data.model.DailyForecast
import com.mobile.sap.data.model.HourlyForecast
import com.mobile.sap.ui.components.BrandHeader
import com.mobile.sap.ui.components.PrimaryButton
import com.mobile.sap.ui.components.SectionCard
import com.mobile.sap.ui.components.SectionLabel
import com.mobile.sap.ui.theme.*
import com.mobile.sap.ui.viewmodel.WeatherUiState
import com.mobile.sap.ui.viewmodel.WeatherViewModel
import com.mobile.sap.util.WeatherCodeMapper
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreen(
    viewModel: WeatherViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val location by viewModel.location.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val currentDayName by viewModel.currentDayName.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()

    Scaffold(
        contentWindowInsets = WindowInsets(0.dp),
        topBar = {
            BrandHeader(
                title = currentDayName,
                subtitle = location
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        when (val state = uiState) {
            is WeatherUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
            is WeatherUiState.Success -> {
                WeatherContent(
                    location = location,
                    currentWeather = state.forecast.currentWeather,
                    hourlyForecasts = state.forecast.hourlyForecasts,
                    dailyForecasts = state.forecast.dailyForecasts,
                    showSoilData = settings.showSoilData,
                    selectedDate = selectedDate,
                    onDaySelected = { date, dayName ->
                        viewModel.selectDay(date, dayName)
                    },
                    onTodaySelected = { viewModel.selectToday() },
                    modifier = Modifier.padding(paddingValues)
                )
            }
            is WeatherUiState.Error -> {
                ErrorState(
                    message = state.message,
                    onRetry = { viewModel.loadWeatherForecast() },
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

@Composable
fun WeatherContent(
    location: String,
    currentWeather: CurrentWeatherInfo?,
    hourlyForecasts: List<HourlyForecast>,
    dailyForecasts: List<DailyForecast>,
    showSoilData: Boolean,
    selectedDate: String?,
    onDaySelected: (String, String) -> Unit,
    onTodaySelected: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Filter hourly forecasts based on selected date
    val filteredHourlyForecasts = if (selectedDate != null) {
        hourlyForecasts.filter { it.date == selectedDate }
    } else {
        // For today, show next 24 hours from current time
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        hourlyForecasts.filter { it.date == today }.take(24)
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        item { Spacer(modifier = Modifier.height(16.dp)) }

        // Day Selection Strip
        item {
            DaySelectionStrip(
                dailyForecasts = dailyForecasts,
                selectedDate = selectedDate,
                onDaySelected = onDaySelected,
                onTodaySelected = onTodaySelected
            )
        }

        item { Spacer(modifier = Modifier.height(20.dp)) }

        // Current Weather Inline
        if (currentWeather != null && selectedDate == null) {
            item {
                CurrentWeatherInline(
                    current = currentWeather,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            item { Spacer(modifier = Modifier.height(20.dp)) }
        }

        // Selected Day Details (when a day is selected)
        if (selectedDate != null) {
            val selectedDayForecast = dailyForecasts.find { it.date == selectedDate }
            if (selectedDayForecast != null) {
                item {
                    SelectedDayCard(
                        forecast = selectedDayForecast,
                        showSoilData = showSoilData,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
                item { Spacer(modifier = Modifier.height(20.dp)) }
            }
        }

        // Hourly Forecast Section
        if (filteredHourlyForecasts.isNotEmpty()) {
            item {
                SectionLabel(
                    text = "Hourly",
                    modifier = Modifier.padding(start = 20.dp, bottom = 10.dp)
                )
            }

            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    items(filteredHourlyForecasts) { forecast ->
                        HourlyForecastCard(forecast, showSoilData)
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(20.dp)) }
        }
    }
}

@Composable
fun DaySelectionStrip(
    dailyForecasts: List<DailyForecast>,
    selectedDate: String?,
    onDaySelected: (String, String) -> Unit,
    onTodaySelected: () -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        modifier = Modifier.height(104.dp)
    ) {
        itemsIndexed(dailyForecasts) { index, forecast ->
            DayCard(
                date = forecast.date,
                dayName = if (index == 0) "Today" else forecast.day.take(3),
                weatherIcon = WeatherCodeMapper.getWeatherIcon(forecast.weatherCode),
                highTemp = forecast.highTemp,
                isSelected = if (index == 0) selectedDate == null else selectedDate == forecast.date,
                onClick = {
                    if (index == 0) {
                        onTodaySelected()
                    } else {
                        onDaySelected(forecast.date, forecast.day)
                    }
                }
            )
        }
    }
}

@Composable
fun DayCard(
    date: String,
    dayName: String,
    weatherIcon: String,
    highTemp: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val displayFormat = SimpleDateFormat("d", Locale.getDefault())
    val dayOfMonth = try {
        displayFormat.format(dateFormat.parse(date) ?: Date())
    } catch (e: Exception) {
        ""
    }

    val containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
    val contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
    val mutedColor = if (isSelected) Color.White.copy(alpha = 0.75f) else MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        modifier = Modifier
            .width(70.dp)
            .height(104.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = containerColor,
        border = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = dayName,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = contentColor
            )

            Text(
                text = weatherIcon,
                fontSize = 28.sp,
                lineHeight = 28.sp
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = highTemp,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = contentColor
                )
                if (dayOfMonth.isNotEmpty()) {
                    Text(
                        text = dayOfMonth,
                        style = MaterialTheme.typography.labelSmall,
                        color = mutedColor
                    )
                }
            }
        }
    }
}

@Composable
fun CurrentWeatherInline(
    current: CurrentWeatherInfo,
    modifier: Modifier = Modifier
) {
    SectionCard(
        modifier = modifier,
        contentPadding = PaddingValues(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Temperature and condition
            Column {
                Text(
                    text = current.temperature,
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Light,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 56.sp,
                    letterSpacing = (-2).sp
                )
                Text(
                    text = current.condition,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Normal
                )
            }

            // Right: Weather details
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                WeatherDetailRow("💧", "${current.humidity}%")
                WeatherDetailRow("🌧", current.precipitation)
                WeatherDetailRow("☔", current.rain)
            }
        }
    }
}

@Composable
fun WeatherDetailRow(icon: String, value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = icon, fontSize = 16.sp)
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun SelectedDayCard(
    forecast: DailyForecast,
    showSoilData: Boolean,
    modifier: Modifier = Modifier
) {
    SectionCard(
        modifier = modifier,
        contentPadding = PaddingValues(14.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = WeatherCodeMapper.getWeatherIcon(forecast.weatherCode),
                fontSize = 64.sp,
                lineHeight = 64.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = forecast.condition,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = forecast.highTemp,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "High",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = forecast.lowTemp,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Low",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (forecast.precipitation > 0) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${forecast.precipitation}%",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Info
                        )
                        Text(
                            text = "Rain",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (showSoilData && forecast.soilTemperature != null) {
                Spacer(modifier = Modifier.height(20.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = forecast.soilTemperature,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Soil Temp",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (forecast.soilMoisture != null) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = forecast.soilMoisture,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Soil Moisture",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HourlyForecastCard(forecast: HourlyForecast, showSoilData: Boolean) {
    Surface(
        modifier = Modifier
            .width(100.dp)
            .height(144.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = forecast.time,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = WeatherCodeMapper.getWeatherIcon(forecast.weatherCode),
                fontSize = 32.sp,
                lineHeight = 32.sp
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = forecast.temperature,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (forecast.precipitationProbability != null && forecast.precipitationProbability > 0) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Info.copy(alpha = 0.10f)
                    ) {
                        Text(
                            text = "${forecast.precipitationProbability}%",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = Info,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                if (showSoilData && forecast.soilTemperature != null) {
                    Text(
                        text = forecast.soilTemperature,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}


@Composable
fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = "⚠️",
                fontSize = 36.sp
            )
            Text(
                text = "Unable to Load Weather",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            PrimaryButton(
                text = "Retry",
                onClick = onRetry,
                modifier = Modifier.widthIn(max = 200.dp)
            )
        }
    }
}
