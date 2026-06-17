package com.shadowfox.weatherapp.model

import com.google.gson.annotations.SerializedName

data class WeatherResponse(
    @SerializedName("name") val cityName: String?,
    @SerializedName("main") val main: MainInfo?,
    @SerializedName("weather") val weather: List<WeatherInfo>?,
    @SerializedName("wind") val wind: WindInfo?,
    @SerializedName("cod") val cod: Int?
)

data class MainInfo(
    @SerializedName("temp") val temp: Double?,
    @SerializedName("humidity") val humidity: Int?
)

data class WeatherInfo(
    @SerializedName("main") val main: String?,
    @SerializedName("description") val description: String?
)

data class WindInfo(
    @SerializedName("speed") val speed: Double?
)
