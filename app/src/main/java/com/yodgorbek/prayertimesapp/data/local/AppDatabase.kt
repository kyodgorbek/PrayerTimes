package com.yodgorbek.prayertimesapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [PrayerTimeEntity::class, CityEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun prayerTimeDao(): PrayerTimeDao
    abstract fun cityDao(): CityDao
}