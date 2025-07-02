package com.yodgorbek.prayertimesapp.domain.model

data class PrayerTime(
    val fajr: String,
    val dhuhr: String,
    val asr: String,
    val maghrib: String,
    val isha: String,
    val date: String
)