package com.yodgorbek.prayertimesapp.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.yodgorbek.prayertimesapp.domain.model.Settings
import com.yodgorbek.prayertimesapp.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepositoryImpl @Inject constructor(
    private val context: Context
) : SettingsRepository {
    private val LANGUAGE_KEY = stringPreferencesKey("language")
    private val NOTIFICATIONS_ENABLED_KEY = booleanPreferencesKey("notifications_enabled")
    private val NOTIFICATION_SOUND_KEY = stringPreferencesKey("notification_sound")
    private val AZAN_SOUND_ENABLED_KEY = booleanPreferencesKey("azan_sound_enabled")
    private val FAJR_AZAN_SOUND_KEY = stringPreferencesKey("fajr_azan_sound")
    private val DHUHR_AZAN_SOUND_KEY = stringPreferencesKey("dhuhr_azan_sound")
    private val ASR_AZAN_SOUND_KEY = stringPreferencesKey("asr_azan_sound")
    private val MAGHRIB_AZAN_SOUND_KEY = stringPreferencesKey("maghrib_azan_sound")
    private val ISHA_AZAN_SOUND_KEY = stringPreferencesKey("isha_azan_sound")

    override suspend fun saveSettings(settings: Settings) {
        context.dataStore.edit { prefs ->
            prefs[LANGUAGE_KEY] = settings.language
            prefs[NOTIFICATIONS_ENABLED_KEY] = settings.notificationsEnabled
            prefs[NOTIFICATION_SOUND_KEY] = settings.notificationSound
            prefs[AZAN_SOUND_ENABLED_KEY] = settings.azanSoundEnabled
            prefs[FAJR_AZAN_SOUND_KEY] = settings.fajrAzanSound
            prefs[DHUHR_AZAN_SOUND_KEY] = settings.dhuhrAzanSound
            prefs[ASR_AZAN_SOUND_KEY] = settings.asrAzanSound
            prefs[MAGHRIB_AZAN_SOUND_KEY] = settings.maghribAzanSound
            prefs[ISHA_AZAN_SOUND_KEY] = settings.ishaAzanSound
        }
    }

    override suspend fun getSettings(): Settings {
        val prefs = context.dataStore.data.first()
        return Settings(
            language = prefs[LANGUAGE_KEY] ?: "en",
            notificationsEnabled = prefs[NOTIFICATIONS_ENABLED_KEY] ?: true,
            notificationSound = prefs[NOTIFICATION_SOUND_KEY] ?: "default",
            azanSoundEnabled = prefs[AZAN_SOUND_ENABLED_KEY] ?: true,
            fajrAzanSound = prefs[FAJR_AZAN_SOUND_KEY] ?: "azan_fajr.mp3",
            dhuhrAzanSound = prefs[DHUHR_AZAN_SOUND_KEY] ?: "azan_dhuhr.mp3",
            asrAzanSound = prefs[ASR_AZAN_SOUND_KEY] ?: "azan_asr.mp3",
            maghribAzanSound = prefs[MAGHRIB_AZAN_SOUND_KEY] ?: "azan_maghrib.mp3",
            ishaAzanSound = prefs[ISHA_AZAN_SOUND_KEY] ?: "azan_isha.mp3"
        )
    }
}
