package com.example.myapplication.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Forecast(
    @SerializedName("id") val id: String,
    @SerializedName("date") val date: String,
    @SerializedName("temperature") val temperature: Double,
    @SerializedName("description") val description: String,
    @SerializedName("icon") val icon: String,
    @SerializedName("humidity") val humidity: Int = 0,
    @SerializedName("wind_speed") val windSpeed: Double = 0.0,
    @SerializedName("wind_deg") val windDirection: Int = 0,
    @SerializedName("pressure") val pressure: Int = 0
) : Serializable