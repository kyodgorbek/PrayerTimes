package com.yodgorbek.prayertimesapp.di

import android.content.Context
import androidx.room.Room
import androidx.work.WorkManager
import com.yodgorbek.prayertimesapp.data.local.AppDatabase
import com.yodgorbek.prayertimesapp.data.remote.ApiService
import com.yodgorbek.prayertimesapp.data.repository.PrayerTimeRepositoryImpl

import com.yodgorbek.prayertimesapp.domain.repository.PrayerTimeRepository
import com.yodgorbek.prayertimesapp.domain.repository.SettingsRepository
import com.yodgorbek.prayertimesapp.util.LocationHelper
import com.yodgorbek.prayertimesapp.util.SettingsRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindPrayerTimeRepository(prayerTimeRepositoryImpl: PrayerTimeRepositoryImpl): PrayerTimeRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(settingsRepositoryImpl: SettingsRepositoryImpl): SettingsRepository
}

@Module
@InstallIn(SingletonComponent::class)
object AppModuleProvides {

    @Provides
    @Singleton
    fun provideHttpClient(): HttpClient {
        return HttpClient(OkHttp) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    prettyPrint = true
                    isLenient = true
                })
            }
        }
    }

    @Provides
    @Singleton
    fun provideApiService(client: HttpClient): ApiService {
        return ApiService(client)
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "prayer_times_db"
        ).build()
    }

    @Provides
    @Singleton
    fun providePrayerTimeDao(database: AppDatabase) = database.prayerTimeDao()

    @Provides
    @Singleton
    fun provideCityDao(database: AppDatabase) = database.cityDao()

    @Provides
    @Singleton
    fun provideLocationHelper(@ApplicationContext context: Context): LocationHelper {
        return LocationHelper(context)
    }

    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager {
        return WorkManager.getInstance(context)
    }
}