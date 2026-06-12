package com.mobile.sap.util

/**
 * Weather code mapper for WMO Weather interpretation codes (WW)
 * Based on Open-Meteo API documentation and OpenWeatherMap icon mapping
 *
 * Reference: https://open-meteo.com/en/docs
 * WMO Code: https://www.nodc.noaa.gov/archive/arc0021/0002199/1.1/data/0-data/HTML/WMO-CODE/WMO4677.HTM
 */
object WeatherCodeMapper {

    /**
     * Returns human-readable weather condition for a given WMO code
     */
    fun getWeatherCondition(code: Int): String {
        return when (code) {
            // Clear
            0 -> "Clear Sky"

            // Mainly Clear / Partly Cloudy
            1 -> "Mainly Clear"
            2 -> "Partly Cloudy"
            3 -> "Overcast"

            // Fog
            45 -> "Fog"
            48 -> "Depositing Rime Fog"

            // Drizzle
            51 -> "Light Drizzle"
            53 -> "Moderate Drizzle"
            55 -> "Dense Drizzle"
            56 -> "Light Freezing Drizzle"
            57 -> "Dense Freezing Drizzle"

            // Rain
            61 -> "Slight Rain"
            63 -> "Moderate Rain"
            65 -> "Heavy Rain"
            66 -> "Light Freezing Rain"
            67 -> "Heavy Freezing Rain"

            // Snow
            71 -> "Slight Snow Fall"
            73 -> "Moderate Snow Fall"
            75 -> "Heavy Snow Fall"
            77 -> "Snow Grains"

            // Rain Showers
            80 -> "Slight Rain Showers"
            81 -> "Moderate Rain Showers"
            82 -> "Violent Rain Showers"

            // Snow Showers
            85 -> "Slight Snow Showers"
            86 -> "Heavy Snow Showers"

            // Thunderstorm
            95 -> "Thunderstorm"
            96 -> "Thunderstorm with Slight Hail"
            99 -> "Thunderstorm with Heavy Hail"

            else -> "Unknown"
        }
    }

    /**
     * Returns emoji icon for a given WMO code
     * Mapped to provide visual representation similar to OpenWeatherMap icons
     */
    fun getWeatherIcon(code: Int): String {
        return when (code) {
            // Group 0: Clear Sky
            0 -> "☀️"            // Clear sky (01d)

            // Group 1-3: Clouds
            1 -> "🌤️"            // Mainly clear / few clouds (02d)
            2 -> "⛅"            // Partly cloudy / scattered clouds (03d)
            3 -> "☁️"            // Overcast / broken clouds (04d)

            // Group 45-48: Fog
            45 -> "🌫️"           // Fog (50d)
            48 -> "🌫️"           // Depositing rime fog (50d)

            // Group 51-57: Drizzle
            51 -> "🌦️"           // Light drizzle (09d)
            53 -> "🌦️"           // Moderate drizzle (09d)
            55 -> "🌧️"           // Dense drizzle (09d)
            56 -> "🌧️"           // Light freezing drizzle (13d)
            57 -> "🌧️"           // Dense freezing drizzle (13d)

            // Group 61-67: Rain
            61 -> "🌧️"           // Slight rain (10d)
            63 -> "🌧️"           // Moderate rain (10d)
            65 -> "🌧️"           // Heavy rain (10d)
            66 -> "🌨️"           // Light freezing rain (13d)
            67 -> "🌨️"           // Heavy freezing rain (13d)

            // Group 71-77: Snow
            71 -> "🌨️"           // Slight snow (13d)
            73 -> "❄️"            // Moderate snow (13d)
            75 -> "❄️"            // Heavy snow (13d)
            77 -> "🌨️"           // Snow grains (13d)

            // Group 80-82: Rain Showers
            80 -> "🌦️"           // Slight rain showers (09d)
            81 -> "🌧️"           // Moderate rain showers (09d)
            82 -> "⛈️"            // Violent rain showers (09d)

            // Group 85-86: Snow Showers
            85 -> "🌨️"           // Slight snow showers (13d)
            86 -> "❄️"            // Heavy snow showers (13d)

            // Group 95-99: Thunderstorm
            95 -> "⛈️"            // Thunderstorm (11d)
            96 -> "⛈️"            // Thunderstorm with slight hail (11d)
            99 -> "⛈️"            // Thunderstorm with heavy hail (11d)

            // Unknown
            else -> "🌡️"         // Unknown condition
        }
    }

    /**
     * Returns a more detailed description including precipitation info
     */
    fun getDetailedDescription(code: Int, precipProbability: Int? = null): String {
        val condition = getWeatherCondition(code)
        return if (precipProbability != null && precipProbability > 0) {
            "$condition ($precipProbability% chance of precipitation)"
        } else {
            condition
        }
    }

    /**
     * Returns weather icon suitable for day/night display
     * Note: Open-Meteo doesn't provide day/night info in weather codes
     * Use this with time-based logic if needed
     */
    fun getWeatherIconWithTime(code: Int, isNight: Boolean): String {
        return when (code) {
            0 -> if (isNight) "🌙" else "☀️"      // Clear
            1 -> if (isNight) "☁️" else "🌤️"      // Mainly clear
            2 -> if (isNight) "☁️" else "⛅"       // Partly cloudy
            else -> getWeatherIcon(code)          // Others same for day/night
        }
    }
}
