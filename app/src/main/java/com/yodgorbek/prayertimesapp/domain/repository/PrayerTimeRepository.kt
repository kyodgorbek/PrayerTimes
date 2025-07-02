package com.yodgorbek.prayertimesapp.domain.repository

import com.yodgorbek.prayertimesapp.domain.model.City
import com.yodgorbek.prayertimesapp.domain.model.PrayerTime

interface PrayerTimeRepository {
    suspend fun getPrayerTimesByCity(city: String, country: String, latitude: Double, longitude: Double): PrayerTime
    suspend fun getPrayerTimesByLocation(latitude: Double, longitude: Double): PrayerTime
    suspend fun saveCity(city: City)
    suspend fun getAllCities(): List<City>
    suspend fun deleteCity(cityName: String)
}
