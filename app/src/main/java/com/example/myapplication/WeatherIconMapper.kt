package com.example.myapplication

import androidx.annotation.DrawableRes

object WeatherIconMapper {

    @DrawableRes
    fun getIconResId(iconCode: String?): Int {
        val code = iconCode?.lowercase()?.trim() ?: return R.drawable.ic_launcher_foreground

        return when {
            code in listOf("01d", "01n", "ясно", "солнечно", "clear", "sunny") -> R.drawable.ic_sunny

            code in listOf("02d", "02n", "переменная облачность", "partly cloudy", "небольшая облачность") -> R.drawable.ic_cloudy

            code in listOf("03d", "03n", "04d", "04n", "облачно", "cloudy", "пасмурно") -> R.drawable.ic_cloudy

            code in listOf("09d", "09n", "10d", "10n", "дождь", "небольшой дождь", "rain", "ливень") -> R.drawable.ic_rainy

            code in listOf("11d", "11n", "гроза", "thunder") -> R.drawable.ic_rainy

            code in listOf("13d", "13n", "снег", "snow") -> R.drawable.ic_snowy

            code in listOf("50d", "50n", "туман", "fog", "дымка") -> R.drawable.ic_foggy

            code in listOf("wind", "ветер", "ветрено") -> R.drawable.ic_windy

            else -> R.drawable.ic_launcher_foreground
        }
    }
    @DrawableRes
    fun getIconResIdByDescription(description: String?): Int {
        val desc = description?.lowercase()?.trim() ?: return R.drawable.ic_launcher_foreground

        return when {
            desc.contains("солнечно") || desc.contains("ясно") || desc.contains("clear") -> R.drawable.ic_sunny
            desc.contains("переменная облачность") || desc.contains("partly") -> R.drawable.ic_cloudy
            desc.contains("облачно") || desc.contains("cloudy") || desc.contains("пасмурно") -> R.drawable.ic_cloudy
            desc.contains("дождь") || desc.contains("rain") || desc.contains("ливень") -> R.drawable.ic_rainy
            desc.contains("снег") || desc.contains("snow") -> R.drawable.ic_snowy
            desc.contains("ветер") || desc.contains("wind") || desc.contains("ветрено") -> R.drawable.ic_windy
            desc.contains("туман") || desc.contains("fog") -> R.drawable.ic_foggy
            else -> R.drawable.ic_launcher_foreground
        }
    }
}