package com.mobile.sap.data.model

data class WeatherSettings(
    val latitude: Double = 3.848,
    val longitude: Double = 11.502,
    val locationName: String = "Yaoundé",
    val useAutoLocation: Boolean = false,
    val lastUpdateTime: Long? = null,
    val temperatureUnit: TemperatureUnit = TemperatureUnit.CELSIUS,
    val windSpeedUnit: WindSpeedUnit = WindSpeedUnit.KMH,
    val precipitationUnit: PrecipitationUnit = PrecipitationUnit.MM,
    val showSoilData: Boolean = true,
    val showCurrentWeather: Boolean = true,
    val selectedDate: String? = null
)

enum class TemperatureUnit(val value: String, val symbol: String) {
    CELSIUS("celsius", "°C"),
    FAHRENHEIT("fahrenheit", "°F")
}

enum class WindSpeedUnit(val value: String, val symbol: String) {
    KMH("kmh", "km/h"),
    MS("ms", "m/s"),
    MPH("mph", "mph"),
    KNOTS("kn", "kn")
}

enum class PrecipitationUnit(val value: String, val symbol: String) {
    MM("mm", "mm"),
    INCH("inch", "in")
}
