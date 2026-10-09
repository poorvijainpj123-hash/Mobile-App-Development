package com.example.weather

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.weather.databinding.ActivityMainBinding
import kotlinx.coroutines.launch
import java.net.UnknownHostException

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.searchButton.setOnClickListener {
            performSearch()
        }

        binding.cityInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch()
                true
            } else {
                false
            }
        }
    }

    private fun performSearch() {
        val city = binding.cityInput.text.toString().trim()
        if (city.isEmpty()) {
            Toast.makeText(this, "Please enter a city name", Toast.LENGTH_SHORT).show()
            return
        }
        fetchWeather(city)
    }



    private fun fetchWeather(city: String) {
        showLoading()

        lifecycleScope.launch {
            try {
                // Step 1: city name -> coordinates
                val geoResponse = RetrofitClient.geocodingApi.searchCity(city)
                val location = geoResponse.results?.firstOrNull()

                if (location == null) {
                    showError("City not found. Try another name.")
                    return@launch
                }

                // Step 2: coordinates -> current weather
                val weatherResponse = RetrofitClient.weatherApi.getCurrentWeather(
                    latitude = location.latitude,
                    longitude = location.longitude
                )

                showResult(location, weatherResponse)

            } catch (e: UnknownHostException) {
                showError("No internet connection. Please check your network.")
            } catch (e: Exception) {
                showError("Error: ${e.localizedMessage}")
            }
        }
    }

    private fun showLoading() {
        binding.progressBar.visibility = android.view.View.VISIBLE
        binding.errorText.visibility = android.view.View.GONE
        binding.resultLayout.visibility = android.view.View.GONE
    }

    private fun showError(message: String) {
        binding.progressBar.visibility = android.view.View.GONE
        binding.resultLayout.visibility = android.view.View.GONE
        binding.errorText.text = message
        binding.errorText.visibility = android.view.View.VISIBLE
    }

    private fun showResult(location: GeocodingResult, weatherResponse: WeatherResponse) {
        binding.progressBar.visibility = View.GONE
        binding.errorText.visibility = View.GONE
        binding.resultLayout.visibility = View.VISIBLE

        val current = weatherResponse.current
        binding.cityNameText.text = "${location.name}, ${location.country ?: ""}"
        binding.temperatureText.text = "${current.temperature.toInt()}°C"
        binding.conditionText.text = weatherCodeToDescription(current.weatherCode)
        binding.humidityText.text = "Humidity: ${current.humidity.toInt()}%"
        binding.windText.text = "Wind: ${current.windSpeed} km/h"

        // Setup 7-day Forecast RecyclerView
        val forecastItems = mutableListOf<ForecastDayItem>()
        val daily = weatherResponse.daily
        for (i in daily.dates.indices) {
            forecastItems.add(
                ForecastDayItem(
                    date = daily.dates[i],
                    maxTemp = daily.maxTemps[i],
                    minTemp = daily.minTemps[i]
                )
            )
        }

        binding.forecastRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.forecastRecyclerView.adapter = ForecastAdapter(forecastItems)
    }

    // Maps Open-Meteo's WMO weather codes to readable text
    private fun weatherCodeToDescription(code: Int): String {
        return when (code) {
            0 -> "Clear sky"
            1, 2, 3 -> "Partly cloudy"
            45, 48 -> "Fog"
            51, 53, 55 -> "Drizzle"
            61, 63, 65 -> "Rain"
            71, 73, 75 -> "Snow"
            80, 81, 82 -> "Rain showers"
            95 -> "Thunderstorm"
            96, 99 -> "Thunderstorm with hail"
            else -> "Unknown"
        }
    }
}