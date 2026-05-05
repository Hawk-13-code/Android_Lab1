package com.example.myapplication

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.google.android.material.snackbar.Snackbar

class DetailFragment : Fragment(R.layout.fragment_detail) {

    private val args: DetailFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val city = args.cityData
        view.findViewById<TextView>(R.id.tvDetailCity).text = city.name
        view.findViewById<TextView>(R.id.tvDetailTemp).text = "${city.temperature}°C"
        view.findViewById<TextView>(R.id.tvDetailDesc).text = city.description
        view.findViewById<ImageView>(R.id.imgDetailWeather).setImageResource(city.iconResId)

        val btnFav = view.findViewById<Button>(R.id.btnToggleFavorite)
        btnFav.text = if (city.isFavorite) "⭐ В избранном" else "☆ В избранное"
        btnFav.setOnClickListener {
            city.isFavorite = !city.isFavorite
            btnFav.text = if (city.isFavorite) "⭐ В избранном" else "☆ В избранное"

            val allCities = PrefsManager.loadCities(requireContext())
            val index = allCities.indexOfFirst { it.name == city.name }
            if (index != -1) {
                allCities[index] = city
                PrefsManager.saveCities(requireContext(), allCities)
            }

            Snackbar.make(view,
                if(city.isFavorite) "Добавлено в избранное" else "Удалено из избранного",
                Snackbar.LENGTH_SHORT).show()
        }
    }
}