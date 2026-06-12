package com.mobile.sap.data.model

import com.google.gson.annotations.SerializedName

data class OpenMeteoResponse(
    @SerializedName("latitude")
    val latitude: Double,
    @SerializedName("longitude")
    val longitude: Double,
    @SerializedName("hourly")
    val hourly: HourlyData,
    @SerializedName("daily")
    val daily: DailyData
)

data class HourlyData(
    @SerializedName("time")
    val time: List<String>,
    @SerializedName("temperature_2m")
    val temperature: List<Double>,
    @SerializedName("relative_humidity_2m")
    val humidity: List<Int>,
    @SerializedName("wind_speed_10m")
    val windSpeed: List<Double>,
    @SerializedName("weather_code")
    val weatherCode: List<Int>
)

data class DailyData(
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
