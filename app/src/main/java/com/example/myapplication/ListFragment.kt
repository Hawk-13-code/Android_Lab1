package com.example.myapplication

import android.app.Application
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.myapplication.model.CityWeather
import com.example.myapplication.model.Forecast
import com.google.android.material.snackbar.Snackbar

class ListFragment : Fragment(R.layout.fragment_list) {

    private lateinit var recyclerView: RecyclerView
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var adapter: CityAdapter

    private val viewModel: WeatherViewModel by viewModels {
        WeatherViewModelFactory(requireContext().applicationContext as Application)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyclerView)
        swipeRefresh = view.findViewById(R.id.swipeRefresh)
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)

        adapter = CityAdapter(
            cities = mutableListOf(),
            onDeleteClick = { city -> confirmDelete(city) },
            onItemClick = { city -> openDetail(city) }
        )
        recyclerView.adapter = adapter

        viewModel.cities.observe(viewLifecycleOwner) { cities ->
            val sorted = cities.sortedByDescending { city ->
                PrefsManager.isFavorite(requireContext(), city.id)
            }
            adapter.submitList(sorted)
            swipeRefresh.isRefreshing = false
        }

        viewModel.error.observe(viewLifecycleOwner) { errorMsg ->
            errorMsg?.let {
                Snackbar.make(view, it, Snackbar.LENGTH_LONG)
                    .setAction("Повторить") { viewModel.loadWeather(requireContext()) }
                    .show()
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            swipeRefresh.isRefreshing = loading
        }

        swipeRefresh.setOnRefreshListener {
            viewModel.loadWeather(requireContext())
        }

        view.findViewById<Button>(R.id.btnAddCity).setOnClickListener {
            showAddCityDialog()
        }

        viewModel.loadWeather(requireContext())
    }

    private fun openDetail(city: CityWeather) {
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
                    val newCity = generateRandomCityWeather(name)
                    val current = PrefsManager.loadCitiesWeather(requireContext()).toMutableList()
                    current.add(newCity)
                    PrefsManager.saveCitiesWeather(requireContext(), current)
                    viewModel.loadWeather(requireContext())
                    Snackbar.make(requireView(), "Город $name добавлен", Snackbar.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun generateRandomCityWeather(name: String): CityWeather {
        val weatherTypes = listOf(
            "Солнечно" to "01d",
            "Облачно" to "03d",
            "Дождь" to "10d",
            "Снег" to "13d",
            "Ветрено" to "wind",
            "Туман" to "50d"
        )

        val dates = generateDates(3)

        val forecasts = dates.mapIndexed { index, date ->
            val (desc, icon) = weatherTypes.random()
            val temp = when {
                desc == "Солнечно" -> (15..30).random()
                desc == "Снег" -> (-10..0).random()
                else -> (5..20).random()
            }
            Forecast(
                id = "${name.hashCode()}_$index",
                date = date,
                temperature = temp.toDouble(),
                description = desc,
                icon = icon
            )
        }

        return CityWeather(
            id = name.hashCode().toString(),
            name = name,
            forecasts = forecasts
        )
    }

    private fun generateDates(count: Int): List<String> {
        val base = System.currentTimeMillis()
        return (0 until count).map { offset ->
            val dayMillis = base + offset * 24 * 60 * 60 * 1000
            java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                .format(java.util.Date(dayMillis))
        }
    }

    private fun confirmDelete(city: CityWeather) {
        AlertDialog.Builder(requireContext())
            .setTitle("Удалить город?")
            .setMessage("Вы уверены, что хотите удалить «${city.name}»?")
            .setPositiveButton("Да") { _, _ ->
                viewModel.removeCity(requireContext(), city)
                Snackbar.make(requireView(), "Город удалён", Snackbar.LENGTH_SHORT).show()
            }
            .setNegativeButton("Отмена", null)
            .show()
    }
}

class WeatherViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WeatherViewModel::class.java)) {
            return WeatherViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}