package com.example.myapplication.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cities")
data class DbCity(
    @PrimaryKey val id: String,
    val name: String
)