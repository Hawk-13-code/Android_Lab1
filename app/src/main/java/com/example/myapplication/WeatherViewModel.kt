package com.example.myapplication

import android.app.Application
import android.content.Context
import androidx.lifecycle.*
import com.example.myapplication.model.CityWeather
import com.example.myapplication.network.RetrofitClient
import kotlinx.coroutines.launch

class WeatherViewModel(application: Application) : AndroidViewModel(application) {

    private val _cities = MutableLiveData<List<CityWeather>>()
    val cities: LiveData<List<CityWeather>> = _cities

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun loadWeather(context: Context) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val serverCities = RetrofitClient.instance.getWeatherForecast().cities
                val localCities = PrefsManager.loadCitiesWeather(context)
                val merged = mergeCities(serverCities, localCities)

                PrefsManager.saveCitiesWeather(context, merged)
                _cities.value = merged

            } catch (e: Exception) {
                val localFallback = PrefsManager.loadCitiesWeather(context)
                if (localFallback.isNotEmpty()) {
                    _cities.value = localFallback
                    _error.value = "Нет соединения. Показаны сохранённые данные."
                } else {
                    _error.value = "Ошибка сети: ${e.message}"
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun mergeCities(
        server: List<CityWeather>,
        local: List<CityWeather>
    ): List<CityWeather> {
        val serverMap = server.associateBy { it.id }
        val result = serverMap.values.toMutableList()

        local.forEach { localCity ->
            if (localCity.id !in serverMap) {
                result.add(localCity)
            }
        }
        return result
    }

    fun removeCity(context: Context, city: CityWeather) {
        val current = _cities.value?.toMutableList() ?: return
        val updated = current.filter { it.id != city.id }
        _cities.value = updated
        PrefsManager.saveCitiesWeather(context, updated)
    }
}