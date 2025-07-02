package com.yodgorbek.prayertimesapp.data.repository

import com.yodgorbek.prayertimesapp.data.local.CityDao
import com.yodgorbek.prayertimesapp.data.local.CityEntity
import com.yodgorbek.prayertimesapp.data.local.PrayerTimeDao
import com.yodgorbek.prayertimesapp.data.local.PrayerTimeEntity
import com.yodgorbek.prayertimesapp.data.remote.ApiService
import com.yodgorbek.prayertimesapp.domain.model.City
import com.yodgorbek.prayertimesapp.domain.model.PrayerTime
import com.yodgorbek.prayertimesapp.domain.repository.PrayerTimeRepository
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

class PrayerTimeRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val prayerTimeDao: PrayerTimeDao,
    private val cityDao: CityDao
) : PrayerTimeRepository {
    override suspend fun getPrayerTimesByCity(city: String, country: String, latitude: Double, longitude: Double): PrayerTime {
        val date = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(System.currentTimeMillis())
        val cached = prayerTimeDao.getPrayerTimes(city, date)
        if (cached != null) {
            return PrayerTime(
                fajr = cached.fajr,
                dhuhr = cached.dhuhr,
                asr = cached.asr,
                maghrib = cached.maghrib,
                isha = cached.isha,
                date = cached.date
            )
        }
        val response = apiService.getPrayerTimesByCity(city, country)
        val entity = PrayerTimeEntity(
            id = "$city-$date",
            city = city,
            fajr = response.data.timings.fajr,
            dhuhr = response.data.timings.dhuhr,
            asr = response.data.timings.asr,
            maghrib = response.data.timings.maghrib,
            isha = response.data.timings.isha,
            date = response.data.date.readable,
            latitude = latitude,
            longitude = longitude
        )
        prayerTimeDao.insert(entity)
        return PrayerTime(
            fajr = entity.fajr,
            dhuhr = entity.dhuhr,
            asr = entity.asr,
            maghrib = entity.maghrib,
            isha = entity.isha,
            date = entity.date
        )
    }

    override suspend fun getPrayerTimesByLocation(latitude: Double, longitude: Double): PrayerTime {
        val date = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(System.currentTimeMillis())
        val cached = prayerTimeDao.getPrayerTimesByLocation(latitude, longitude, date)
        if (cached != null) {
            return PrayerTime(
                fajr = cached.fajr,
                dhuhr = cached.dhuhr,
                asr = cached.asr,
                maghrib = cached.maghrib,
                isha = cached.isha,
                date = cached.date
            )
        }
        val response = apiService.getPrayerTimesByLocation(latitude, longitude)
        val entity = PrayerTimeEntity(
            id = "location-$latitude-$longitude-$date",
            city = "Current Location",
            fajr = response.data.timings.fajr,
            dhuhr = response.data.timings.dhuhr,
            asr = response.data.timings.asr,
            maghrib = response.data.timings.maghrib,
            isha = response.data.timings.isha,
            date = response.data.date.readable,
            latitude = latitude,
            longitude = longitude
        )
        prayerTimeDao.insert(entity)
        return PrayerTime(
            fajr = entity.fajr,
            dhuhr = entity.dhuhr,
            asr = entity.asr,
            maghrib = entity.maghrib,
            isha = entity.isha,
            date = entity.date
        )
    }

    override suspend fun saveCity(city: City) {
        cityDao.insert(CityEntity(city.cityName, city.country, city.latitude, city.longitude))
    }

    override suspend fun getAllCities(): List<City> {
        return cityDao.getAllCities().map { City(it.cityName, it.country, it.latitude, it.longitude) }
    }

    override suspend fun deleteCity(cityName: String) {
        cityDao.deleteCity(cityName)
    }
}
