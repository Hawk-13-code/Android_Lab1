package com.example.myapplication.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WeatherDao {
    @Query("SELECT * FROM cities")
    fun getAllCities(): Flow<List<DbCity>>

    @Query("SELECT * FROM forecasts WHERE cityId = :cityId ORDER BY date")
    fun getForecastsByCity(cityId: String): Flow<List<DbForecast>>

    @Query("SELECT * FROM forecasts ORDER BY date DESC")
    fun getAllForecasts(): Flow<List<DbForecast>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCities(cities: List<DbCity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertForecasts(forecasts: List<DbForecast>)

    @Query("DELETE FROM forecasts WHERE cityId = :cityId")
    suspend fun deleteForecastsByCity(cityId: String)

    @Query("DELETE FROM cities WHERE id = :cityId")
    suspend fun deleteCity(cityId: String)
}