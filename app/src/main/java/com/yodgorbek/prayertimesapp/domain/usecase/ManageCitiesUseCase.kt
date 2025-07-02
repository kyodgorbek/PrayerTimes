package com.yodgorbek.prayertimesapp.domain.usecase

import com.yodgorbek.prayertimesapp.domain.model.City
import com.yodgorbek.prayertimesapp.domain.repository.PrayerTimeRepository
import javax.inject.Inject

class ManageCitiesUseCase @Inject constructor(
    private val repository: PrayerTimeRepository
) {
    suspend fun saveCity(city: City) {
        repository.saveCity(city)
    }

    suspend fun getAllCities(): List<City> {
        return repository.getAllCities()
    }

    suspend fun deleteCity(cityName: String) {
        repository.deleteCity(cityName)
    }
}