package com.example.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CityAdapter(
    private var cities: MutableList<City>,
    private val onDeleteClick: (City) -> Unit
) : RecyclerView.Adapter<CityAdapter.CityViewHolder>() {

    class CityViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvCity: TextView = view.findViewById(R.id.tvCity)
        val tvTemp: TextView = view.findViewById(R.id.tvTemp)
        val tvDesc: TextView = view.findViewById(R.id.tvDesc)
        val imgWeather: ImageView = view.findViewById(R.id.imgWeather)
        val btnDelete: ImageButton = view.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CityViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_city, parent, false)
        return CityViewHolder(view)
    }

    override fun onBindViewHolder(holder: CityViewHolder, position: Int) {
        val city = cities[position]
        holder.tvCity.text = city.name
        holder.tvTemp.text = "${city.temperature}°C"
        holder.tvDesc.text = city.description
        holder.imgWeather.setImageResource(city.iconResId)

        holder.btnDelete.setOnClickListener {
            onDeleteClick(city)
        }
    }

    override fun getItemCount(): Int = cities.size

    fun submitList(newList: List<City>) {
        cities.clear()
        cities.addAll(newList)
        notifyDataSetChanged()
    }
}