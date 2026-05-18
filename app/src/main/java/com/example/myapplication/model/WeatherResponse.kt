package com.example.myapplication.model

import com.google.gson.annotations.SerializedName

data class WeatherResponse(
    @SerializedName("cities") val cities: List<CityWeather>
)