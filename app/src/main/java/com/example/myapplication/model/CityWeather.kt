package com.example.myapplication.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class CityWeather(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("forecasts") val forecasts: List<Forecast>
) : Serializable