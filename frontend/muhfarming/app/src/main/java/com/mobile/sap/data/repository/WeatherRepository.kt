package com.mobile.sap.data.repository

import com.mobile.sap.data.api.WeatherRetrofitClient
import com.mobile.sap.data.model.*
import com.mobile.sap.util.WeatherCodeMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class WeatherRepository {
    private val apiService = WeatherRetrofitClient.weatherApiService

    suspend fun getWeatherForecast(
        location: String,
        settings: WeatherSettings
    ): Result<WeatherForecast> = withContext(Dispatchers.IO) {
        try {
            // Build hourly parameters
            val hourlyParams = buildList {
                add("temperature_2m")
                add("relative_humidity_2m")
                add("wind_speed_10m")
                add("weather_code")
                add("precipitation_probability")
                if (settings.showSoilData) {
                    add("soil_temperature_0cm")
                    add("soil_moisture_0_to_1cm")
                }
            }.joinToString(",")

            // Build daily parameters
            val dailyParams = buildList {
                add("temperature_2m_max")
                add("temperature_2m_min")
                add("precipitation_probability_max")
                add("weather_code")
                // Note: Soil data is not available for daily aggregations in Open-Meteo API
                // Only hourly soil data is supported
            }.joinToString(",")

            // Build current parameters
            val currentParams = if (settings.showCurrentWeather) {
                "temperature_2m,relative_humidity_2m,precipitation,rain,weather_code"
            } else null

            val response = apiService.getWeatherForecast(
                latitude = settings.latitude,
                longitude = settings.longitude,
                hourly = hourlyParams,
                daily = dailyParams,
                current = currentParams,
                temperatureUnit = settings.temperatureUnit.value,
                windSpeedUnit = settings.windSpeedUnit.value,
                precipitationUnit = settings.precipitationUnit.value
            )

            if (response.isSuccessful && response.body() != null) {
                val data = response.body()!!

                // Parse current weather
                val currentWeather = data.current?.let { current ->
                    CurrentWeatherInfo(
                        temperature = formatTemperature(current.temperature, settings.temperatureUnit),
                        humidity = current.humidity,
                        precipitation = "${current.precipitation}${settings.precipitationUnit.symbol}",
                        rain = "${current.rain}${settings.precipitationUnit.symbol}",
                        condition = WeatherCodeMapper.getWeatherCondition(current.weatherCode)
                    )
                }

                // Parse hourly forecasts (all available hours for 7 days)
                val hourlyForecasts = mutableListOf<HourlyForecast>()
                for (i in 0 until data.hourly.time.size) {
                    val timestamp = data.hourly.time[i]
                    val date = timestamp.substring(0, 10) // Extract YYYY-MM-DD
                    val time = formatTime(timestamp)
                    val temp = formatTemperature(data.hourly.temperature[i], settings.temperatureUnit)
                    val humidity = data.hourly.humidity[i]
                    val windSpeed = "${data.hourly.windSpeed[i].toInt()}${settings.windSpeedUnit.symbol}"
                    val condition = WeatherCodeMapper.getWeatherCondition(data.hourly.weatherCode[i])
                    val precipProb = data.hourly.precipitationProbability?.get(i)
                    val soilTemp = data.hourly.soilTemperature?.get(i)?.let {
                        formatTemperature(it, settings.temperatureUnit)
                    }
                    val soilMoisture = data.hourly.soilMoisture?.get(i)?.let {
                        "${String.format("%.1f", it)}%"
                    }

                    hourlyForecasts.add(
                        HourlyForecast(
                            time = time,
                            date = date,
                            temperature = temp,
                            condition = condition,
                            weatherCode = data.hourly.weatherCode[i],
                            humidity = humidity,
                            windSpeed = windSpeed,
                            precipitationProbability = precipProb,
                            soilTemperature = soilTemp,
                            soilMoisture = soilMoisture
                        )
                    )
                }

                // Parse daily forecasts (next 7 days)
                val dailyForecasts = mutableListOf<DailyForecast>()
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val dayFormat = SimpleDateFormat("EEEE", Locale.getDefault())

                for (i in 0 until minOf(7, data.daily.time.size)) {
                    val date = data.daily.time[i]
                    val parsedDate = dateFormat.parse(date)
                    val dayName = if (i == 0) "Today" else dayFormat.format(parsedDate!!)
                    val highTemp = formatTemperature(data.daily.temperatureMax[i], settings.temperatureUnit)
                    val lowTemp = formatTemperature(data.daily.temperatureMin[i], settings.temperatureUnit)
                    val precipitation = data.daily.precipitationProbability[i]
                    val condition = WeatherCodeMapper.getWeatherCondition(data.daily.weatherCode[i])

                    dailyForecasts.add(
                        DailyForecast(
                            date = date,
                            day = dayName,
                            highTemp = highTemp,
                            lowTemp = lowTemp,
                            condition = condition,
                            weatherCode = data.daily.weatherCode[i],
                            precipitation = precipitation,
                            soilTemperature = null,
                            soilMoisture = null
                        )
                    )
                }

                Result.success(
                    WeatherForecast(
                        location = location,
                        currentWeather = currentWeather,
                        hourlyForecasts = hourlyForecasts,
                        dailyForecasts = dailyForecasts
                    )
                )
            } else {
                Result.failure(Exception("Failed to fetch weather data: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun formatTime(timestamp: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.getDefault())
            val outputFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val date = inputFormat.parse(timestamp)
            outputFormat.format(date!!)
        } catch (e: Exception) {
            timestamp.substring(11, 16) // Fallback: extract HH:mm
        }
    }

    private fun formatTemperature(temp: Double, unit: TemperatureUnit): String {
        return "${temp.toInt()}${unit.symbol}"
    }
}
