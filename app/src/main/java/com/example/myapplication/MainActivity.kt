package com.example.myapplication

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CityAdapter
    private val cityList = mutableListOf<City>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = GridLayoutManager(this, 2)

        adapter = CityAdapter(cityList) { city -> confirmDelete(city) }
        recyclerView.adapter = adapter

        findViewById<Button>(R.id.btnAddCity).setOnClickListener {
            showAddCityDialog()
        }

        addCity("Москва")
        addCity("Санкт-Петербург")
    }

    private fun showAddCityDialog() {
        val input = EditText(this).apply { hint = "Название города" }
        AlertDialog.Builder(this)
            .setTitle("Добавить город")
            .setView(input)
            .setPositiveButton("Добавить") { _, _ ->
                val name = input.text.toString().trim()
                if (name.isNotEmpty()) addCity(name)
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun addCity(name: String) {
        val weather = generateRandomWeather()
        val newCity = City(name, weather.temp, weather.desc, weather.icon)
        cityList.add(newCity)
        adapter.notifyItemInserted(cityList.size - 1)
    }

    private fun confirmDelete(city: City) {
        AlertDialog.Builder(this)
            .setTitle("Удалить город?")
            .setMessage("Вы уверены, что хотите удалить «${city.name}»?")
            .setPositiveButton("Да, удалить") { _, _ -> removeCity(city) }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun removeCity(city: City) {
        val index = cityList.indexOf(city)
        if (index != -1) {
            cityList.removeAt(index)
            adapter.notifyItemRemoved(index)
        }
    }

    private fun generateRandomWeather(): WeatherData {
        val weatherTypes = listOf(
            WeatherType("Солнечно", R.drawable.ic_sunny),
            WeatherType("Облачно", R.drawable.ic_cloudy),
            WeatherType("Дождь", R.drawable.ic_rainy),
            WeatherType("Снег", R.drawable.ic_snowy),
            WeatherType("Ветрено", R.drawable.ic_windy),
            WeatherType("Туман", R.drawable.ic_foggy)
        )

        val weather = weatherTypes.random()
        val temp = when {
            weather.desc == "Солнечно" -> (15..35).random()
            weather.desc == "Снег" -> (-15..0).random()
            weather.desc in listOf("Дождь", "Туман", "Облачно") -> (5..15).random()
            else -> (10..25).random()
        }

        return WeatherData(temp, weather.desc, weather.icon)
    }
    data class WeatherType(val desc: String, val icon: Int)
    data class WeatherData(val temp: Int, val desc: String, val icon: Int)
}