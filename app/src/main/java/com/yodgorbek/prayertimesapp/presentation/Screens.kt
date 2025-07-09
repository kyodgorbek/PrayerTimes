package com.yodgorbek.prayertimesapp.presentation

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.MediaPlayer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.yodgorbek.prayertimesapp.R
import com.yodgorbek.prayertimesapp.domain.model.City
import com.yodgorbek.prayertimesapp.domain.model.PrayerTime
import com.yodgorbek.prayertimesapp.domain.model.Settings
import com.yodgorbek.prayertimesapp.presentation.navigation.NavigationHost
import com.yodgorbek.prayertimesapp.presentation.viewmodel.PrayerTimeUiState
import com.yodgorbek.prayertimesapp.presentation.viewmodel.PrayerTimeViewModel
import com.yodgorbek.prayertimesapp.util.LocalizationHelper
import com.yodgorbek.prayertimesapp.util.QiblaHelper
import java.text.SimpleDateFormat
import java.util.Locale

enum class NavRoute {
    PRAYER_TIMES,
    QIBLA,
    CITIES,
    SETTINGS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavHostController, viewModel: PrayerTimeViewModel) {
    val context = LocalContext.current
    val uiState by viewModel.uiState
    LaunchedEffect(uiState.suggestedCity) {
        uiState.suggestedCity?.let { cityInfo ->
            val languageCode = when (cityInfo.third.toUpperCase(Locale.ROOT)) {
                "RU" -> "ru"
                "UZ" -> "uz"
                "DE" -> "de"
                else -> "en"
            }
            if (languageCode != uiState.settings.language) {
                viewModel.saveSettings(uiState.settings.copy(language = languageCode))
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                actions = {
                    IconButton(onClick = { navController.navigate(NavRoute.CITIES.name) }) {
                        Icon(Icons.Default.List, contentDescription = stringResource(R.string.cities))
                    }
                    IconButton(onClick = { navController.navigate(NavRoute.SETTINGS.name) }) {
                        Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.settings))
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Schedule, contentDescription = stringResource(R.string.prayer_times)) },
                    label = { Text(stringResource(R.string.prayer_times)) },
                    selected = navController.currentDestination?.route == NavRoute.PRAYER_TIMES.name,
                    onClick = { navController.navigate(NavRoute.PRAYER_TIMES.name) }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Place, contentDescription = stringResource(R.string.qibla)) },
                    label = { Text(stringResource(R.string.qibla)) },
                    selected = navController.currentDestination?.route == NavRoute.QIBLA.name,
                    onClick = { navController.navigate(NavRoute.QIBLA.name) }
                )
            }
        }
    ) { padding ->
        NavigationHost(navController, viewModel, Modifier.padding(padding), context)
    }
}


@Composable
fun PrayerTimeScreen(viewModel: PrayerTimeViewModel, context: Context) {
    val uiState by viewModel.uiState
    var city by remember { mutableStateOf(TextFieldValue(uiState.suggestedCity?.first ?: "")) }
    var country by remember { mutableStateOf(TextFieldValue(uiState.suggestedCity?.second ?: "")) }
    var latitude by remember { mutableStateOf(TextFieldValue("")) }
    var longitude by remember { mutableStateOf(TextFieldValue("")) }
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                viewModel.fetchPrayerTimesByLocation()
                viewModel.suggestCity()
            }
        }
    )

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = city,
            onValueChange = { city = it },
            label = { Text(stringResource(R.string.city)) },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = country,
            onValueChange = { country = it },
            label = { Text(stringResource(R.string.country)) },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = latitude,
            onValueChange = { latitude = it },
            label = { Text(stringResource(R.string.latitude)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = longitude,
            onValueChange = { longitude = it },
            label = { Text(stringResource(R.string.longitude)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                val lat = latitude.text.toDoubleOrNull()
                val lon = longitude.text.toDoubleOrNull()
                if (city.text.isNotBlank() && country.text.isNotBlank() && lat != null && lon != null) {
                    viewModel.fetchPrayerTimesByCity(city.text, country.text, lat, lon)
                    viewModel.saveCity(city.text, country.text, lat, lon)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.get_prayer_times))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = { locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.use_location))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = { viewModel.suggestCity() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.suggest_city))
        }
        Spacer(modifier = Modifier.height(16.dp))
        PrayerTimeContent(uiState, context)
        uiState.prayerTime?.let { prayerTime ->
            val nextPrayer = uiState.nextPrayer
            val azanSound = when (nextPrayer) {
                "Fajr" -> uiState.settings.fajrAzanSound
                "Dhuhr" -> uiState.settings.dhuhrAzanSound
                "Asr" -> uiState.settings.asrAzanSound
                "Maghrib" -> uiState.settings.maghribAzanSound
                "Isha" -> uiState.settings.ishaAzanSound
                else -> uiState.settings.fajrAzanSound // Default to Fajr
            }
            Button(
                onClick = {
                    if (uiState.settings.azanSoundEnabled) {
                        try {
                            val rawResId = when (nextPrayer) {
                                "Fajr" -> R.raw.azan_fajr
                                "Dhuhr" -> R.raw.azan_dhuhr
                                "Asr" -> R.raw.azan_asr
                                "Maghrib" -> R.raw.azan_maghrib
                                "Isha" -> R.raw.azan_isha
                                else -> R.raw.azan_fajr
                            }

                            val mediaPlayer = MediaPlayer.create(context, rawResId)
                            mediaPlayer?.apply {
                                start()
                                setOnCompletionListener { release() }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState.settings.azanSoundEnabled
            ) {
                Text("Play ${nextPrayer ?: "Fajr"} Azan")
            }

        }
    }
}


@Composable
fun PrayerTimeContent(uiState: PrayerTimeUiState, context: Context) {
    when {
        uiState.isLoading -> CircularProgressIndicator()
        uiState.error != null -> Text(
            text = stringResource(R.string.error, uiState.error),
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(16.dp)
        )
        uiState.prayerTime != null -> PrayerTimeDisplay(
            uiState.prayerTime,
            uiState.nextPrayer,
            uiState.selectedCity?.cityName ?: "Current Location",
            context
        )
    }
}

@Composable
fun PrayerTimeDisplay(
    prayerTime: PrayerTime,
    nextPrayer: String?,
    location: String,
    context: Context
) {
    val timeFormat = LocalizationHelper.getLocalizedTimeFormat(context)
    val sdf = SimpleDateFormat(timeFormat, context.resources.configuration.locales[0])
    val inputFormat = SimpleDateFormat("HH:mm:ss", Locale.US)

    fun formatTime(time: String): String {
        return try {
            val parsed = inputFormat.parse(time)
            parsed?.let { sdf.format(it) } ?: time
        } catch (e: Exception) {
            time
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.prayer_time_for, location),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            PrayerTimeCard(stringResource(R.string.fajr), formatTime(prayerTime.fajr), nextPrayer == "Fajr")
            PrayerTimeCard(stringResource(R.string.dhuhr), formatTime(prayerTime.dhuhr), nextPrayer == "Dhuhr")
            PrayerTimeCard(stringResource(R.string.asr), formatTime(prayerTime.asr), nextPrayer == "Asr")
            PrayerTimeCard(stringResource(R.string.maghrib), formatTime(prayerTime.maghrib), nextPrayer == "Maghrib")
            PrayerTimeCard(stringResource(R.string.isha), formatTime(prayerTime.isha), nextPrayer == "Isha")
        }
    }
}

@Composable
fun PrayerTimeCard(prayerName: String, time: String, isNext: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(if (isNext) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = prayerName, fontWeight = FontWeight.Medium)
        Text(text = time, fontWeight = if (isNext) FontWeight.Bold else FontWeight.Normal)
    }
}

@Composable
fun QiblaScreen(viewModel: PrayerTimeViewModel, context: Context) {
    val uiState by viewModel.uiState
    var qiblaDirection by remember { mutableStateOf(0f) }
    var azimuth by remember { mutableStateOf(0f) }
    val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                viewModel.fetchPrayerTimesByLocation()
            }
        }
    )

    val sensorListener = remember {
        object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event?.sensor?.type == Sensor.TYPE_ROTATION_VECTOR) {
                    val rotationMatrix = FloatArray(9)
                    SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                    val orientation = FloatArray(3)
                    SensorManager.getOrientation(rotationMatrix, orientation)
                    azimuth = (Math.toDegrees(orientation[0].toDouble()).toFloat() + 360) % 360
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
    }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        uiState.currentLocation?.let { location ->
            qiblaDirection = QiblaHelper.calculateQiblaDirection(location.first, location.second)
        } ?: uiState.selectedCity?.let { city ->
            qiblaDirection = QiblaHelper.calculateQiblaDirection(city.latitude, city.longitude)
        }
        sensorManager.registerListener(sensorListener, sensor, SensorManager.SENSOR_DELAY_UI)
    }

    DisposableEffect(Unit) {
        onDispose {
            sensorManager.unregisterListener(sensorListener)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (uiState.currentLocation == null && uiState.selectedCity == null) {
            Text(
                text = stringResource(R.string.qibla_no_location),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(16.dp)
            )
        } else {
            QiblaCompassView(azimuth, qiblaDirection)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.calibrate_compass),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun QiblaCompassView(azimuth: Float, qiblaDirection: Float) {
    Box(
        modifier = Modifier
            .size(200.dp)
            .background(Color.White, CircleShape)
            .border(2.dp, Color.Black, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(200.dp)) {
            val centerX = size.width / 2
            val centerY = size.height / 2
            val radius = size.width / 2 - 10
            val center = Offset(centerX, centerY)

            // Draw compass circle
            drawCircle(
                color = Color.Black,
                center = center,
                radius = radius,
                style = Stroke(width = 5f)
            )

            // Draw Qibla line (green)
            val qiblaAngle = qiblaDirection - 90
            val qiblaX = centerX + radius * kotlin.math.cos(Math.toRadians(qiblaAngle.toDouble())).toFloat()
            val qiblaY = centerY + radius * kotlin.math.sin(Math.toRadians(qiblaAngle.toDouble())).toFloat()
            drawLine(
                color = Color.Green,
                start = center,
                end = Offset(qiblaX, qiblaY),
                strokeWidth = 5f
            )

            // Draw North line (red)
            val northAngle = -azimuth - 90
            val northX = centerX + radius * kotlin.math.cos(Math.toRadians(northAngle.toDouble())).toFloat()
            val northY = centerY + radius * kotlin.math.sin(Math.toRadians(northAngle.toDouble())).toFloat()
            drawLine(
                color = Color.Red,
                start = center,
                end = Offset(northX, northY),
                strokeWidth = 5f
            )
        }

        // Labels
        Text(
            text = "Qibla",
            fontSize = 16.sp,
            color = Color.Green,
            modifier = Modifier.align(Alignment.TopCenter)
        )
        Text(
            text = "N",
            fontSize = 16.sp,
            color = Color.Red,
            modifier = Modifier
                .align(Alignment.Center)
                .rotate(-azimuth)
        )
    }
}

@Composable
fun CitiesScreen(viewModel: PrayerTimeViewModel, context: Context) {
    val uiState by viewModel.uiState
    var city by remember { mutableStateOf(TextFieldValue("")) }
    var country by remember { mutableStateOf(TextFieldValue("")) }
    var latitude by remember { mutableStateOf(TextFieldValue("")) }
    var longitude by remember { mutableStateOf(TextFieldValue("")) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.add_city),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = city,
            onValueChange = { city = it },
            label = { Text(stringResource(R.string.city)) },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = country,
            onValueChange = { country = it },
            label = { Text(stringResource(R.string.country)) },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = latitude,
            onValueChange = { latitude = it },
            label = { Text(stringResource(R.string.latitude)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = longitude,
            onValueChange = { longitude = it },
            label = { Text(stringResource(R.string.longitude)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                val lat = latitude.text.toDoubleOrNull()
                val lon = longitude.text.toDoubleOrNull()
                if (city.text.isNotBlank() && country.text.isNotBlank() && lat != null && lon != null) {
                    viewModel.saveCity(city.text, country.text, lat, lon)
                    city = TextFieldValue("")
                    country = TextFieldValue("")
                    latitude = TextFieldValue("")
                    longitude = TextFieldValue("")
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.add_city))
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.saved_cities),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(uiState.cities) { city ->
                CityCard(
                    city = city,
                    onSelect = {
                        viewModel.fetchPrayerTimesByCity(city.cityName, city.country, city.latitude, city.longitude)
                    },
                    onDelete = { viewModel.deleteCity(city.cityName) }
                )
            }
        }
    }
}

@Composable
fun CityCard(city: City, onSelect: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onSelect() },
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = city.cityName, fontWeight = FontWeight.Bold)
                Text(text = city.country, style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete))
            }
        }
    }
}

@Composable
fun SettingsScreen(viewModel: PrayerTimeViewModel, context: Context) {
    val uiState by viewModel.uiState
    var notificationsEnabled by remember { mutableStateOf(uiState.settings.notificationsEnabled) }
    var azanSoundEnabled by remember { mutableStateOf(uiState.settings.azanSoundEnabled) }
    var selectedLanguage by remember { mutableStateOf(uiState.settings.language) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.settings),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(stringResource(R.string.language), style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        DropdownMenu(
            languages = listOf("en" to stringResource(R.string.english), "ru" to stringResource(R.string.russian), "uz" to stringResource(R.string.uzbek), "de" to stringResource(R.string.german)),
            selectedLanguage = selectedLanguage,
            onLanguageSelected = { language ->
                selectedLanguage = language
                viewModel.saveSettings(
                    uiState.settings.copy(language = language)
                )
            }
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(stringResource(R.string.notifications), style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(stringResource(R.string.enable_notifications))
            Switch(
                checked = notificationsEnabled,
                onCheckedChange = {
                    notificationsEnabled = it
                    viewModel.saveSettings(
                        uiState.settings.copy(notificationsEnabled = it)
                    )
                }
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(stringResource(R.string.enable_azan_sound))
            Switch(
                checked = azanSoundEnabled,
                onCheckedChange = {
                    azanSoundEnabled = it
                    viewModel.saveSettings(
                        uiState.settings.copy(azanSoundEnabled = it)
                    )
                }
            )
        }
    }
}

@Composable
fun DropdownMenu(
    languages: List<Pair<String, String>>,
    selectedLanguage: String,
    onLanguageSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedTextField(
            value = languages.find { it.first == selectedLanguage }?.second ?: "",
            onValueChange = {},
            label = { Text(stringResource(R.string.language)) },
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true }
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            languages.forEach { (code, name) ->
                DropdownMenuItem(
                    text = { Text(name) },
                    onClick = {
                        onLanguageSelected(code)
                        expanded = false
                    }
                )
            }
        }
    }
}