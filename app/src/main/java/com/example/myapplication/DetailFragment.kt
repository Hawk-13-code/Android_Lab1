package com.example.myapplication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.ui.ForecastUiItem
import com.example.myapplication.ui.WeatherViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DetailFragment : Fragment() {

    private val viewModel: WeatherViewModel by viewModels()
    private var cityId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            cityId = DetailFragmentArgs.fromBundle(it).cityId
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvCityTitle = view.findViewById<TextView>(R.id.tvCityTitle)
        val tvTodayTemp = view.findViewById<TextView>(R.id.tvTodayTemp)
        val tvTodayDesc = view.findViewById<TextView>(R.id.tvTodayDesc)
        val imgTodayIcon = view.findViewById<ImageView>(R.id.imgTodayIcon)
        val btnFavorite = view.findViewById<ImageButton>(R.id.btnFavorite)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewForecast)

        val tvHumidity = view.findViewById<TextView>(R.id.tvHumidity)
        val tvWind = view.findViewById<TextView>(R.id.tvWind)
        val tvWindDir = view.findViewById<TextView>(R.id.tvWindDir)
        val tvPressure = view.findViewById<TextView>(R.id.tvPressure)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        cityId?.let { id ->
            viewLifecycleOwner.lifecycleScope.launch {
                viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    viewModel.getCityDetails(id).collect { forecasts ->
                        if (forecasts.isNotEmpty()) {
                            val first = forecasts.first()

                            tvCityTitle.text = first.cityName
                            tvTodayTemp.text = "${first.temperature}°C"
                            tvTodayDesc.text = first.description


                            tvHumidity.text = "${first.humidity}%"
                            tvWind.text = "${first.windSpeed} м/с"
                            tvWindDir.text = getWindDirection(first.windDirection)
                            tvPressure.text = "${first.pressure} гПа"

                            val iconResId = WeatherIconMapper.getIconResId(first.icon)
                                .takeIf { it != R.drawable.ic_launcher_foreground }
                                ?: WeatherIconMapper.getIconResIdByDescription(first.description)
                            imgTodayIcon.setImageResource(iconResId)

                            updateFavoriteButton(btnFavorite, id)
                            btnFavorite.setOnClickListener {
                                val isNowFavorite = PrefsManager.toggleFavorite(requireContext(), id)
                                updateFavoriteButton(btnFavorite, id)
                                val action = if (isNowFavorite) "добавлен в" else "убран из"
                                Snackbar.make(view, "Город $action избранное", Snackbar.LENGTH_SHORT).show()
                            }

                            val adapter = ForecastAdapter(forecasts)
                            recyclerView.adapter = adapter
                        }
                    }
                }
            }
        }
    }

    private fun updateFavoriteButton(btn: ImageButton, cityId: String) {
        val isFav = PrefsManager.isFavorite(requireContext(), cityId)
        btn.setImageResource(
            if (isFav) android.R.drawable.btn_star_big_on
            else android.R.drawable.btn_star_big_off
        )
    }

    private fun getWindDirection(degrees: Int): String {
        return when (degrees) {
            in 0..22 -> "С"
            in 23..67 -> "СВ"
            in 68..112 -> "В"
            in 113..157 -> "ЮВ"
            in 158..202 -> "Ю"
            in 203..247 -> "ЮЗ"
            in 248..292 -> "З"
            in 293..337 -> "СЗ"
            else -> "С"
        }
    }
}

class ForecastAdapter(
    private val forecasts: List<ForecastUiItem>
) : RecyclerView.Adapter<ForecastAdapter.ForecastViewHolder>() {

    class ForecastViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDate: TextView = view.findViewById(R.id.tvDate)
        val tvTemp: TextView = view.findViewById(R.id.tvTemp)
        val tvDesc: TextView = view.findViewById(R.id.tvDesc)
        val imgIcon: ImageView = view.findViewById(R.id.imgIcon)
        // Новые поля
        val tvHumidity: TextView = view.findViewById(R.id.tvHumidity)
        val tvWind: TextView = view.findViewById(R.id.tvWind)
        val tvWindDir: TextView = view.findViewById(R.id.tvWindDir)
        val tvPressure: TextView = view.findViewById(R.id.tvPressure)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ForecastViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_forecast, parent, false)
        return ForecastViewHolder(view)
    }

    override fun onBindViewHolder(holder: ForecastViewHolder, position: Int) {
        val forecast = forecasts[position]
        holder.tvDate.text = forecast.date
        holder.tvTemp.text = "${forecast.temperature}°C"
        holder.tvDesc.text = forecast.description

        holder.tvHumidity.text = "💧 ${forecast.humidity}%"
        holder.tvWind.text = "💨 ${forecast.windSpeed} м/с"
        holder.tvWindDir.text = "🧭 ${getWindDirection(forecast.windDirection)}"
        holder.tvPressure.text = "📊 ${forecast.pressure} гПа"

        val iconResId = WeatherIconMapper.getIconResId(forecast.icon)
            .takeIf { it != R.drawable.ic_launcher_foreground }
            ?: WeatherIconMapper.getIconResIdByDescription(forecast.description)
        holder.imgIcon.setImageResource(iconResId)
    }

    override fun getItemCount(): Int = forecasts.size
}

private fun getWindDirection(degrees: Int): String {
    return when (degrees) {
        in 0..22 -> "С"
        in 23..67 -> "СВ"
        in 68..112 -> "В"
        in 113..157 -> "ЮВ"
        in 158..202 -> "Ю"
        in 203..247 -> "ЮЗ"
        in 248..292 -> "З"
        in 293..337 -> "СЗ"
        else -> "С"
    }
}