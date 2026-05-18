package com.example.myapplication

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.model.CityWeather

class CityAdapter(
    private var cities: MutableList<CityWeather>,
    private val onDeleteClick: (CityWeather) -> Unit,
    private val onItemClick: (CityWeather) -> Unit
) : RecyclerView.Adapter<CityAdapter.CityViewHolder>() {

    class CityViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvCity: TextView = view.findViewById(R.id.tvCity)
        val tvTemp: TextView = view.findViewById(R.id.tvTemp)
        val tvDesc: TextView = view.findViewById(R.id.tvDesc)
        val imgWeather: ImageView = view.findViewById(R.id.imgWeather)
        val btnDelete: ImageButton = view.findViewById(R.id.btnDelete)
        val btnFavorite: ImageButton = view.findViewById(R.id.btnFavorite)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CityViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_city, parent, false)
        return CityViewHolder(view)
    }

    override fun onBindViewHolder(holder: CityViewHolder, position: Int) {
        val city = cities[position]
        val todayForecast = city.forecasts.firstOrNull()

        holder.tvCity.text = city.name
        holder.tvTemp.text = todayForecast?.let { "${it.temperature}°C" } ?: "N/A"
        holder.tvDesc.text = todayForecast?.description ?: "Нет данных"

        val iconResId = WeatherIconMapper.getIconResId(todayForecast?.icon)
            .takeIf { it != R.drawable.ic_launcher_foreground }
            ?: WeatherIconMapper.getIconResIdByDescription(todayForecast?.description)
        holder.imgWeather.setImageResource(iconResId)

        // Звёздочка только как индикатор (не кликабельна)
        val isFav = PrefsManager.isFavorite(holder.itemView.context, city.id)
        holder.btnFavorite.setImageResource(
            if (isFav) android.R.drawable.btn_star_big_on
            else android.R.drawable.btn_star_big_off
        )
        holder.btnFavorite.isEnabled = false
        holder.btnFavorite.alpha = 0.6f

        holder.itemView.setOnClickListener { onItemClick(city) }
        holder.btnDelete.setOnClickListener { onDeleteClick(city) }
    }

    override fun getItemCount(): Int = cities.size

    fun submitList(newList: List<CityWeather>) {
        cities.clear()
        cities.addAll(newList)
        notifyDataSetChanged()
    }
}