package com.yodgorbek.prayertimesapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "prayer_times")
data class PrayerTimeEntity(
    @PrimaryKey val id: String,
    val city: String,
    val fajr: String,
    val dhuhr: String,
    val asr: String,
    val maghrib: String,
    val isha: String,
    val date: String,
    val latitude: Double,
    val longitude: Double
)