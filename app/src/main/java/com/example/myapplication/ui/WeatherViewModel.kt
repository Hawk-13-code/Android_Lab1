package com.example.myapplication.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.repository.WeatherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val repository: WeatherRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    private val _filterCityId = MutableStateFlow<String?>(null)
    private val _sortBy = MutableStateFlow<SortType>(SortType.BY_NAME)
    private val _selectedDayIndex = MutableStateFlow(0)

    val selectedDayIndex: StateFlow<Int> = _selectedDayIndex.asStateFlow()

    init {
        observeData()
        syncData()
    }

    private fun observeData() {
        viewModelScope.launch {
            combine(
                repository.getCitiesFlow(),
                repository.getForecastsFlow(),
                _filterCityId,
                _sortBy,
                _selectedDayIndex
            ) { cities, forecasts, filterCityId, sortType, dayIndex ->

                val uniqueDates = forecasts.map { it.date }.distinct().sorted()
                val targetDate = uniqueDates.getOrNull(dayIndex) ?: uniqueDates.firstOrNull()

                val dayForecasts = if (targetDate != null) {
                    forecasts.filter { it.date == targetDate }
                } else forecasts

                val uiData = dayForecasts.mapNotNull { forecast ->
                    val city = cities.find { it.id == forecast.cityId }
                    city?.let {
                        ForecastUiItem(
                            cityId = it.id,
                            cityName = it.name,
                            forecastId = forecast.id,
                            date = forecast.date,
                            temperature = forecast.temperature,
                            description = forecast.description,
                            icon = forecast.icon,
                            humidity = forecast.humidity,
                            windSpeed = forecast.windSpeed,
                            windDirection = forecast.windDirection,
                            pressure = forecast.pressure
                        )
                    }
                }

                val filtered = if (filterCityId != null) {
                    uiData.filter { it.cityId == filterCityId }
                } else uiData

                val sorted = when (sortType) {
                    SortType.BY_NAME -> filtered.sortedBy { it.cityName }
                    SortType.TEMP_UP -> filtered.sortedBy { it.temperature }
                    SortType.TEMP_DOWN -> filtered.sortedByDescending { it.temperature }
                }

                WeatherUiState.Success(sorted)
            }
                .catch { e ->
                    _uiState.value = WeatherUiState.Error(e.message ?: "Ошибка")
                }
                .collect { state ->
                    _uiState.value = state
                }
        }
    }

    fun getCityDetails(cityId: String): Flow<List<ForecastUiItem>> {
        return combine(
            repository.getCitiesFlow(),
            repository.getForecastsByCity(cityId)
        ) { cities, forecasts ->
            forecasts.mapNotNull { forecast ->
                val city = cities.find { it.id == forecast.cityId }
                city?.let {
                    ForecastUiItem(
                        cityId = it.id,
                        cityName = it.name,
                        forecastId = forecast.id,
                        date = forecast.date,
                        temperature = forecast.temperature,
                        description = forecast.description,
                        icon = forecast.icon,
                        humidity = forecast.humidity,
                        windSpeed = forecast.windSpeed,
                        windDirection = forecast.windDirection,
                        pressure = forecast.pressure
                    )
                }
            }.sortedBy { it.date }
        }
    }

    fun syncData() {
        viewModelScope.launch {
            _uiState.value = WeatherUiState.Loading
            try {
                repository.syncWeather()
            } catch (e: Exception) {
                _uiState.value = WeatherUiState.Error("Нет соединения. Показаны сохранённые данные.")
            }
        }
    }

    fun setFilter(cityId: String?) { _filterCityId.value = cityId }
    fun setSort(type: SortType) { _sortBy.value = type }
    fun setDay(index: Int) { _selectedDayIndex.value = index }

    fun removeCity(cityId: String) {
        viewModelScope.launch {
            repository.removeCity(cityId)
        }
    }

    fun addManualCity(name: String) {
        viewModelScope.launch {
            repository.addCity(name)
        }
    }
}

data class ForecastUiItem(
    val cityId: String,
    val cityName: String,
    val forecastId: String,
    val date: String,
    val temperature: Double,
    val description: String,
    val icon: String,
    val humidity: Int = 0,
    val windSpeed: Double = 0.0,
    val windDirection: Int = 0,
    val pressure: Int = 0
)

sealed class WeatherUiState {
    object Loading : WeatherUiState()
    data class Success(val data: List<ForecastUiItem>) : WeatherUiState()
    data class Error(val message: String) : WeatherUiState()
}

enum class SortType { BY_NAME, TEMP_UP, TEMP_DOWN }