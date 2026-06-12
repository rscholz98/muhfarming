# API Error Fixes

## Issues Fixed

### 1. Wind Speed Unit Error
**Error:** `Cannot initialize WindspeedUnit from invalid String value km/h`

**Cause:** API expects `kmh` not `km/h` with slash

**Fix:** Updated `WindSpeedUnit` enum values:
```kotlin
enum class WindSpeedUnit(val value: String, val symbol: String) {
    KMH("kmh", "km/h"),      // API value: kmh, Display: km/h
    MS("ms", "m/s"),          // API value: ms, Display: m/s
    MPH("mph", "mph"),        // Already correct
    KNOTS("kn", "kn")         // Already correct
}
```

### 2. Daily Soil Parameters Error
**Error:** `Cannot initialize ForecastVariableDaily from invalid String value ...soil_temperature_0cm_mean,soil_moisture_0_to_1cm_mean`

**Cause:** Open-Meteo API does not support soil data in daily aggregations

**Fix:** 
- Removed soil parameters from daily API request
- Soil data only available in hourly forecasts
- Updated `DailyWeatherData` model to remove soil fields
- Set soil data to `null` in daily forecasts

## API Parameter Changes

### Working Query Parameters

**Hourly (with soil data when enabled):**
```
temperature_2m,relative_humidity_2m,wind_speed_10m,weather_code,
precipitation_probability,soil_temperature_0cm,soil_moisture_0_to_1cm
```

**Daily (no soil data):**
```
temperature_2m_max,temperature_2m_min,precipitation_probability_max,weather_code
```

**Current (optional):**
```
temperature_2m,relative_humidity_2m,precipitation,rain,weather_code
```

**Units:**
- `temperature_unit=celsius` or `fahrenheit`
- `wind_speed_unit=kmh` or `ms` or `mph` or `kn` (NO SLASHES!)
- `precipitation_unit=mm` or `inch`

## Valid API Request Example

```
https://api.open-meteo.com/v1/forecast?
  latitude=6.0&
  longitude=12.0&
  hourly=temperature_2m,relative_humidity_2m,wind_speed_10m,weather_code,precipitation_probability,soil_temperature_0cm,soil_moisture_0_to_1cm&
  daily=temperature_2m_max,temperature_2m_min,precipitation_probability_max,weather_code&
  current=temperature_2m,relative_humidity_2m,precipitation,rain,weather_code&
  temperature_unit=celsius&
  wind_speed_unit=kmh&
  precipitation_unit=mm&
  timezone=auto
```

## Impact on UI

### Hourly Forecast
- ✅ Shows soil temperature (when enabled)
- ✅ Shows soil moisture (when enabled)
- ✅ All weather data available

### Daily Forecast
- ✅ Shows temperature high/low
- ✅ Shows precipitation probability
- ✅ Shows weather condition
- ❌ Soil data NOT shown (API limitation)
- Note: Daily cards won't display soil metrics even if toggle is on

## Open-Meteo API Limitations

According to the API documentation:
- Soil data is ONLY available at hourly resolution
- Daily aggregations do NOT include soil parameters
- This is a permanent API limitation, not a bug

## Build Status

✅ **BUILD SUCCESSFUL**
- Wind speed units fixed
- Soil data handling corrected
- API requests now properly formatted
- App ready for testing with real API

## Testing Notes

1. Weather should now load successfully
2. All 5 locations should work
3. Unit changes should work correctly
4. Soil toggle affects ONLY hourly forecasts
5. Daily forecasts will never show soil data (API design)
