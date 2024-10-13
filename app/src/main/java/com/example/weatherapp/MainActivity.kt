package com.example.weatherapp

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import com.example.weatherapp.databinding.ActivityMainBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.math.roundToInt
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Calendar

class MainActivity : AppCompatActivity() {
    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        fetchWeatherData("london")
        searchCity()
    }

    private fun searchCity() {
        val searchView = binding.searchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            android.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) {
                    fetchWeatherData(query)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }
        })
    }

    private fun convertTimestampToTime(timestamp: Number): String {
        val date =
            Date(timestamp.toLong() * 1000)
        val format = SimpleDateFormat("HH:mm", Locale.getDefault())
        return format.format(date)
    }

    private fun getCurrentDayOfWeek(): String {
        val currentDate = Date()
        val dateFormat = SimpleDateFormat("EEEE", Locale.getDefault())
        return dateFormat.format(currentDate)
    }

    private fun getCurrentDateFormatted(): String {
        val currentDate = Date()
        val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        return dateFormat.format(currentDate)
    }

    private fun fetchWeatherData(city: String) {
        val retrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .build()

        val weatherApi = retrofit.create(WeatherApi::class.java)

        val response =
            weatherApi.getWeatherData(city, "00eee5c2cabcfd6b850b0d49da0895ce", "metric")

        response.enqueue(object : Callback<Weather> {
            override fun onResponse(call: Call<Weather>, response: Response<Weather>) {
                val responseBody = response.body()
                if (response.isSuccessful && responseBody != null) {
                    val weather: Weather = responseBody
                    val condition = weather.weather.firstOrNull()?.main ?: "unknown"
                    val temperature = weather.main.temp.roundToInt()
                    val sunrise = convertTimestampToTime(weather.sys.sunrise)
                    val sunset = convertTimestampToTime(weather.sys.sunset)
                    val humidity = weather.main.humidity
                    val windSpeed = weather.wind.speed
                    val sea = weather.main.sea_level
                    val minTemp = weather.main.temp_min.roundToInt()
                    val maxTemp = weather.main.temp_max.roundToInt()
                    val city = weather.name
                    val today = getCurrentDayOfWeek()
                    val todayDate = getCurrentDateFormatted()
                    binding.temp.text = "${temperature}ºC"
                    binding.weather.text = "${condition}"
                    binding.humidity.text = "${humidity} %"
                    binding.windSpeed.text = "${windSpeed} m/s"
                    binding.sunrise.text = sunrise
                    binding.sunset.text = sunset
                    binding.sea.text = "${sea} hPa"
                    binding.minTemp.text = "Min: ${minTemp}ºC"
                    binding.maxTemp.text = "Max: ${maxTemp}ºC"
                    binding.today.text = today
                    binding.cityName.text = city
                    binding.date.text = todayDate

                    changeImagesAccordingToWeatherCondition(condition)
                } else {
                    Log.e("Weather", "Error fetching data: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<Weather>, t: Throwable) {
                Log.e("Weather", "Error calling the API: ${t.message}")
            }
        })
    }

    private fun changeImagesAccordingToWeatherCondition(condition: String) {
        when (condition) {
            "Party Clouds", "Clouds", "Overcast", "Mist", "Foggy", "Haze" -> {
                binding.root.setBackgroundResource(R.drawable.colud_background)
                binding.lottieAnimationView.setAnimation((R.raw.cloud))
            }

            "Light Rain", "Drizzle", "Modarate Rain", "Showers", "Heavy Rain" -> {
                binding.root.setBackgroundResource(R.drawable.rain_background)
                binding.lottieAnimationView.setAnimation(R.raw.rain)
            }

            "Light Snow", "Modarate Snow", "Havy Snow", "Blizzard" -> {
                binding.root.setBackgroundResource(R.drawable.snow_background)
                binding.lottieAnimationView.setAnimation(R.raw.snow)
            }

            else -> {
                binding.root.setBackgroundResource(R.drawable.sunny_background)
                binding.lottieAnimationView.setAnimation(R.raw.sun)
            }
        }
        binding.lottieAnimationView.playAnimation()
    }
}
