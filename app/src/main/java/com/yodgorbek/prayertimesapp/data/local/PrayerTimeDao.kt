package com.yodgorbek.prayertimesapp.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface PrayerTimeDao {
    @Insert
    suspend fun insert(prayerTime: PrayerTimeEntity)

    @Query("SELECT * FROM prayer_times WHERE city = :city AND date = :date LIMIT 1")
    suspend fun getPrayerTimes(city: String, date: String): PrayerTimeEntity?

    @Query("SELECT * FROM prayer_times WHERE latitude = :latitude AND longitude = :longitude AND date = :date LIMIT 1")
    suspend fun getPrayerTimesByLocation(latitude: Double, longitude: Double, date: String): PrayerTimeEntity?
}