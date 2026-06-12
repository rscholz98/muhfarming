# Weather Icon System Documentation

## Overview
The weather app uses WMO (World Meteorological Organization) weather interpretation codes from the Open-Meteo API, mapped to emoji icons for visual representation.

## WMO Weather Codes

### Code Structure
Open-Meteo uses WMO Weather Interpretation Codes (WW):
- **0**: Clear sky
- **1-3**: Clouds (varying coverage)
- **45-48**: Fog
- **51-57**: Drizzle
- **61-67**: Rain
- **71-77**: Snow
- **80-82**: Rain showers
- **85-86**: Snow showers
- **95-99**: Thunderstorm

## Icon Mapping

### Clear & Clouds
| Code | Condition | Emoji | OpenWeatherMap Equivalent |
|------|-----------|-------|---------------------------|
| 0 | Clear Sky | ☀️ | 01d (clear sky) |
| 1 | Mainly Clear | 🌤️ | 02d (few clouds) |
| 2 | Partly Cloudy | ⛅ | 03d (scattered clouds) |
| 3 | Overcast | ☁️ | 04d (broken clouds) |

### Fog & Mist
| Code | Condition | Emoji | OpenWeatherMap Equivalent |
|------|-----------|-------|---------------------------|
| 45 | Fog | 🌫️ | 50d (mist) |
| 48 | Depositing Rime Fog | 🌫️ | 50d (mist) |

### Drizzle
| Code | Condition | Emoji | OpenWeatherMap Equivalent |
|------|-----------|-------|---------------------------|
| 51 | Light Drizzle | 🌦️ | 09d (shower rain) |
| 53 | Moderate Drizzle | 🌦️ | 09d (shower rain) |
| 55 | Dense Drizzle | 🌧️ | 09d (shower rain) |
| 56 | Light Freezing Drizzle | 🌧️ | 13d (snow) |
| 57 | Dense Freezing Drizzle | 🌧️ | 13d (snow) |

### Rain
| Code | Condition | Emoji | OpenWeatherMap Equivalent |
|------|-----------|-------|---------------------------|
| 61 | Slight Rain | 🌧️ | 10d (rain) |
| 63 | Moderate Rain | 🌧️ | 10d (rain) |
| 65 | Heavy Rain | 🌧️ | 10d (rain) |
| 66 | Light Freezing Rain | 🌨️ | 13d (snow) |
| 67 | Heavy Freezing Rain | 🌨️ | 13d (snow) |

### Snow
| Code | Condition | Emoji | OpenWeatherMap Equivalent |
|------|-----------|-------|---------------------------|
| 71 | Slight Snow Fall | 🌨️ | 13d (snow) |
| 73 | Moderate Snow Fall | ❄️ | 13d (snow) |
| 75 | Heavy Snow Fall | ❄️ | 13d (snow) |
| 77 | Snow Grains | 🌨️ | 13d (snow) |

### Rain Showers
| Code | Condition | Emoji | OpenWeatherMap Equivalent |
|------|-----------|-------|---------------------------|
| 80 | Slight Rain Showers | 🌦️ | 09d (shower rain) |
| 81 | Moderate Rain Showers | 🌧️ | 09d (shower rain) |
| 82 | Violent Rain Showers | ⛈️ | 09d (shower rain) |

### Snow Showers
| Code | Condition | Emoji | OpenWeatherMap Equivalent |
|------|-----------|-------|---------------------------|
| 85 | Slight Snow Showers | 🌨️ | 13d (snow) |
| 86 | Heavy Snow Showers | ❄️ | 13d (snow) |

### Thunderstorm
| Code | Condition | Emoji | OpenWeatherMap Equivalent |
|------|-----------|-------|---------------------------|
| 95 | Thunderstorm | ⛈️ | 11d (thunderstorm) |
| 96 | Thunderstorm with Slight Hail | ⛈️ | 11d (thunderstorm) |
| 99 | Thunderstorm with Heavy Hail | ⛈️ | 11d (thunderstorm) |

## Implementation Details

### Data Flow
```
Open-Meteo API
    ↓
Returns: weatherCode (Int)
    ↓
WeatherRepository
    ↓
Stores in: HourlyForecast.weatherCode
           DailyForecast.weatherCode
    ↓
WeatherScreen UI
    ↓
Calls: WeatherCodeMapper.getWeatherIcon(weatherCode)
    ↓
Displays: Emoji icon
```

### Code Example

```kotlin
// In WeatherRepository
HourlyForecast(
    time = "14:00",
    temperature = "28°C",
    condition = "Moderate Rain",
    weatherCode = 63,  // Actual WMO code from API
    // ... other fields
)

// In WeatherScreen
Text(
    text = WeatherCodeMapper.getWeatherIcon(forecast.weatherCode),
    fontSize = 32.sp
)
// Displays: 🌧️
```

### WeatherCodeMapper Functions

#### `getWeatherCondition(code: Int): String`
Returns human-readable condition name.
```kotlin
getWeatherCondition(63)  // Returns: "Moderate Rain"
```

#### `getWeatherIcon(code: Int): String`
Returns emoji icon for the weather code.
```kotlin
getWeatherIcon(63)  // Returns: "🌧️"
```

#### `getDetailedDescription(code: Int, precipProbability: Int?): String`
Returns detailed description with precipitation probability.
```kotlin
getDetailedDescription(63, 75)  
// Returns: "Moderate Rain (75% chance of precipitation)"
```

#### `getWeatherIconWithTime(code: Int, isNight: Boolean): String`
Returns day/night appropriate icon (limited support).
```kotlin
getWeatherIconWithTime(0, isNight = true)   // Returns: "🌙"
getWeatherIconWithTime(0, isNight = false)  // Returns: "☀️"
```

## Emoji Selection Rationale

### Primary Icons
- **☀️ Sun**: Clear sky, bright weather
- **🌤️ Sun behind cloud**: Mostly clear with some clouds
- **⛅ Sun behind cloud**: Partly cloudy
- **☁️ Cloud**: Overcast, cloudy
- **🌫️ Fog**: Foggy, misty conditions
- **🌦️ Sun behind rain cloud**: Light rain, drizzle with sun
- **🌧️ Cloud with rain**: Rain, steady precipitation
- **🌨️ Cloud with snow**: Snow, winter precipitation
- **❄️ Snowflake**: Heavy snow, snowfall
- **⛈️ Cloud with lightning**: Thunderstorm, severe weather
- **🌙 Moon**: Night time clear sky

### Icon Consistency
- All icons use standard Unicode emoji
- Cross-platform compatible
- Consistent size scaling across devices
- No external assets required
- Instant rendering without network calls

## Day/Night Support

### Current Implementation
Open-Meteo API doesn't include day/night indicators in weather codes. The app uses:
- Standard icons for all times
- Optional `getWeatherIconWithTime()` function for future enhancement
- Clear sky shows ☀️ (could be enhanced to show 🌙 at night)

### Future Enhancement
To add day/night support:
1. Parse time from forecast timestamp
2. Calculate sunrise/sunset for location
3. Use `getWeatherIconWithTime()` with isNight flag
4. Display moon for night, sun for day

## API Response Example

```json
{
  "hourly": {
    "time": ["2026-06-12T14:00"],
    "temperature_2m": [28.5],
    "weather_code": [63]  ← WMO code
  }
}
```

Maps to:
```kotlin
HourlyForecast(
    time = "14:00",
    temperature = "28°C",
    condition = "Moderate Rain",  // From getWeatherCondition(63)
    weatherCode = 63,             // Stored for icon lookup
    // ... other fields
)
```

UI displays:
```
┌──────────┐
│  14:00   │
│   🌧️     │ ← From getWeatherIcon(63)
│   28°C   │
└──────────┘
```

## Testing Icons

### Manual Testing
1. Check each weather code displays correct emoji
2. Verify icons scale properly (32sp, 64sp)
3. Test on different devices (Android versions)
4. Confirm emoji rendering consistency

### Test Cases
```kotlin
// Clear sky
assert(getWeatherIcon(0) == "☀️")

// Partly cloudy
assert(getWeatherIcon(2) == "⛅")

// Rain
assert(getWeatherIcon(63) == "🌧️")

// Snow
assert(getWeatherIcon(75) == "❄️")

// Thunderstorm
assert(getWeatherIcon(95) == "⛈️")
```

## Benefits Over Previous Implementation

### Before
- Used `condition.hashCode()` - unpredictable mapping
- Hash collisions possible
- No correlation to actual weather
- Inconsistent icon display

### After
- ✅ Uses actual WMO weather codes from API
- ✅ Predictable, documented mapping
- ✅ Consistent with weather standards
- ✅ Accurate visual representation
- ✅ Enhanced emoji selection
- ✅ Future-ready for day/night support

## References

- [Open-Meteo API Documentation](https://open-meteo.com/en/docs)
- [WMO Weather Codes](https://www.nodc.noaa.gov/archive/arc0021/0002199/1.1/data/0-data/HTML/WMO-CODE/WMO4677.HTM)
- [OpenWeatherMap Icon Codes](https://openweathermap.org/weather-conditions)
- [Unicode Emoji List](https://unicode.org/emoji/charts/full-emoji-list.html)
