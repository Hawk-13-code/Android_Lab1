package com.example.myapplication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.model.CityWeather
import com.example.myapplication.model.Forecast
import com.google.android.material.snackbar.Snackbar

class DetailFragment : Fragment() {

    private var city: CityWeather? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            city = it.getSerializable("city") as? CityWeather
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

        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        city?.let { cityData ->
            tvCityTitle.text = cityData.name

            val today = cityData.forecasts.firstOrNull()
            tvTodayTemp.text = today?.let { "${it.temperature}°C" } ?: "N/A"
            tvTodayDesc.text = today?.description ?: "Нет данных"

            val iconResId = WeatherIconMapper.getIconResId(today?.icon)
                .takeIf { it != R.drawable.ic_launcher_foreground }
                ?: WeatherIconMapper.getIconResIdByDescription(today?.description)
            imgTodayIcon.setImageResource(iconResId)

            updateFavoriteButton(btnFavorite, cityData.id)
            btnFavorite.setOnClickListener {
                val isNowFavorite = PrefsManager.toggleFavorite(requireContext(), cityData.id)
                updateFavoriteButton(btnFavorite, cityData.id)
                val action = if (isNowFavorite) "добавлен в" else "убран из"
                Snackbar.make(view, "Город $action избранное", Snackbar.LENGTH_SHORT).show()
            }

            val adapter = ForecastAdapter(cityData.forecasts)
            recyclerView.adapter = adapter
        }
    }

    private fun updateFavoriteButton(btn: ImageButton, cityId: String) {
        val isFav = PrefsManager.isFavorite(requireContext(), cityId)
        btn.setImageResource(
            if (isFav) android.R.drawable.btn_star_big_on
            else android.R.drawable.btn_star_big_off
        )
    }
}

class ForecastAdapter(
    private val forecasts: List<Forecast>
) : RecyclerView.Adapter<ForecastAdapter.ForecastViewHolder>() {

    class ForecastViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDate: TextView = view.findViewById(R.id.tvDate)
        val tvTemp: TextView = view.findViewById(R.id.tvTemp)
        val tvDesc: TextView = view.findViewById(R.id.tvDesc)
        val imgIcon: ImageView = view.findViewById(R.id.imgIcon)
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

        val iconResId = WeatherIconMapper.getIconResId(forecast.icon)
            .takeIf { it != R.drawable.ic_launcher_foreground }
            ?: WeatherIconMapper.getIconResIdByDescription(forecast.description)
        holder.imgIcon.setImageResource(iconResId)
    }

    override fun getItemCount(): Int = forecasts.size
}