package com.example.myapplication.data.repository

import com.example.myapplication.data.local.WeatherDao
import com.example.myapplication.data.local.DbCity
import com.example.myapplication.data.local.DbForecast
import com.example.myapplication.model.CityWeather
import com.example.myapplication.model.Forecast
import com.example.myapplication.network.WeatherApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeatherRepository @Inject constructor(
    private val dao: WeatherDao,
    private val api: WeatherApi
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
            val response = api.getWeatherForecast()

            val serverCities = response.cities.map { it.toDbCity() }
            val serverForecasts = response.cities.flatMap { city ->
                city.forecasts.map { it.toDbForecast(city.id) }
            }

            android.util.Log.d("DEBUG_SYNC_IN", "Пришло городов: ${serverCities.size}")
            android.util.Log.d("DEBUG_SYNC_IN", "Пришло прогнозов: ${serverForecasts.size}")

            val localCities = dao.getAllCities().first()
            val localCityIds = localCities.map { it.id }.toSet()
            val serverCityIds = serverCities.map { it.id }.toSet()

            val localOnlyCities = localCities.filter { it.id !in serverCityIds }
            val citiesToSave = serverCities + localOnlyCities

            val localForecasts = dao.getAllForecasts().first()
                .filter { it.cityId in localCityIds && it.cityId !in serverCityIds }
            val forecastsToSave = serverForecasts + localForecasts

            android.util.Log.d("DEBUG_SYNC_SAVE", "Сохраняем городов: ${citiesToSave.size}")
            android.util.Log.d("DEBUG_SYNC_SAVE", "Сохраняем прогнозов: ${forecastsToSave.size}")

            dao.insertCities(citiesToSave)
            dao.insertForecasts(forecastsToSave)

            android.util.Log.d("DEBUG_SYNC_OUT", " Успешно сохранено!")

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

        android.util.Log.d("DEBUG_ADD", " Город добавлен: $name (ID: $cityId, даты: $forecastDates)")
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
                icon = icon
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
    icon = icon
)