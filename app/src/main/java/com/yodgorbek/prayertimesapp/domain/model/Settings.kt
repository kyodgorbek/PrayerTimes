package com.yodgorbek.prayertimesapp.domain.model

data class Settings(
    val language: String,
    val notificationsEnabled: Boolean,
    val notificationSound: String,
    val azanSoundEnabled: Boolean = true,
    val fajrAzanSound: String = "azan_fajr.mp3",
    val dhuhrAzanSound: String = "azan_dhuhr.mp3",
    val asrAzanSound: String = "azan_asr.mp3",
    val maghribAzanSound: String = "azan_maghrib.mp3",
    val ishaAzanSound: String = "azan_isha.mp3"
)