package com.example.myapplication.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Forecast(
    @SerializedName("id") val id: String,
    @SerializedName("date") val date: String,
    @SerializedName("temperature") val temperature: Double,
    @SerializedName("description") val description: String,
    @SerializedName("icon") val icon: String
) : Serializable