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

## Build Instructions
Ensure you have the Android SDK installed with target platform 34 and build-tools 34.0.0.

To build the debug APK:
```bash
./gradlew assembleDebug
```

The compiled APK will be located at:
`app/build/outputs/apk/debug/weather-app-debug.apk`
