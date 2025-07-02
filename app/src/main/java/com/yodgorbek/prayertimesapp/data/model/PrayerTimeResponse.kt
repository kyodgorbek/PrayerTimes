package com.yodgorbek.prayertimesapp.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PrayerTimeResponse(
    @SerialName("data") val data: PrayerData
)

@Serializable
data class PrayerData(
    @SerialName("timings") val timings: Timings,
    @SerialName("date") val date: DateData,
    @SerialName("meta") val meta: MetaData
)

@Serializable
data class Timings(
    @SerialName("Fajr") val fajr: String,
    @SerialName("Dhuhr") val dhuhr: String,
    @SerialName("Asr") val asr: String,
    @SerialName("Maghrib") val maghrib: String,
    @SerialName("Isha") val isha: String
)

@Serializable
data class DateData(
    @SerialName("readable") val readable: String
)

@Serializable
data class MetaData(
    @SerialName("latitude") val latitude: Double,
    @SerialName("longitude") val longitude: Double
)
