package com.example.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.ui.ForecastUiItem

class CityAdapter(
    private var items: MutableList<ForecastUiItem>,
    private val onDeleteClick: (ForecastUiItem) -> Unit,
    private val onItemClick: (ForecastUiItem) -> Unit
) : RecyclerView.Adapter<CityAdapter.CityViewHolder>() {

    class CityViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvCity: TextView = view.findViewById(R.id.tvCity)
        val tvTemp: TextView = view.findViewById(R.id.tvTemp)
        val tvDesc: TextView = view.findViewById(R.id.tvDesc)
        val tvDate: TextView = view.findViewById(R.id.tvDate)
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
        val item = items[position]

        holder.tvCity.text = item.cityName
        holder.tvTemp.text = "${item.temperature}°C"
        holder.tvDesc.text = item.description
        holder.tvDate.text = item.date

        val iconResId = WeatherIconMapper.getIconResId(item.icon)
            .takeIf { it != R.drawable.ic_launcher_foreground }
            ?: WeatherIconMapper.getIconResIdByDescription(item.description)
        holder.imgWeather.setImageResource(iconResId)

        val isFav = PrefsManager.isFavorite(holder.itemView.context, item.cityId)
        holder.btnFavorite.setImageResource(
            if (isFav) android.R.drawable.btn_star_big_on
            else android.R.drawable.btn_star_big_off
        )
        holder.btnFavorite.isEnabled = false
        holder.btnFavorite.alpha = 0.6f

        holder.itemView.setOnClickListener { onItemClick(item) }
        holder.btnDelete.setOnClickListener { onDeleteClick(item) }
    }

    override fun getItemCount(): Int = items.size

    fun submitList(newList: List<ForecastUiItem>) {
        items.clear()
        items.addAll(newList)
        notifyDataSetChanged()
    }
}