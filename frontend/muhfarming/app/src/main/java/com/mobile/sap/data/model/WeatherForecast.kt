package com.mobile.sap.data.model

data class WeatherForecast(
    val location: String,
    val currentWeather: CurrentWeatherInfo?,
    val hourlyForecasts: List<HourlyForecast>,
    val dailyForecasts: List<DailyForecast>
)

data class CurrentWeatherInfo(
    val temperature: String,
    val humidity: Int,
    val precipitation: String,
    val rain: String,
    val condition: String
)

data class HourlyForecast(
    val time: String,
    val date: String,
    val temperature: String,
    val condition: String,
    val weatherCode: Int,
    val humidity: Int,
    val windSpeed: String,
    val precipitationProbability: Int? = null,
    val soilTemperature: String? = null,
    val soilMoisture: String? = null
)

data class DailyForecast(
    val date: String,
    val day: String,
    val highTemp: String,
    val lowTemp: String,
    val condition: String,
    val weatherCode: Int,
    val precipitation: Int,
    val soilTemperature: String? = null,
    val soilMoisture: String? = null
)
