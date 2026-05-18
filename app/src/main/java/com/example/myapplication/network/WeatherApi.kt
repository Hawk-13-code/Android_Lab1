package com.example.myapplication.network

import com.example.myapplication.model.WeatherResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApi {
    @GET("weather-forecast")
    suspend fun getWeatherForecast(@Query("city") city: String? = null): WeatherResponse
}