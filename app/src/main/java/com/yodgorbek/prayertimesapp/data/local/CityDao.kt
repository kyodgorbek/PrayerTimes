package com.yodgorbek.prayertimesapp.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface CityDao {
    @Insert
    suspend fun insert(city: CityEntity)

    @Query("SELECT * FROM cities")
    suspend fun getAllCities(): List<CityEntity>

    @Query("DELETE FROM cities WHERE cityName = :cityName")
    suspend fun deleteCity(cityName: String)
}