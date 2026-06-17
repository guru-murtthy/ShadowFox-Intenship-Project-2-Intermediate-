package com.shadowfox.weatherapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shadowfox.weatherapp.api.WeatherApiService
import com.shadowfox.weatherapp.model.WeatherResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WeatherViewModel : ViewModel() {

    private val apiService = WeatherApiService.create()

    private val _weatherData = MutableLiveData<WeatherResponse?>()
    val weatherData: LiveData<WeatherResponse?> get() = _weatherData

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    fun fetchWeather(city: String, apiKey: String) {
        _isLoading.value = true
        _errorMessage.value = null
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = apiService.getWeatherByCity(city, apiKey)
                _weatherData.postValue(response)
                _isLoading.postValue(false)
            } catch (e: Exception) {
                _isLoading.postValue(false)
                _errorMessage.postValue(e.localizedMessage ?: "Failed to fetch weather data")
            }
        }
    }

    fun fetchWeatherByCoordinates(lat: Double, lon: Double, apiKey: String) {
        _isLoading.value = true
        _errorMessage.value = null
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = apiService.getWeatherByCoordinates(lat, lon, apiKey)
                _weatherData.postValue(response)
                _isLoading.postValue(false)
            } catch (e: Exception) {
                _isLoading.postValue(false)
                _errorMessage.postValue(e.localizedMessage ?: "Failed to fetch weather by location")
            }
        }
    }
}
