package com.example.myapplication

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.myapplication.ui.ForecastUiItem
import com.example.myapplication.ui.SortType
import com.example.myapplication.ui.WeatherUiState
import com.example.myapplication.ui.WeatherViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ListFragment : Fragment(R.layout.fragment_list) {

    private lateinit var recyclerView: RecyclerView
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var adapter: CityAdapter
    private lateinit var spinnerSort: Spinner
    private lateinit var spinnerCity: Spinner
    private lateinit var spinnerDay: Spinner

    private val viewModel: WeatherViewModel by viewModels()

    private var uniqueCities: List<Pair<String, String>> = emptyList()

    private var isSpinnerUpdating = false

    private var selectedCityId: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyclerView)
        swipeRefresh = view.findViewById(R.id.swipeRefresh)
        spinnerSort = view.findViewById(R.id.spinnerSort)
        spinnerCity = view.findViewById(R.id.spinnerCity)
        spinnerDay = view.findViewById(R.id.spinnerDay)

        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)

        adapter = CityAdapter(
            items = mutableListOf(),
            onDeleteClick = { item -> confirmDelete(item) },
            onItemClick = { item -> openDetail(item) }
        )
        recyclerView.adapter = adapter

        setupCitySpinnerListener()

        setupSortSpinner()

        setupDaySpinner()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is WeatherUiState.Loading -> {
                            swipeRefresh.isRefreshing = true
                        }
                        is WeatherUiState.Success -> {
                            swipeRefresh.isRefreshing = false

                            val sortedByFavorite = state.data.sortedByDescending { item ->
                                PrefsManager.isFavorite(requireContext(), item.cityId)
                            }

                            val uniqueItems = sortedByFavorite.distinctBy { it.cityId }

                            adapter.submitList(uniqueItems)

                            updateCitySpinner(state.data)
                        }
                        is WeatherUiState.Error -> {
                            swipeRefresh.isRefreshing = false
                            Snackbar.make(view, state.message, Snackbar.LENGTH_LONG)
                                .setAction("Повторить") { viewModel.syncData() }
                                .show()
                        }
                    }
                }
            }
        }

        swipeRefresh.setOnRefreshListener { viewModel.syncData() }

        view.findViewById<Button>(R.id.btnAddCity).setOnClickListener {
            showAddCityDialog()
        }

        viewModel.syncData()
    }

    private fun setupCitySpinnerListener() {
        val initialAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            arrayOf("Все города")
        )
        initialAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCity.adapter = initialAdapter

        spinnerCity.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                if (isSpinnerUpdating) return

                if (position == 0) {
                    selectedCityId = null
                    viewModel.setFilter(null)
                } else {
                    val cityPair = uniqueCities.getOrNull(position - 1)
                    selectedCityId = cityPair?.second
                    viewModel.setFilter(cityPair?.second)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {
                selectedCityId = null
                viewModel.setFilter(null)
            }
        }
    }

    private fun setupSortSpinner() {
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.sort_options,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerSort.adapter = adapter
        }

        spinnerSort.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val sortType = when (position) {
                    0 -> SortType.BY_NAME
                    1 -> SortType.TEMP_UP
                    2 -> SortType.TEMP_DOWN
                    else -> SortType.BY_NAME
                }
                viewModel.setSort(sortType)
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun setupDaySpinner() {
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.day_options,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerDay.adapter = adapter
        }

        spinnerDay.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                viewModel.setDay(position)  // 0=Сегодня, 1=Завтра, 2=Послезавтра
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // Синхронизируем спиннер с ViewModel
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.selectedDayIndex.collect { dayIndex ->
                    spinnerDay.setSelection(dayIndex, false)
                }
            }
        }
    }

    private fun updateCitySpinner(items: List<ForecastUiItem>) {
        uniqueCities = items
            .distinctBy { it.cityId }
            .map { it.cityName to it.cityId }
            .sortedBy { it.first }

        val displayList = arrayOf("Все города") + uniqueCities.map { it.first }.toTypedArray()

        isSpinnerUpdating = true

        val newAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            displayList
        )
        newAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCity.adapter = newAdapter

        spinnerCity.post {
            val positionToSelect = if (selectedCityId != null) {
                val index = uniqueCities.indexOfFirst { it.second == selectedCityId }
                if (index != -1) index + 1 else 0
            } else {
                0
            }

            spinnerCity.setSelection(positionToSelect, false)
            isSpinnerUpdating = false
        }
    }

    private fun openDetail(item: ForecastUiItem) {
        val action = ListFragmentDirections.actionListToDetail(cityId = item.cityId)
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
                    viewModel.addManualCity(name)
                    Snackbar.make(requireView(), "Город $name добавлен", Snackbar.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun confirmDelete(item: ForecastUiItem) {
        AlertDialog.Builder(requireContext())
            .setTitle("Удалить город?")
            .setMessage("Вы уверены, что хотите удалить «${item.cityName}»?")
            .setPositiveButton("Да") { _, _ ->
                viewModel.removeCity(item.cityId)
                Snackbar.make(requireView(), "Город удалён", Snackbar.LENGTH_SHORT).show()
            }
            .setNegativeButton("Отмена", null)
            .show()
    }
}