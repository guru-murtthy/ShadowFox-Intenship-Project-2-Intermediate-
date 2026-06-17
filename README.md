# ShadowFox Internship - Project 2: Intermediate Level

Welcome to the intermediate level project repository for the ShadowFox Android Developer Internship. This repository contains two separate Android applications designed to demonstrate advanced concepts in local database storage, location services, networking, background tasks, and clean architectural design patterns (MVVM).

---

## 📂 Repository Structure

```
├── ToDoApp/                 # Android application for Task Management (Room DB, Media)
├── WeatherApp/              # Android application for Weather Tracking & Alerts (Retrofit, GPS, WorkManager)
├── proof/                   # Proof of deliverables (APKs and screenshots)
│   ├── apks/
│   │   ├── todo-app-debug.apk
│   │   └── weather-app-debug.apk
│   └── screenshots/
│       └── todo_screenshot.png
├── .gitignore               # Main Git exclusion configurations
├── LICENSE                  # MIT License
└── README.md                # Main documentation (this file)
```

---

## 📝 1. Task Manager App (ToDoApp)

### Overview
**ToDoApp** is a modern, feature-rich task manager application. It leverages local database storage for persistence, providing a smooth user experience for tracking daily tasks with priority tags, media attachments, and powerful search capabilities.

### Key Features
- **Local Persistence**: Full CRUD actions for task management stored in a local SQLite database via the Room library.
- **Dynamic Priority Sorting**: Sorts tasks automatically based on priority (High, Medium, Low) to keep critical tasks prominent.
- **Filter and Search**:
  - Filter tasks using a Priority selection chip group.
  - Search query functionality filtering directly using a database `LIKE %keyword%` query.
- **User Interactions**: Swipe-to-delete gesture with an immediate Undo (Snackbar) action to restore deleted entries.
- **Media Attachments**:
  - **Voice Notes**: Integrates a voice recorder (`MediaRecorder`) to attach voice memos directly to tasks.
  - **Photo Attachments**: Implements Android's `PhotoPicker` to attach gallery photos with persistable read permissions.

### Tech Stack
- **Language**: Kotlin
- **Architecture**: MVVM (Model-View-ViewModel) + Repository pattern.
- **Database**: Room Database + Kotlin Symbol Processing (KSP) + Coroutines.
- **UI & Layout**: ConstraintLayout, Material Design 3 components, ViewBinding.
- **Recycler Handling**: `RecyclerView` using `ListAdapter` and `DiffUtil` for optimized item updating and smooth scrolling animations.

---

## 🌦️ 2. Weather & Notification Alert App (WeatherApp)

### Overview
**WeatherApp** is a real-time weather details checking application. It detects the device's location, retrieves meteorological data via a remote REST API, and implements background scheduling to alert the user of impending rain conditions.

### Key Features
- **Real-Time GPS Location**: Requests GPS permissions to automatically obtain the user's current coordinates using Google Play Services Location.
- **REST API Integration**: Integrates the OpenWeatherMap API to fetch current temperatures, humidity, wind speeds, and precipitation forecasts.
- **City Search**: Allows users to manually search for weather details in any city worldwide.
- **Smart Weather Alerts (Background Worker)**:
  - Employs `WorkManager` to run a background worker (`WeatherAlertWorker`) scheduled every 4 hours.
  - Automatically queries the local forecast and fires system notifications to warn the user if rain or storms are expected in their area.

### Tech Stack
- **Language**: Kotlin
- **Architecture**: MVVM (Model-View-ViewModel) + LiveData.
- **Networking**: Retrofit2 + OkHttp + GSON Converter.
- **Background Tasks**: Android Jetpack `WorkManager`.
- **Location Services**: FusedLocationProviderClient.
- **UI & Layout**: Material Design 3, ConstraintLayout, ViewBinding.

---

## 🛠️ Build and Execution Instructions

### Prerequisites
1. **Android Studio**: Android Studio Jellyfish (or newer) is recommended.
2. **JDK**: JDK 17 or JDK 21 configured in your path.
3. **Android SDK**: Target Platform 34 and Build-Tools 34.0.0.

### Compiling from Command Line
You can compile each application independently using the Gradle wrapper.

#### 1. Compile ToDoApp
```bash
cd ToDoApp
./gradlew assembleDebug
```
The compiled debug APK will be generated at:
`ToDoApp/app/build/outputs/apk/debug/app-debug.apk`

#### 2. Compile WeatherApp
Before compiling, you need to set up your OpenWeatherMap API key:
- Open `WeatherApp/app/src/main/java/com/shadowfox/weatherapp/MainActivity.kt` and replace `"YOUR_API_KEY"` with your key.
- Open `WeatherApp/app/src/main/java/com/shadowfox/weatherapp/worker/WeatherAlertWorker.kt` and replace `"YOUR_API_KEY"` with your key.

Then compile using:
```bash
cd WeatherApp
./gradlew assembleDebug
```
The compiled debug APK will be generated at:
`WeatherApp/app/build/outputs/apk/debug/app-debug.apk`


---

## 📦 Deliverables & Proof

Pre-built APKs and demonstration files are available directly inside the repository under the `proof/` directory:
- **ToDoApp Debug APK**: [todo-app-debug.apk](proof/apks/todo-app-debug.apk)
- **WeatherApp Debug APK**: [weather-app-debug.apk](proof/apks/weather-app-debug.apk)
- **Application Screenshot**: [todo_screenshot.png](proof/screenshots/todo_screenshot.png)

---

## 📜 License
This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.