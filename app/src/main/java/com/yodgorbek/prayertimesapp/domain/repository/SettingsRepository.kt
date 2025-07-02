package com.yodgorbek.prayertimesapp.domain.repository

import com.yodgorbek.prayertimesapp.domain.model.Settings

interface SettingsRepository {
    suspend fun saveSettings(settings: Settings)
    suspend fun getSettings(): Settings
}