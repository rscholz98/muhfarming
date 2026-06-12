# Testing Location Configuration Feature

## Prerequisites
- Physical Android device (recommended) or emulator with Google Play Services
- Location services enabled on device
- Internet connection for weather API

## Test Cases

### 1. Manual City Selection (Default Mode)
**Steps:**
1. Open app
2. Navigate to Settings screen
3. Verify "Auto Geolocation" switch is OFF
4. Tap on "City" setting
5. Select a city from the list (e.g., Douala)
6. Navigate back to Weather screen

**Expected:**
- City dialog shows 10 Cameroon cities
- Weather screen updates with selected city name
- Weather data loads for selected city coordinates
- No permission prompts

### 2. Enable Auto Geolocation (First Time)
**Steps:**
1. Open Settings screen
2. Toggle "Auto Geolocation" switch ON
3. Grant location permission when prompted
4. Wait for location update

**Expected:**
- Permission dialog appears requesting location access
- After granting, location is fetched automatically
- Location shows as "CityName (Auto)"
- Last update time shows "Just now"
- Weather updates with current location

### 3. Refresh Auto Location
**Steps:**
1. Ensure Auto Geolocation is ON
2. Note current location and time
3. Move to a different location (or wait a few minutes)
4. Tap "Refresh" button in Settings

**Expected:**
- Location updates to closest city
- Last update time updates to "Just now"
- Weather screen refreshes with new data
- Location name updates in Weather screen header

### 4. Switch Between Modes
**Steps:**
1. Enable Auto Geolocation
2. Wait for location update
3. Toggle Auto Geolocation OFF
4. Tap "City" and select a different city
5. Toggle Auto Geolocation back ON

**Expected:**
- Mode switches smoothly
- Weather updates appropriately for each mode
- No crashes or data loss
- City selection disappears when auto is on
- Refresh button appears when auto is on

### 5. Permission Denial Handling
**Steps:**
1. Toggle Auto Geolocation ON
2. Deny location permission
3. Toggle Auto Geolocation OFF
4. Select a manual city
5. Toggle Auto Geolocation ON again

**Expected:**
- App doesn't crash when permission denied
- Falls back gracefully (no location update)
- Manual selection still works
- Permission prompt appears again on next toggle

### 6. Last Update Time Display
**Steps:**
1. Enable Auto Geolocation
2. Refresh location
3. Wait 2 minutes, check time display
4. Wait 1 hour, check time display
5. Wait 1 day, check time display

**Expected:**
- Shows "Just now" initially
- Shows "2m ago" after 2 minutes
- Shows "1h ago" after 1 hour
- Shows formatted date after 1 day (e.g., "Jun 12, 14:30")

### 7. Weather Screen Updates
**Steps:**
1. Change location (manual or auto)
2. Navigate to Weather screen

**Expected:**
- Location name updates in header
- Weather data corresponds to new location
- Current weather card shows correct data
- Hourly and daily forecasts update

### 8. Edge Cases

#### No Internet Connection
**Steps:**
1. Disable internet
2. Try to refresh location (auto mode)
3. Try to change city (manual mode)

**Expected:**
- Error message appears
- App doesn't crash
- Previous data remains visible

#### GPS Not Available
**Steps:**
1. Disable Location services in device settings
2. Try to toggle Auto Geolocation ON

**Expected:**
- Permission request fails or times out
- Graceful error handling
- Can switch to manual mode

#### Multiple Rapid Refreshes
**Steps:**
1. Enable Auto Geolocation
2. Tap Refresh button multiple times quickly

**Expected:**
- No crashes
- Last request completes
- UI remains responsive

## Success Criteria
- ✅ All 10 Cameroon cities are selectable
- ✅ Auto geolocation finds closest city
- ✅ Permissions are properly requested and handled
- ✅ Weather updates correctly for both modes
- ✅ Last update time displays correctly
- ✅ Refresh button works reliably
- ✅ No crashes or ANRs
- ✅ Smooth transitions between modes
- ✅ No Geocoding API calls (check network logs)

## Known Limitations
1. Auto location only updates on manual refresh (not automatic/periodic)
2. Closest city is calculated from 10 predefined cities only
3. Requires Google Play Services (won't work on devices without it)
4. Location permission must be granted for auto mode to work
