package com.example.myapplication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import kotlin.random.Random

class ListFragment : Fragment(R.layout.fragment_list) {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CityAdapter
    private val cityList = mutableListOf<City>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)

        if (cityList.isEmpty()) {
            cityList.addAll(PrefsManager.loadCities(requireContext()))
        }
        sortCities()

        adapter = CityAdapter(cityList,
            onDeleteClick = { city -> confirmDelete(city) },
            onItemClick = { city -> openDetail(city) }
        )
        recyclerView.adapter = adapter

        view.findViewById<Button>(R.id.btnAddCity).setOnClickListener {
            showAddCityDialog()
        }

        if (cityList.isEmpty()) {
            addCity("Москва")
            addCity("Санкт-Петербург")
        }
    }

    private fun sortCities() {
        cityList.sortByDescending { it.isFavorite }
    }

    private fun openDetail(city: City) {
        Snackbar.make(requireView(), "Выбран город: ${city.name}", Snackbar.LENGTH_SHORT).show()
        val action = ListFragmentDirections.actionListToDetail(city)
        findNavController().navigate(action)
    }

    private fun showAddCityDialog() {
        val input = EditText(requireContext()).apply { hint = "Название города" }
        AlertDialog.Builder(requireContext())
            .setTitle("Добавить город")
            .setView(input)
            .setPositiveButton("Добавить") { _, _ ->
                val name = input.text.toString().trim()
                if (name.isNotEmpty()) {
                    addCity(name)
                    Snackbar.make(requireView(), "Город $name добавлен", Snackbar.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun addCity(name: String) {
        val weather = generateRandomWeather()
        cityList.add(City(name, weather.temp, weather.desc, weather.icon, false))
        sortCities()
        adapter.notifyDataSetChanged()
        PrefsManager.saveCities(requireContext(), cityList)
    }

    private fun confirmDelete(city: City) {
        AlertDialog.Builder(requireContext())
            .setTitle("Удалить город?")
            .setMessage("Вы уверены, что хотите удалить «${city.name}»?")
            .setPositiveButton("Да") { _, _ ->
                val index = cityList.indexOf(city)
                if (index != -1) {
                    cityList.removeAt(index)
                    adapter.notifyItemRemoved(index)
                    PrefsManager.saveCities(requireContext(), cityList)
                    Snackbar.make(requireView(), "Город удалён", Snackbar.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun generateRandomWeather(): WeatherData {
        val types = listOf(
            WeatherType("Солнечно", R.drawable.ic_sunny),
            WeatherType("Облачно", R.drawable.ic_cloudy),
            WeatherType("Дождь", R.drawable.ic_rainy),
            WeatherType("Снег", R.drawable.ic_snowy),
            WeatherType("Ветрено", R.drawable.ic_windy),
            WeatherType("Туман", R.drawable.ic_foggy)
        )
        val w = types.random()
        val temp = when {
            w.desc == "Солнечно" -> (15..35).random()
            w.desc == "Снег" -> (-15..0).random()
            else -> (5..20).random()
        }
        return WeatherData(temp, w.desc, w.icon)
    }

    data class WeatherType(val desc: String, val icon: Int)
    data class WeatherData(val temp: Int, val desc: String, val icon: Int)
}