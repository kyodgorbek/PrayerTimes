package com.yodgorbek.prayertimesapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cities")
data class CityEntity(
    @PrimaryKey val cityName: String,
    val country: String,
    val latitude: Double,
    val longitude: Double
)