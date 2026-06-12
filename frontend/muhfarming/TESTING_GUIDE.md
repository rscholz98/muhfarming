# Testing and Customization Guide

## Running the App

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or later
- Android SDK 30 or higher
- Gradle 8.0+

### Steps to Run
1. Open Android Studio
2. Open the project folder
3. Wait for Gradle sync to complete
4. Select a device/emulator (Android 11+)
5. Click Run ▶️ or press Shift+F10

### Expected Behavior
- App launches with Weather tab active
- Bottom navigation shows 3 tabs
- Weather screen displays mock forecast data for Cameroon
- Pest Management screen shows loading, then error (until API is configured)
- Settings screen allows language and location selection

## Configuring the API

### Pest Management API Setup

1. **Update Base URL** in `RetrofitClient.kt`:
```kotlin
// Line 11
private const val BASE_URL = "https://your-actual-api.com/"
```

2. **Test API endpoint**:
   - Ensure your API is accessible
   - Endpoint: `GET /pest-info`
   - Must return JSON matching the schema

3. **Expected JSON Response**:
```json
{
  "pests": [
    {
      "pest-name": "Aphids",
      "pest-description": "Small sap-sucking insects that damage leaves and stems.",
      "season": "Spring",
      "severity": "Medium"
    }
  ]
}
```

### Weather API Integration (Optional)

Currently using mock data. To integrate real weather API:

1. **Add Weather API Service** in `ApiService.kt`:
```kotlin
@GET("weather/forecast")
suspend fun getWeatherForecast(
    @Query("location") location: String
): Response<WeatherForecastResponse>
```

2. **Update WeatherRepository.kt**:
   - Replace mock data with actual API call
   - Update data models if needed

3. **Popular Weather APIs**:
   - OpenWeatherMap
   - WeatherAPI
   - AccuWeather

## Testing Different Scenarios

### Test Mock Data

The weather screen uses mock data. To modify:
- Edit `WeatherRepository.kt` lines 13-39
- Adjust temperatures, conditions, precipitation

### Test Error States

To test error handling:
1. Leave API unconfigured (Pest screen will show error)
2. Disconnect internet (both screens will show errors)
3. Click "Retry" button to test reload

### Test Settings

1. Navigate to Settings tab
2. Click "Language" → Select different language (UI labels not yet translated)
3. Click "Location" → Select different city
4. Weather screen should reload with new location

## Customization Options

### Change Colors

Edit `app/src/main/java/com/mobile/sap/ui/theme/Color.kt`:

```kotlin
// Change primary blue
val FioriBlue = Color(0xFF0070F2)  // Change hex code

// Change severity colors
val SeverityHigh = Color(0xFFBB0000)
val SeverityMedium = Color(0xFFE76500)
val SeverityLow = Color(0xFFF8B000)
```

### Change App Name

Edit `app/src/main/res/values/strings.xml`:
```xml
<string name="app_name">Your App Name</string>
```

### Add More Cities

Edit `SettingsScreen.kt` line 156:
```kotlin
val locations = listOf(
    "Cameroon",
    "Yaoundé, Cameroon",
    "Douala, Cameroon",
    "Garoua, Cameroon",
    "Bamenda, Cameroon",
    // Add more cities here
    "Your City, Cameroon"
)
```

### Add More Languages

Edit `SettingsScreen.kt` line 130:
```kotlin
val languages = listOf(
    "English", 
    "French", 
    "Arabic", 
    "Spanish",
    // Add more languages
    "German"
)
```

Note: Language selection currently only changes the setting. To add translations:
1. Create `res/values-{lang}/strings.xml` files
2. Translate all string resources
3. Android system handles language switching

### Modify Card Styles

To change card appearance, edit screen files:
- `WeatherScreen.kt` - Weather cards
- `PestManagementScreen.kt` - Pest cards
- `SettingsScreen.kt` - Settings cards

Common properties to adjust:
```kotlin
Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(12.dp),  // Corner radius
    colors = CardDefaults.cardColors(
        containerColor = FioriWhite      // Background color
    ),
    elevation = CardDefaults.cardElevation(
        defaultElevation = 2.dp          // Shadow depth
    )
)
```

## Debugging

### Enable Retrofit Logging

Already enabled in `RetrofitClient.kt`:
```kotlin
private val loggingInterceptor = HttpLoggingInterceptor().apply {
    level = HttpLoggingInterceptor.Level.BODY
}
```

View logs in Logcat filtered by "OkHttp"

### Common Issues

**Issue: App won't compile**
- Solution: Sync Gradle files (File → Sync Project with Gradle Files)
- Ensure internet connection for dependency download

**Issue: Pest screen shows error**
- Solution: Configure API base URL in `RetrofitClient.kt`
- Check API is accessible
- Verify JSON response format

**Issue: Navigation not working**
- Solution: Ensure all screens are properly registered in `AppNavigation.kt`

**Issue: Colors not applying**
- Solution: Check `Theme.kt` has `dynamicColor = false`
- Verify color values in `Color.kt`

## Performance Optimization

### Reduce Network Timeouts

Edit `RetrofitClient.kt`:
```kotlin
private val okHttpClient = OkHttpClient.Builder()
    .connectTimeout(30, TimeUnit.SECONDS)  // Adjust timeout
    .readTimeout(30, TimeUnit.SECONDS)     // Adjust timeout
    .build()
```

### Add Caching

To cache API responses, add to `OkHttpClient.Builder()`:
```kotlin
.cache(Cache(context.cacheDir, 10 * 1024 * 1024)) // 10 MB cache
```

## Building for Production

### Generate Signed APK

1. Build → Generate Signed Bundle/APK
2. Select APK
3. Create or select keystore
4. Choose release variant
5. Click Finish

### ProGuard Configuration

Already configured in `build.gradle.kts`:
```kotlin
buildTypes {
    release {
        isMinifyEnabled = false  // Change to true for production
        proguardFiles(
            getDefaultProguardFile("proguard-android-optimize.txt"),
            "proguard-rules.pro"
        )
    }
}
```

## Additional Features to Implement

### Suggested Enhancements

1. **User Authentication**
   - Add login/registration screens
   - Store user preferences

2. **Offline Support**
   - Use Room database for caching
   - Show last loaded data when offline

3. **Push Notifications**
   - Integrate Firebase Cloud Messaging
   - Notify users of pest alerts

4. **Dark Mode**
   - Already partially implemented in `Theme.kt`
   - Update screens to respect system theme

5. **Pull to Refresh**
   - Add SwipeRefresh to screens
   - Allow manual data refresh

6. **Search & Filter**
   - Add search bar to pest screen
   - Filter by severity or season

7. **Detailed Views**
   - Add pest detail screen with more info
   - Add hourly weather detail screen

8. **Analytics**
   - Integrate Firebase Analytics
   - Track user behavior

## Testing Checklist

- [ ] App launches successfully
- [ ] All three tabs are accessible
- [ ] Weather screen displays mock data
- [ ] Pest screen shows loading/error state
- [ ] Settings can change language
- [ ] Settings can change location
- [ ] Location change updates weather screen
- [ ] Navigation between tabs works smoothly
- [ ] Error states show retry button
- [ ] Retry button reloads data
- [ ] App handles rotation correctly
- [ ] Colors match SAP Fiori design
- [ ] Cards have proper spacing and elevation
- [ ] Text is readable on all backgrounds
- [ ] Bottom navigation highlights active tab

## Resources

- [SAP Fiori Design Guidelines](https://www.sap.com/design-system/fiori-design-android/)
- [Jetpack Compose Documentation](https://developer.android.com/jetpack/compose)
- [Material Design 3](https://m3.material.io/)
- [Retrofit Documentation](https://square.github.io/retrofit/)
