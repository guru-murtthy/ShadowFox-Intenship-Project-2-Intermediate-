# WeatherApp

A fully functional weather application that integrates API data, live GPS location tracking, and smart weather alerts.

## Tech Stack
- **Language**: Kotlin
- **Networking**: Retrofit2 + OkHttp + Gson
- **Async**: Kotlin Coroutines (`viewModelScope` + `Dispatchers.IO`)
- **Architecture**: MVVM (ViewModel + LiveData) with ViewBinding
- **UI**: XML layouts (ConstraintLayout + Material Design 3)
- **API**: OpenWeatherMap API (metric units)
- **WorkManager**: Background task (`WeatherAlertWorker`) running every 4 hours to send local notifications when rain is forecast.

## Setup Instructions
Before building the app, make sure to configure your OpenWeatherMap API key:
1. Obtain an API key from [OpenWeatherMap](https://openweathermap.org/api).
2. Open [MainActivity.kt](app/src/main/java/com/shadowfox/weatherapp/MainActivity.kt) and replace `"YOUR_API_KEY"` with your actual API key:
   ```kotlin
   private val apiKey = "YOUR_API_KEY"
   ```
3. Open [WeatherAlertWorker.kt](app/src/main/java/com/shadowfox/weatherapp/worker/WeatherAlertWorker.kt) and replace `"YOUR_API_KEY"` with your actual API key:
   ```kotlin
   val apiKey = sharedPrefs.getString("api_key", "YOUR_API_KEY") ?: "YOUR_API_KEY"
   ```

## Build Instructions
Ensure you have the Android SDK installed with target platform 34 and build-tools 34.0.0.

To build the debug APK:
```bash
./gradlew assembleDebug
```

The compiled APK will be located at:
`app/build/outputs/apk/debug/app-debug.apk`

