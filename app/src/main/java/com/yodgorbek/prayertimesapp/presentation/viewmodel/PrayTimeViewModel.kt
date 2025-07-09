package com.yodgorbek.prayertimesapp.presentation.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yodgorbek.prayertimesapp.domain.model.City
import com.yodgorbek.prayertimesapp.domain.model.PrayerTime
import com.yodgorbek.prayertimesapp.domain.model.Settings
import com.yodgorbek.prayertimesapp.domain.usecase.GetPrayerTimesByCityUseCase
import com.yodgorbek.prayertimesapp.domain.usecase.GetPrayerTimesByLocationUseCase
import com.yodgorbek.prayertimesapp.domain.usecase.ManageCitiesUseCase
import com.yodgorbek.prayertimesapp.domain.usecase.ManageSettingsUseCase
import com.yodgorbek.prayertimesapp.util.LocationHelper
import com.yodgorbek.prayertimesapp.util.NotificationWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit
import android.util.Log

data class PrayerTimeUiState(
    val isLoading: Boolean = false,
    val prayerTime: PrayerTime? = null,
    val error: String? = null,
    val cities: List<City> = emptyList(),
    val currentLocation: Pair<Double, Double>? = null,
    val nextPrayer: String? = null,
    val selectedCity: City? = null,
    val suggestedCity: Triple<String, String, String>? = null,
    val settings: Settings = Settings("en", true, "default")
)

@HiltViewModel
class PrayerTimeViewModel @Inject constructor(
    private val getPrayerTimesByCityUseCase: GetPrayerTimesByCityUseCase,
    private val getPrayerTimesByLocationUseCase: GetPrayerTimesByLocationUseCase,
    private val manageCitiesUseCase: ManageCitiesUseCase,
    private val manageSettingsUseCase: ManageSettingsUseCase,
    private val locationHelper: LocationHelper,
    private val workManager: WorkManager
) : ViewModel() {

    private val _uiState = mutableStateOf(PrayerTimeUiState())
    val uiState: State<PrayerTimeUiState> = _uiState

    init {
        loadCities()
        loadSettings()
        fetchPrayerTimesByLocation()
        suggestCity()
    }

    fun fetchPrayerTimesByCity(city: String, country: String, latitude: Double, longitude: Double) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = getPrayerTimesByCityUseCase(city, country, latitude, longitude)
            _uiState.value = if (result.isSuccess) {
                val prayerTime = result.getOrNull()
                if (_uiState.value.settings.notificationsEnabled) {
                    prayerTime?.let { scheduleNotifications(it) }
                }
                _uiState.value.copy(
                    prayerTime = prayerTime,
                    nextPrayer = calculateNextPrayer(prayerTime),
                    isLoading = false,
                    selectedCity = City(city, country, latitude, longitude),
                    error = null
                )
            } else {
                _uiState.value.copy(
                    error = result.exceptionOrNull()?.message ?: "Unknown error",
                    isLoading = false
                )
            }
        }
    }

    fun fetchPrayerTimesByLocation() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val location = locationHelper.getLastLocation()
            if (location != null) {
                val result = getPrayerTimesByLocationUseCase(location.first, location.second)
                _uiState.value = if (result.isSuccess) {
                    val prayerTime = result.getOrNull()
                    if (_uiState.value.settings.notificationsEnabled) {
                        prayerTime?.let { scheduleNotifications(it) }
                    }
                    _uiState.value.copy(
                        prayerTime = prayerTime,
                        currentLocation = location,
                        nextPrayer = calculateNextPrayer(prayerTime),
                        isLoading = false,
                        selectedCity = null,
                        error = null
                    )
                } else {
                    _uiState.value.copy(
                        error = result.exceptionOrNull()?.message ?: "Unknown error",
                        isLoading = false
                    )
                }
            } else {
                _uiState.value = _uiState.value.copy(
                    error = "Location not available",
                    isLoading = false
                )
            }
        }
    }

    fun suggestCity() {
        viewModelScope.launch {
            val location = locationHelper.getLastLocation()
            if (location != null) {
                val cityInfo = locationHelper.getCityFromLocation(location.first, location.second)
                _uiState.value = _uiState.value.copy(suggestedCity = cityInfo)
            }
        }
    }

    fun saveCity(city: String, country: String, latitude: Double, longitude: Double) {
        viewModelScope.launch {
            manageCitiesUseCase.saveCity(City(city, country, latitude, longitude))
            loadCities()
        }
    }

    fun deleteCity(cityName: String) {
        viewModelScope.launch {
            manageCitiesUseCase.deleteCity(cityName)
            loadCities()
            if (_uiState.value.selectedCity?.cityName == cityName) {
                _uiState.value = _uiState.value.copy(selectedCity = null, prayerTime = null)
            }
        }
    }

    fun saveSettings(settings: Settings) {
        viewModelScope.launch {
            manageSettingsUseCase.saveSettings(settings)
            loadSettings()
        }
    }

    private fun loadCities() {
        viewModelScope.launch {
            val cities = manageCitiesUseCase.getAllCities()
            _uiState.value = _uiState.value.copy(cities = cities)
        }
    }

    private fun loadSettings() {
        viewModelScope.launch {
            val settings = manageSettingsUseCase.getSettings()
            _uiState.value = _uiState.value.copy(settings = settings)
        }
    }

    private fun scheduleNotifications(prayerTime: PrayerTime) {
        val prayers = listOf(
            "Fajr" to Pair(prayerTime.fajr, _uiState.value.settings.fajrAzanSound),
            "Dhuhr" to Pair(prayerTime.dhuhr, _uiState.value.settings.dhuhrAzanSound),
            "Asr" to Pair(prayerTime.asr, _uiState.value.settings.asrAzanSound),
            "Maghrib" to Pair(prayerTime.maghrib, _uiState.value.settings.maghribAzanSound),
            "Isha" to Pair(prayerTime.isha, _uiState.value.settings.ishaAzanSound)
        )
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.US)
        val now = Calendar.getInstance()

        for (dayOffset in 0..6) {
            prayers.forEach { (name, pair) ->
                val (time, azanSound) = pair
                try {
                    val parsedTime = sdf.parse(time) ?: return@forEach
                    val calendar = Calendar.getInstance()
                    calendar.time = parsedTime
                    calendar.set(Calendar.YEAR, now.get(Calendar.YEAR))
                    calendar.set(Calendar.MONTH, now.get(Calendar.MONTH))
                    calendar.set(Calendar.DAY_OF_MONTH, now.get(Calendar.DAY_OF_MONTH) + dayOffset)
                    calendar.set(Calendar.SECOND, 0)

                    if (calendar.timeInMillis > System.currentTimeMillis()) {
                        val delay = calendar.timeInMillis - System.currentTimeMillis()
                        val data = Data.Builder()
                            .putString("prayerName", name)
                            .putString("prayerTime", time)
                            .putString("azanSound", azanSound)
                            .putBoolean("azanSoundEnabled", _uiState.value.settings.azanSoundEnabled)
                            .build()
                        val workRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
                            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                            .setInputData(data)
                            .build()
                        workManager.enqueue(workRequest)
                    }
                } catch (e: Exception) {
                    Log.e("PrayerTimeViewModel", "Failed to schedule $name: ${e.message}")
                }
            }
        }
    }

    private fun calculateNextPrayer(prayerTime: PrayerTime?): String? {
        if (prayerTime == null) return null
        val prayers = listOf(
            "Fajr" to prayerTime.fajr,
            "Dhuhr" to prayerTime.dhuhr,
            "Asr" to prayerTime.asr,
            "Maghrib" to prayerTime.maghrib,
            "Isha" to prayerTime.isha
        )
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.US)
        val now = Calendar.getInstance().timeInMillis

        for ((name, timeStr) in prayers) {
            try {
                val parsedTime = sdf.parse(timeStr) ?: continue
                val prayerCal = Calendar.getInstance()
                prayerCal.time = parsedTime
                prayerCal.set(Calendar.HOUR_OF_DAY, parsedTime.hours)
                prayerCal.set(Calendar.MINUTE, parsedTime.minutes)
                prayerCal.set(Calendar.SECOND, 0)
                if (prayerCal.timeInMillis > now) {
                    return name
                }
            } catch (e: Exception) {
                Log.e("PrayerTimeViewModel", "Error parsing $name: $timeStr")
            }
        }

        return null
    }
}
