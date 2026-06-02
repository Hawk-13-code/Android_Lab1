package com.example.myapplication.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "forecasts",
    foreignKeys = [ForeignKey(
        entity = DbCity::class,
        parentColumns = ["id"],
        childColumns = ["cityId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class DbForecast(
    @PrimaryKey val id: String,
    val cityId: String,
    val date: String,
    val temperature: Double,
    val description: String,
    val icon: String,
    val humidity: Int = 0,
    val windSpeed: Double = 0.0,
    val windDirection: Int = 0,
    val pressure: Int = 0,
    val updatedAt: Long = System.currentTimeMillis()
)