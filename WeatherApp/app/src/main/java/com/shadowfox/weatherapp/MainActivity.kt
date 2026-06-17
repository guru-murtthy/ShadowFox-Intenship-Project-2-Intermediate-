package com.shadowfox.weatherapp

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.material.snackbar.Snackbar
import com.shadowfox.weatherapp.databinding.ActivityMainBinding
import com.shadowfox.weatherapp.viewmodel.WeatherViewModel
import com.shadowfox.weatherapp.worker.WeatherAlertWorker
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: WeatherViewModel by viewModels()
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // Free OpenWeatherMap API Key
    private val apiKey = "8bcbc0f252cfd77f8ceeaebaa03a0cc5"

    // Runtime location permission request
    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

        if (fineLocationGranted || coarseLocationGranted) {
            getLastKnownLocation()
        } else {
            showSnackbar("Location permission denied. Please search manually.")
            binding.cardError.visibility = View.VISIBLE
            binding.tvErrorTitle.text = "GPS Disabled"
            binding.tvErrorDescription.text = "Location permission is required to auto-fetch weather."
        }
    }

    // Android 13+ Notification permission request
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            scheduleBackgroundAlerts()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Save API key globally in shared preferences for WorkManager access
        val sharedPrefs = getSharedPreferences("weather_prefs", Context.MODE_PRIVATE)
        sharedPrefs.edit().putString("api_key", apiKey).apply()

        setupUI()
        observeViewModel()
        requestNotificationPermission()

        // Check connection and fetch default/location weather on startup
        if (!isNetworkAvailable()) {
            showNoConnectionUI()
        } else {
            getLastKnownLocation()
        }
    }

    private fun setupUI() {
        binding.btnSearch.setOnClickListener {
            val city = binding.etCity.text.toString().trim()
            if (city.isNotEmpty()) {
                hideKeyboard()
                if (isNetworkAvailable()) {
                    viewModel.fetchWeather(city, apiKey)
                } else {
                    showNoConnectionUI()
                }
            } else {
                binding.tilCity.error = "City name cannot be empty"
            }
        }

        binding.btnLocation.setOnClickListener {
            if (isNetworkAvailable()) {
                getLastKnownLocation()
            } else {
                showNoConnectionUI()
            }
        }
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            if (isLoading) {
                binding.cardWeather.visibility = View.GONE
                binding.cardError.visibility = View.GONE
            }
        }

        viewModel.weatherData.observe(this) { weather ->
            if (weather != null && weather.cod == 200) {
                binding.cardWeather.visibility = View.VISIBLE
                binding.cardError.visibility = View.GONE

                binding.tvCityName.text = "${weather.cityName ?: ""}"
                binding.tvTemperature.text = "${weather.main?.temp?.toInt() ?: 0}°C"
                binding.tvCondition.text = weather.weather?.firstOrNull()?.main ?: "N/A"
                binding.tvDescription.text = weather.weather?.firstOrNull()?.description ?: ""
                binding.tvHumidity.text = "${weather.main?.humidity ?: 0}%"
                binding.tvWindSpeed.text = "${weather.wind?.speed ?: 0.0} m/s"

                // Save last city for background alerts
                val sharedPrefs = getSharedPreferences("weather_prefs", Context.MODE_PRIVATE)
                sharedPrefs.edit().putString("last_city", weather.cityName).apply()
            }
        }

        viewModel.errorMessage.observe(this) { errorMsg ->
            if (errorMsg != null) {
                binding.cardWeather.visibility = View.GONE
                binding.cardError.visibility = View.VISIBLE
                binding.tvErrorTitle.text = "API Error"
                binding.tvErrorDescription.text = errorMsg
                showSnackbar(errorMsg)
            }
        }
    }

    private fun getLastKnownLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            
            locationPermissionLauncher.launch(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            )
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                viewModel.fetchWeatherByCoordinates(location.latitude, location.longitude, apiKey)
            } else {
                requestNewLocationData()
            }
        }.addOnFailureListener {
            requestNewLocationData()
        }
    }

    private fun requestNewLocationData() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
            .setMinUpdateIntervalMillis(5000)
            .setMaxUpdates(1)
            .build()

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    val lastLocation = locationResult.lastLocation
                    if (lastLocation != null) {
                        viewModel.fetchWeatherByCoordinates(lastLocation.latitude, lastLocation.longitude, apiKey)
                    } else {
                        showSnackbar("Unable to determine GPS location. Please search manually.")
                    }
                }
            }, Looper.getMainLooper())
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun showNoConnectionUI() {
        binding.cardWeather.visibility = View.GONE
        binding.cardError.visibility = View.VISIBLE
        binding.tvErrorTitle.text = "No Connection"
        binding.tvErrorDescription.text = "Please check your mobile data / Wi-Fi settings or toggle airplane mode."
        showSnackbar("No Internet connection available.")
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                scheduleBackgroundAlerts()
            }
        } else {
            scheduleBackgroundAlerts()
        }
    }

    private fun scheduleBackgroundAlerts() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val workRequest = PeriodicWorkRequestBuilder<WeatherAlertWorker>(4, TimeUnit.HOURS)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "WeatherAlertSync",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    private fun hideKeyboard() {
        val view = this.currentFocus
        if (view != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }
}
