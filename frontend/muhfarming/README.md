# Farm Assistant - Android Mobile App

A modern Android application built with Jetpack Compose, following SAP Fiori design principles with bright blue and white color scheme.

## Features

### 1. Weather Forecast Tab ☀️
- **Location-based forecast** for Cameroon
- **Hourly forecast** cards showing:
  - Temperature
  - Weather condition
  - Humidity percentage
  - Wind speed
- **7-day forecast** with daily high/low temperatures and precipitation chance
- Beautiful card-based UI with SAP Fiori blue theme

### 2. Pest Management Tab 🐛
- Lists pest information from API endpoint
- Displays pest details:
  - Pest name
  - Description
  - Active season
  - Severity level (High/Medium/Low) with color-coded badges
- Color-coded severity indicators:
  - **Red** - High severity
  - **Orange** - Medium severity
  - **Yellow** - Low severity

### 3. Settings Tab ⚙️
- **Language selection**: English, French, Arabic, Spanish
- **Location configuration** for weather forecasts:
  - Cameroon (general)
  - Yaoundé, Cameroon
  - Douala, Cameroon
  - Garoua, Cameroon
  - Bamenda, Cameroon
- App version and build information

## Design System

### SAP Fiori Design Colors
Following the [SAP Fiori Design System for Android](https://www.sap.com/design-system/fiori-design-android/v24-12/foundations/colors):

- **Primary Blue**: `#0070F2` - Main brand color
- **Light Blue**: `#5899DA` - Secondary elements
- **Dark Blue**: `#003F8F` - Accents
- **White**: `#FFFFFF` - Cards and surfaces
- **Light Gray**: `#F5F6F7` - Background
- **Status Colors**:
  - Success: `#107E3E`
  - Warning: `#E76500`
  - Error: `#BB0000`

## API Integration

### Pest Information API
```
GET /pest-info
```

**Response Schema:**
```json
{
  "pests": [
    {
      "pest-name": "string",
      "pest-description": "string",
      "season": "string",
      "severity": "string"
    }
  ]
}
```

**Example Response:**
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

## Technical Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM (Model-View-ViewModel)
- **Navigation**: Navigation Compose
- **Networking**: Retrofit 2 + OkHttp3
- **Coroutines**: Kotlin Coroutines for async operations
- **Minimum SDK**: 30 (Android 11)
- **Target SDK**: 36

## Project Structure

```
app/src/main/java/com/mobile/sap/
├── data/
│   ├── api/
│   │   ├── ApiService.kt          # Retrofit API interface
│   │   └── RetrofitClient.kt      # Retrofit configuration
│   ├── model/
│   │   ├── PestInfo.kt            # Pest data models
│   │   └── WeatherForecast.kt     # Weather data models
│   └── repository/
│       ├── PestRepository.kt       # Pest data repository
│       └── WeatherRepository.kt    # Weather data repository
├── ui/
│   ├── navigation/
│   │   ├── AppNavigation.kt       # Main navigation setup
│   │   └── Screen.kt              # Screen definitions
│   ├── screens/
│   │   ├── WeatherScreen.kt       # Weather forecast UI
│   │   ├── PestManagementScreen.kt # Pest management UI
│   │   └── SettingsScreen.kt      # Settings UI
│   ├── theme/
│   │   ├── Color.kt               # SAP Fiori colors
│   │   ├── Theme.kt               # Material3 theme
│   │   └── Type.kt                # Typography
│   └── viewmodel/
│       ├── WeatherViewModel.kt    # Weather screen state
│       └── PestViewModel.kt       # Pest screen state
└── MainActivity.kt                 # App entry point
```

## Setup Instructions

1. **Clone the repository**
2. **Open in Android Studio** (Recommended: Android Studio Hedgehog or later)
3. **Configure API endpoint**:
   - Update `BASE_URL` in `RetrofitClient.kt` with your actual API domain
   - Example: `private const val BASE_URL = "https://your-api.com/"`
4. **Sync Gradle** and wait for dependencies to download
5. **Run the app** on an emulator or physical device (Android 11+)

## Configuration

### API Configuration
Edit `app/src/main/java/com/mobile/sap/data/api/RetrofitClient.kt`:

```kotlin
private const val BASE_URL = "https://your-api-domain.com/"
```

### Weather Data
Currently using mock data in `WeatherRepository.kt`. To integrate real weather API:
1. Add weather API service interface to `ApiService.kt`
2. Update `WeatherRepository.kt` to call the actual API
3. Update data models in `WeatherForecast.kt` to match API response

## Building

### Debug Build
```bash
./gradlew assembleDebug
```

### Release Build
```bash
./gradlew assembleRelease
```

## Dependencies

Key dependencies included:
- `androidx.navigation:navigation-compose:2.7.7` - Navigation
- `com.squareup.retrofit2:retrofit:2.9.0` - HTTP client
- `com.squareup.retrofit2:converter-gson:2.9.0` - JSON parsing
- `androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0` - ViewModel support
- `io.coil-kt:coil-compose:2.5.0` - Image loading

## Screenshots

The app features:
- Clean, modern Material Design 3 UI
- SAP Fiori blue and white color scheme
- Smooth tab navigation with bottom navigation bar
- Responsive cards with rounded corners
- Color-coded severity indicators for pest information
- Comprehensive weather forecast visualization

## Future Enhancements

- [ ] Real-time weather API integration
- [ ] Push notifications for severe pest warnings
- [ ] Offline data caching
- [ ] User authentication
- [ ] Additional language support
- [ ] Dark mode support
- [ ] Weather alerts and notifications
- [ ] Farm location mapping

## License

This project is created for farming assistance purposes.
