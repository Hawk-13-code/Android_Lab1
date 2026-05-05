package com.example.myapplication

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object PrefsManager {
    private const val PREFS_NAME = "weather_prefs"
    private const val KEY_CITIES = "cities_list"

    private fun getPrefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun saveCities(context: Context, cities: List<City>) {
        val json = Gson().toJson(cities)
        getPrefs(context).edit().putString(KEY_CITIES, json).apply()
    }

    fun loadCities(context: Context): MutableList<City> {
        val json = getPrefs(context).getString(KEY_CITIES, null)
        return if (json != null) {
            val type = object : TypeToken<MutableList<City>>() {}.type
            Gson().fromJson(json, type)
        } else {
            mutableListOf()
        }
    }
}