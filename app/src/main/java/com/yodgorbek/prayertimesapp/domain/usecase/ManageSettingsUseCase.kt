package com.yodgorbek.prayertimesapp.domain.usecase

import com.yodgorbek.prayertimesapp.domain.model.Settings
import com.yodgorbek.prayertimesapp.domain.repository.SettingsRepository
import javax.inject.Inject

class ManageSettingsUseCase @Inject constructor(
    private val repository: SettingsRepository
) {
    suspend fun saveSettings(settings: Settings) {
        repository.saveSettings(settings)
    }

    suspend fun getSettings(): Settings {
        return repository.getSettings()
    }
}