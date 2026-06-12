package com.mobile.sap.ui.viewmodel

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.mobile.sap.data.model.*
import com.mobile.sap.data.repository.WeatherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

sealed class WeatherUiState {
    object Loading : WeatherUiState()
    data class Success(val forecast: WeatherForecast) : WeatherUiState()
    data class Error(val message: String) : WeatherUiState()
}

class WeatherViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = WeatherRepository()
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(application)

    private val _uiState = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    private val _location = MutableStateFlow("Yaoundé")
    val location: StateFlow<String> = _location.asStateFlow()

    private val _settings = MutableStateFlow(WeatherSettings())
    val settings: StateFlow<WeatherSettings> = _settings.asStateFlow()

    private val _selectedDate = MutableStateFlow<String?>(null)
    val selectedDate: StateFlow<String?> = _selectedDate.asStateFlow()

    private val _currentDayName = MutableStateFlow(getTodayName())
    val currentDayName: StateFlow<String> = _currentDayName.asStateFlow()

    private val _locationPermissionGranted = MutableStateFlow(false)
    val locationPermissionGranted: StateFlow<Boolean> = _locationPermissionGranted.asStateFlow()

    init {
        checkLocationPermission()
        loadWeatherForecast()
    }

    private fun checkLocationPermission() {
        val context = getApplication<Application>()
        _locationPermissionGranted.value = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun updateLocationPermission(granted: Boolean) {
        _locationPermissionGranted.value = granted
    }

    fun selectDay(date: String, dayName: String) {
        _selectedDate.value = date
        _currentDayName.value = dayName
        _settings.value = _settings.value.copy(selectedDate = date)
    }

    fun selectToday() {
        _selectedDate.value = null
        _currentDayName.value = getTodayName()
        _settings.value = _settings.value.copy(selectedDate = null)
    }

    fun updateLocationMode(useAutoLocation: Boolean) {
        _settings.value = _settings.value.copy(useAutoLocation = useAutoLocation)
        if (useAutoLocation) {
            getCurrentLocationAndUpdate()
        }
    }

    fun updateManualCity(cityName: String) {
        val city = CameroonCities.getCityByName(cityName)
        if (city != null) {
            _location.value = city.name
            _settings.value = _settings.value.copy(
                latitude = city.latitude,
                longitude = city.longitude,
                locationName = city.name,
                useAutoLocation = false
            )
            selectToday()
            loadWeatherForecast()
        }
    }

    fun refreshAutoLocation() {
        if (_settings.value.useAutoLocation) {
            getCurrentLocationAndUpdate()
        }
    }

    private fun getCurrentLocationAndUpdate() {
        if (!_locationPermissionGranted.value) {
            return
        }

        viewModelScope.launch {
            try {
                val cancellationTokenSource = CancellationTokenSource()
                val location = fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                    cancellationTokenSource.token
                ).await()

                if (location != null) {
                    val closestCity = findClosestCity(location.latitude, location.longitude)
                    _location.value = "${closestCity.name} (Auto)"
                    _settings.value = _settings.value.copy(
                        latitude = location.latitude,
                        longitude = location.longitude,
                        locationName = "${closestCity.name} (Auto)",
                        lastUpdateTime = System.currentTimeMillis()
                    )
                    selectToday()
                    loadWeatherForecast()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun findClosestCity(latitude: Double, longitude: Double): CameroonCity {
        return CameroonCities.cities.minByOrNull { city ->
            val latDiff = city.latitude - latitude
            val lonDiff = city.longitude - longitude
            latDiff * latDiff + lonDiff * lonDiff
        } ?: CameroonCities.getDefaultCity()
    }

    fun updateLocation(newLocation: String, latitude: Double, longitude: Double) {
        _location.value = newLocation
        _settings.value = _settings.value.copy(
            latitude = latitude,
            longitude = longitude,
            locationName = newLocation
        )
        selectToday()
        loadWeatherForecast()
    }

    fun updateTemperatureUnit(unit: TemperatureUnit) {
        _settings.value = _settings.value.copy(temperatureUnit = unit)
        loadWeatherForecast()
    }

    fun updateWindSpeedUnit(unit: WindSpeedUnit) {
        _settings.value = _settings.value.copy(windSpeedUnit = unit)
        loadWeatherForecast()
    }

    fun updatePrecipitationUnit(unit: PrecipitationUnit) {
        _settings.value = _settings.value.copy(precipitationUnit = unit)
        loadWeatherForecast()
    }

    fun toggleSoilData(enabled: Boolean) {
        _settings.value = _settings.value.copy(showSoilData = enabled)
        loadWeatherForecast()
    }

    fun toggleCurrentWeather(enabled: Boolean) {
        _settings.value = _settings.value.copy(showCurrentWeather = enabled)
        loadWeatherForecast()
    }

    fun loadWeatherForecast() {
        viewModelScope.launch {
            _uiState.value = WeatherUiState.Loading
            repository.getWeatherForecast(_location.value, _settings.value)
                .onSuccess { forecast ->
                    _uiState.value = WeatherUiState.Success(forecast)
                }
                .onFailure { exception ->
                    _uiState.value = WeatherUiState.Error(
                        exception.message ?: "Unknown error occurred"
                    )
                }
        }
    }

    private fun getTodayName(): String {
        val calendar = Calendar.getInstance()
        val dayFormat = SimpleDateFormat("EEEE", Locale.getDefault())
        return dayFormat.format(calendar.time)
    }
}
