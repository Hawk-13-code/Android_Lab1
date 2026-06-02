package com.example.myapplication.data.repository

import android.content.Context
import com.example.myapplication.R
import com.example.myapplication.data.local.DbCity
import com.example.myapplication.data.local.DbForecast
import com.example.myapplication.data.local.WeatherDao
import com.example.myapplication.model.CityWeather
import com.example.myapplication.model.Forecast
import com.example.myapplication.model.WeatherResponse
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton
import com.example.myapplication.PrefsManager
import com.example.myapplication.NotificationHelper
@Singleton
class WeatherRepository @Inject constructor(
    private val dao: WeatherDao,
    @ApplicationContext private val context: Context
) {
    fun getCitiesFlow(): Flow<List<DbCity>> = dao.getAllCities()

    fun getForecastsFlow(cityId: String? = null): Flow<List<DbForecast>> {
        return if (cityId != null) {
            dao.getForecastsByCity(cityId)
        } else {
            dao.getAllForecasts()
        }
    }

    suspend fun syncWeather() {
        try {
            val jsonString = context.resources.openRawResource(R.raw.weather_data)
                .bufferedReader().use { it.readText() }
            val response = Gson().fromJson(jsonString, WeatherResponse::class.java)

            val serverCities = response.cities.map { it.toDbCity() }
            val serverForecasts = response.cities.flatMap { city ->
                city.forecasts.map { it.toDbForecast(city.id) }
            }


            val oldForecasts = dao.getAllForecasts().first()


            val favoriteCityIds = PrefsManager.loadFavorites(context)


            for (cityId in favoriteCityIds) {
                val oldCityForecasts = oldForecasts.filter { it.cityId == cityId }
                val newCityForecasts = serverForecasts.filter { it.cityId == cityId }

                for (newForecast in newCityForecasts) {
                    val oldForecast = oldCityForecasts.find { it.date == newForecast.date }
                    if (oldForecast != null) {
                        val tempDiff = kotlin.math.abs(newForecast.temperature - oldForecast.temperature)
                        if (tempDiff >= 2.0 || oldForecast.description != newForecast.description) {
                            val cityName = serverCities.find { it.id == cityId }?.name ?: "Неизвестный город"
                            NotificationHelper.showWeatherChangeNotification(
                                context,
                                cityName,
                                oldForecast.temperature,
                                newForecast.temperature,
                                newForecast.description
                            )
                        }
                    }
                }
            }


            val localCities = dao.getAllCities().first()
            val localCityIds = localCities.map { it.id }.toSet()
            val serverCityIds = serverCities.map { it.id }.toSet()
            val localOnlyCities = localCities.filter { it.id !in serverCityIds }
            val citiesToSave = serverCities + localOnlyCities
            val localForecasts = dao.getAllForecasts().first()
                .filter { it.cityId in localCityIds && it.cityId !in serverCityIds }
            val forecastsToSave = serverForecasts + localForecasts

            dao.insertCities(citiesToSave)
            dao.insertForecasts(forecastsToSave)

        } catch (e: Exception) {
            android.util.Log.e("DEBUG_SYNC_ERR", "Ошибка: ${e.message}", e)
            throw Exception("Ошибка синхронизации: ${e.message}")
        }
    }

    suspend fun addCity(name: String) {
        val cityId = name.hashCode().toString()
        val dbCity = DbCity(id = cityId, name = name)

        val existingForecasts = dao.getAllForecasts().first()
        val availableDates = existingForecasts.map { it.date }.distinct().sorted()

        val forecastDates = if (availableDates.size >= 3) {
            availableDates.take(3)
        } else {
            listOf("2026-04-06", "2026-04-07", "2026-04-08")
        }

        val forecasts = generateRandomForecasts(cityId, forecastDates)

        dao.insertCities(listOf(dbCity))
        dao.insertForecasts(forecasts)
    }

    private fun generateRandomForecasts(cityId: String, dates: List<String>): List<DbForecast> {
        val weatherTypes = listOf(
            "Солнечно" to "01d",
            "Облачно" to "03d",
            "Дождь" to "10d",
            "Снег" to "13d",
            "Ветрено" to "wind",
            "Туман" to "50d"
        )

        return dates.mapIndexed { index, date ->
            val (desc, icon) = weatherTypes.random()
            val temp = when {
                desc == "Солнечно" -> (15..30).random()
                desc == "Снег" -> (-10..0).random()
                else -> (5..20).random()
            }
            DbForecast(
                id = "${cityId}_$index",
                cityId = cityId,
                date = date,
                temperature = temp.toDouble(),
                description = desc,
                icon = icon,
                humidity = (40..90).random(),
                windSpeed = (0..15).random().toDouble(),
                windDirection = (0..360).random(),
                pressure = (980..1030).random()
            )
        }
    }

    fun getForecastsByCity(cityId: String): Flow<List<DbForecast>> {
        return dao.getForecastsByCity(cityId)
    }

    suspend fun removeCity(cityId: String) {
        dao.deleteForecastsByCity(cityId)
        dao.deleteCity(cityId)
    }
}

fun CityWeather.toDbCity() = DbCity(id, name)
fun Forecast.toDbForecast(cityId: String) = DbForecast(
    id = id,
    cityId = cityId,
    date = date,
    temperature = temperature,
    description = description,
    icon = icon,
    humidity = humidity,
    windSpeed = windSpeed,
    windDirection = windDirection,
    pressure = pressure
)