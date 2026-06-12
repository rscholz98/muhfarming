package com.mobile.sap.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mobile.sap.data.model.CurrentWeatherInfo
import com.mobile.sap.data.model.DailyForecast
import com.mobile.sap.data.model.HourlyForecast
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
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = currentDayName,
                            fontWeight = FontWeight.Medium,
                            fontSize = 20.sp,
                            letterSpacing = 0.sp
                        )
                        Text(
                            text = location,
                            fontWeight = FontWeight.Normal,
                            fontSize = 13.sp,
                            letterSpacing = 0.sp,
                            color = FioriWhite.copy(alpha = 0.85f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = FioriBlue,
                    titleContentColor = FioriWhite
                )
            )
        },
        containerColor = FioriLightGray
    ) { paddingValues ->
        when (val state = uiState) {
            is WeatherUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = FioriBlue)
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
        item { Spacer(modifier = Modifier.height(12.dp)) }

        // Day Selection Strip
        item {
            DaySelectionStrip(
                dailyForecasts = dailyForecasts,
                selectedDate = selectedDate,
                onDaySelected = onDaySelected,
                onTodaySelected = onTodaySelected
            )
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }

        // Current Weather Inline
        if (currentWeather != null && selectedDate == null) {
            item {
                CurrentWeatherInline(
                    current = currentWeather,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            item { Spacer(modifier = Modifier.height(16.dp)) }
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
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }

        // Hourly Forecast Section
        if (filteredHourlyForecasts.isNotEmpty()) {
            item {
                Text(
                    text = "HOURLY",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium,
                    color = FioriDarkGray.copy(alpha = 0.7f),
                    fontSize = 11.sp,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(start = 18.dp, bottom = 8.dp)
                )
            }

            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    items(filteredHourlyForecasts) { forecast ->
                        HourlyForecastCard(forecast, showSoilData)
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
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
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        modifier = Modifier.height(100.dp)
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

    Card(
        modifier = Modifier
            .width(68.dp)
            .height(100.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) FioriBlue else FioriWhite
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = dayName,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = if (isSelected) FioriWhite else FioriBlack,
                fontSize = 12.sp,
                letterSpacing = 0.sp
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
                    fontWeight = FontWeight.Medium,
                    color = if (isSelected) FioriWhite else FioriBlack,
                    fontSize = 14.sp
                )
                if (dayOfMonth.isNotEmpty()) {
                    Text(
                        text = dayOfMonth,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) FioriWhite.copy(alpha = 0.7f) else FioriDarkGray.copy(alpha = 0.6f),
                        fontSize = 10.sp
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
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = FioriWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Temperature and condition
            Column {
                Text(
                    text = current.temperature,
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Light,
                    color = FioriBlack,
                    fontSize = 56.sp,
                    letterSpacing = (-2).sp
                )
                Text(
                    text = current.condition,
                    style = MaterialTheme.typography.bodyLarge,
                    color = FioriDarkGray,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Normal
                )
            }

            // Right: Weather details
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
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
            color = FioriDarkGray,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal
        )
    }
}

@Composable
fun SelectedDayCard(
    forecast: DailyForecast,
    showSoilData: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = FioriWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
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
                style = MaterialTheme.typography.bodyLarge,
                color = FioriDarkGray,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = forecast.highTemp,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Medium,
                        color = FioriBlack,
                        fontSize = 28.sp
                    )
                    Text(
                        text = "High",
                        style = MaterialTheme.typography.labelMedium,
                        color = FioriDarkGray.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = forecast.lowTemp,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Medium,
                        color = FioriBlack,
                        fontSize = 28.sp
                    )
                    Text(
                        text = "Low",
                        style = MaterialTheme.typography.labelMedium,
                        color = FioriDarkGray.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                }

                if (forecast.precipitation > 0) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${forecast.precipitation}%",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Medium,
                            color = FioriBlue,
                            fontSize = 28.sp
                        )
                        Text(
                            text = "Rain",
                            style = MaterialTheme.typography.labelMedium,
                            color = FioriDarkGray.copy(alpha = 0.7f),
                            fontSize = 12.sp
                        )
                    }
                }
            }

            if (showSoilData && forecast.soilTemperature != null) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = FioriGray.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = forecast.soilTemperature,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            color = FioriBlack,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "Soil Temp",
                            style = MaterialTheme.typography.labelSmall,
                            color = FioriDarkGray.copy(alpha = 0.7f),
                            fontSize = 11.sp
                        )
                    }

                    if (forecast.soilMoisture != null) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = forecast.soilMoisture,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium,
                                color = FioriBlack,
                                fontSize = 18.sp
                            )
                            Text(
                                text = "Soil Moisture",
                                style = MaterialTheme.typography.labelSmall,
                                color = FioriDarkGray.copy(alpha = 0.7f),
                                fontSize = 11.sp
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
    Card(
        modifier = Modifier
            .width(100.dp)
            .height(140.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = FioriWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
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
                fontWeight = FontWeight.Normal,
                color = FioriDarkGray,
                fontSize = 11.sp,
                letterSpacing = 0.sp
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
                    fontWeight = FontWeight.Normal,
                    color = FioriBlack,
                    fontSize = 20.sp
                )

                if (forecast.precipitationProbability != null && forecast.precipitationProbability > 0) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = FioriBlue.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = "${forecast.precipitationProbability}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = FioriBlue,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }

                if (showSoilData && forecast.soilTemperature != null) {
                    Text(
                        text = forecast.soilTemperature,
                        style = MaterialTheme.typography.labelSmall,
                        color = FioriDarkGray.copy(alpha = 0.7f),
                        fontSize = 10.sp
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
                fontSize = 48.sp
            )
            Text(
                text = "Unable to Load Weather",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = FioriBlack
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = FioriDarkGray,
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = FioriBlue),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Retry",
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
