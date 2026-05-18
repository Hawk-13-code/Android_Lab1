package com.example.myapplication
import java.io.Serializable
data class City(
    val name: String,
    val temperature: Int,
    val description: String,
    val iconResId: Int,
    var isFavorite: Boolean = false
) : Serializable