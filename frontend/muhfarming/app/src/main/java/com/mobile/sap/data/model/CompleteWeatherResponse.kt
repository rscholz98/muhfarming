package com.mobile.sap.data.model

import com.google.gson.annotations.SerializedName

data class CompleteWeatherResponse(
    @SerializedName("latitude")
    val latitude: Double,
    @SerializedName("longitude")
    val longitude: Double,
    @SerializedName("current")
    val current: CurrentWeather?,
    @SerializedName("hourly")
    val hourly: HourlyWeatherData,
    @SerializedName("daily")
    val daily: DailyWeatherData
)

data class CurrentWeather(
    @SerializedName("time")
    val time: String,
    @SerializedName("temperature_2m")
    val temperature: Double,
    @SerializedName("relative_humidity_2m")
    val humidity: Int,
    @SerializedName("precipitation")
    val precipitation: Double,
    @SerializedName("rain")
    val rain: Double,
    @SerializedName("weather_code")
    val weatherCode: Int
)

data class HourlyWeatherData(
    @SerializedName("time")
    val time: List<String>,
    @SerializedName("temperature_2m")
    val temperature: List<Double>,
    @SerializedName("relative_humidity_2m")
    val humidity: List<Int>,
    @SerializedName("wind_speed_10m")
    val windSpeed: List<Double>,
    @SerializedName("weather_code")
    val weatherCode: List<Int>,
    @SerializedName("precipitation_probability")
    val precipitationProbability: List<Int>?,
    @SerializedName("soil_temperature_0cm")
    val soilTemperature: List<Double>?,
    @SerializedName("soil_moisture_0_to_1cm")
    val soilMoisture: List<Double>?
)

data class DailyWeatherData(
    @SerializedName("time")
    val time: List<String>,
    @SerializedName("temperature_2m_max")
    val temperatureMax: List<Double>,
    @SerializedName("temperature_2m_min")
    val temperatureMin: List<Double>,
    @SerializedName("precipitation_probability_max")
    val precipitationProbability: List<Int>,
    @SerializedName("weather_code")
    val weatherCode: List<Int>
)
