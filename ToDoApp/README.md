# ToDoApp

A modern, local-persistence tasks manager app with priority sorting, keyword searching, voice memo recording, and image attachment capability.

## Tech Stack
- **Language**: Kotlin
- **Database**: Room Database (with Kotlin Symbol Processing - KSP)
- **UI List**: RecyclerView (using ListAdapter + DiffUtil for high performance and smooth scrolling)
- **Async**: Kotlin Coroutines + `viewModelScope` on IO Dispatcher for database writes
- **Architecture**: MVVM (ViewModel + Repository pattern) with ViewBinding
- **UI**: XML layouts (ConstraintLayout + Material Design 3)
- **Features**: 
  - Dynamic Priority Sorting (High priority first)
  - SearchView query filtering (Room `LIKE %keyword%` query)
  - Chips filter by priority (All / High / Low)
  - Swipe-to-delete with undo action
  - Voice notes recorder (using `MediaRecorder`, saving file URI)
  - Gallery photo attachments (using PhotoPicker, saving persistable URI)

## Build Instructions
Ensure you have the Android SDK installed with target platform 34 and build-tools 34.0.0.

To build the debug APK:
```bash
./gradlew assembleDebug
```

The compiled APK will be located at:
`app/build/outputs/apk/debug/todo-app-debug.apk`
