package com.example.myapplication

import android.content.Context
import android.content.SharedPreferences
import com.example.myapplication.model.CityWeather
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object PrefsManager {
    private const val PREFS_NAME = "weather_prefs"
    private const val KEY_CITIES_OLD = "cities_list"
    private const val KEY_CITIES_WEATHER = "cities_weather"
    private const val KEY_FAVORITES = "favorite_city_ids"  // ← НОВОЕ

    private fun getPrefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun saveCities(context: Context, cities: List<CityOld>) {
        val json = Gson().toJson(cities)
        getPrefs(context).edit().putString(KEY_CITIES_OLD, json).apply()
    }

    fun loadCities(context: Context): MutableList<CityOld> {
        val json = getPrefs(context).getString(KEY_CITIES_OLD, null)
        return if (json != null) {
            val type = object : TypeToken<MutableList<CityOld>>() {}.type
            Gson().fromJson(json, type)
        } else {
            mutableListOf()
        }
    }

    fun saveCitiesWeather(context: Context, cities: List<CityWeather>) {
        val json = Gson().toJson(cities)
        getPrefs(context).edit().putString(KEY_CITIES_WEATHER, json).apply()
    }

    fun loadCitiesWeather(context: Context): List<CityWeather> {
        val json = getPrefs(context).getString(KEY_CITIES_WEATHER, null)
        return if (json != null) {
            val type = object : TypeToken<List<CityWeather>>() {}.type
            Gson().fromJson(json, type) ?: emptyList()
        } else {
            emptyList()
        }
    }

    fun saveFavorites(context: Context, cityIds: Set<String>) {
        getPrefs(context).edit().putStringSet(KEY_FAVORITES, cityIds).apply()
    }

    fun loadFavorites(context: Context): Set<String> {
        return getPrefs(context).getStringSet(KEY_FAVORITES, emptySet()) ?: emptySet()
    }

    fun toggleFavorite(context: Context, cityId: String): Boolean {
        val favorites = loadFavorites(context).toMutableSet()
        val isNowFavorite = if (cityId in favorites) {
            favorites.remove(cityId)
            false
        } else {
            favorites.add(cityId)
            true
        }
        saveFavorites(context, favorites)
        return isNowFavorite
    }

    fun isFavorite(context: Context, cityId: String): Boolean {
        return loadFavorites(context).contains(cityId)
    }

    fun migrateOldCities(context: Context): List<CityWeather> {
        val oldCities = loadCities(context)
        return oldCities.map { old ->
            CityWeather(
                id = old.name.hashCode().toString(),
                name = old.name,
                forecasts = listOf(
                    com.example.myapplication.model.Forecast(
                        id = "${old.name.hashCode()}_forecast",
                        date = "2026-01-01",
                        temperature = old.temperature.toDouble(),
                        description = old.description,
                        icon = "01d"
                    )
                )
            )
        }
    }
}