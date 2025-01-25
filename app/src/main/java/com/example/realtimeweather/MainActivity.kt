package com.example.realtimeweather

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import coil3.compose.AsyncImage
import com.example.realtimeweather.api.NetworkResponse
import com.example.realtimeweather.api.WeatherModel
import com.example.realtimeweather.ui.theme.RealtimeWeatherTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val weatherviewModel = ViewModelProvider(this)[WeatherViewModel::class.java]
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RealtimeWeatherTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    WeatherPage(
                        modifier = Modifier.padding(innerPadding),
                        weatherviewModel
                    )
                }
            }
        }
    }
}

@Composable
fun WeatherPage(modifier: Modifier = Modifier, viewModel: WeatherViewModel) {
    var city = rememberSaveable { mutableStateOf("") }
    val weatherResult = viewModel.weatherResult.observeAsState()
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = CenterHorizontally
    ) {
        // Search Bar Section
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = CenterVertically
        ) {
            OutlinedTextField(
                modifier = Modifier.weight(1f),
                value = city.value,
                onValueChange = { city.value = it },
                label = { Text(text = "City") }
            )
            IconButton(onClick = {
                viewModel.getData(city.value)
                keyboardController?.hide()
            }) {
                Icon(imageVector = Icons.Default.Search, contentDescription = "Search")
            }
        }

        // Weather Result Section
        when (val result = weatherResult.value) {
            is NetworkResponse.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(CenterHorizontally))
                Text(text = "Loading...", modifier = Modifier.align(CenterHorizontally))
            }
            is NetworkResponse.Success -> {
                WeatherDetails(data = result.data)
            }
            is NetworkResponse.Error -> {
                Text(
                    text = result.message,
                    color = Color.Red,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            null -> {}
        }
    }
}

@Composable
fun WeatherDetails(data: WeatherModel) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalAlignment = CenterHorizontally
    ) {
        // Location and Temperature Section
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = "Location icon",
                modifier = Modifier.size(40.dp)
            )
            Text(text = data.location.name, fontSize = 30.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = data.location.country,
                fontSize = 18.sp,
                color = Color.Gray
            )
        }

        // Temperature & Weather Icon
        Text(
            text = "${data.current.temp_c}°C",
            fontSize = 64.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        val imageUrl = "https:${data.current.condition.icon}".replace("64x64", "128x128")
        AsyncImage(
            model = imageUrl,
            contentDescription = "Weather icon",
            modifier = Modifier.size(128.dp),
            onError = { Log.e("WeatherImage", "Failed to load image: ${it.result.throwable}") }
        )

        Text(
            text = data.current.condition.text,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = Color.Gray,
            modifier = Modifier.padding(top = 8.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Weather Details Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                WeatherInfoRow(title = "Humidity", value = "${data.current.humidity}%")
                WeatherInfoRow(title = "Wind", value = "${data.current.wind_kph} km/h")
                WeatherInfoRow(title = "Pressure", value = "${data.current.pressure_mb} mb")
                WeatherInfoRow(title = "UV", value = data.current.uv)
                WeatherInfoRow(title = "Feels like", value = "${data.current.feelslike_c}°C")
                WeatherInfoRow(title = "Wind chill", value = "${data.current.windchill_c}°C")
                WeatherInfoRow(title = "Heat index", value = "${data.current.heatindex_c}°C")
                WeatherInfoRow(title = "Dew point", value = "${data.current.dewpoString_c}°C")
                WeatherInfoRow(title = "Wind direction", value = data.current.wind_dir)
                WeatherInfoRow(title = "Wind gust", value = "${data.current.gust_kph} km/h")
                WeatherInfoRow(title = "Cloud", value = "${data.current.cloud}%")
                WeatherInfoRow(title = "Condition", value = data.current.condition.text)
            }
        }
    }
}

@Composable
fun WeatherInfoRow(title: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = title, fontSize = 16.sp)
        Text(text = value, fontSize = 16.sp)
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    val weatherViewModel = WeatherViewModel() // Create a mock or sample WeatherViewModel
    RealtimeWeatherTheme {
        WeatherPage(viewModel = weatherViewModel)
    }
}
