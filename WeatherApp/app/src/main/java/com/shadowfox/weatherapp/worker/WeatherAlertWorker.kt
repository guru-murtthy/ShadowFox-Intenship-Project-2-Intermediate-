package com.shadowfox.weatherapp.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.shadowfox.weatherapp.MainActivity
import com.shadowfox.weatherapp.api.WeatherApiService

class WeatherAlertWorker(
    private val appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        private const val TAG = "WeatherAlertWorker"
        private const val CHANNEL_ID = "weather_alerts"
        private const val CHANNEL_NAME = "Weather Alerts"
        private const val NOTIFICATION_ID = 101
    }

    override suspend fun doWork(): Result {
        Log.d(TAG, "Background weather check task started")
        
        // Retrieve the last searched city or default to "London"
        val sharedPrefs = appContext.getSharedPreferences("weather_prefs", Context.MODE_PRIVATE)
        val city = sharedPrefs.getString("last_city", "London") ?: "London"
        val apiKey = sharedPrefs.getString("api_key", "8bcbc0f252cfd77f8ceeaebaa03a0cc5") ?: "8bcbc0f252cfd77f8ceeaebaa03a0cc5"

        try {
            val apiService = WeatherApiService.create()
            val response = apiService.getWeatherByCity(city, apiKey)
            
            val weatherDescription = response.weather?.firstOrNull()?.main ?: ""
            Log.d(TAG, "Weather fetched in background: $weatherDescription in $city")

            if (weatherDescription.contains("Rain", ignoreCase = true)) {
                sendRainNotification(city, response.weather?.firstOrNull()?.description ?: "rainy weather")
            }
            return Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching weather in background: ${e.message}")
            return Result.retry()
        }
    }

    private fun sendRainNotification(city: String, description: String) {
        val notificationManager = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                this.description = "Triggers alerts when rain is expected"
            }
            notificationManager.createNotificationChannel(channel)
        }

        // We check for POST_NOTIFICATIONS permission on Android 13+ (API 33+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(appContext, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                Log.w(TAG, "Notification permission not granted. Cannot show notification.")
                return
            }
        }

        val builder = NotificationCompat.Builder(appContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Rain Alert for $city")
            .setContentText("Expect $description. Don't forget your umbrella!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        NotificationManagerCompat.from(appContext).notify(NOTIFICATION_ID, builder.build())
    }
}
