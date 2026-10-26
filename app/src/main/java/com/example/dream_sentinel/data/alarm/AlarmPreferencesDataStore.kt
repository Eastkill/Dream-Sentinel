package com.example.dream_sentinel.data.alarm

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.dream_sentinel.domain.alarm.AlarmPreferences
import com.example.dream_sentinel.domain.alarm.AlarmRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlarmPreferencesDataStore @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : AlarmRepository {

    override val preferences: Flow<AlarmPreferences> = dataStore.data.map { prefs ->
        AlarmPreferences(
            mode = prefs[KEY_MODE] ?: "AT_A_TIME",
            hour = prefs[KEY_HOUR] ?: 8,
            minute = prefs[KEY_MINUTE] ?: 0,
            soundOption = prefs[KEY_SOUND] ?: "First light",
            repeatOption = prefs[KEY_REPEAT] ?: "Just today",
            easeMeAwake = prefs[KEY_EASE_ME_AWAKE] ?: true,
            isConfigured = prefs[KEY_IS_CONFIGURED] ?: false,
            isScheduled = prefs[KEY_IS_SCHEDULED] ?: false,
            scheduledTitle = prefs[KEY_SCHEDULED_TITLE],
            triggerAtMillis = prefs[KEY_TRIGGER_AT_MILLIS]
        )
    }

    override suspend fun save(preferences: AlarmPreferences) {
        dataStore.edit { prefs ->
            prefs[KEY_MODE] = preferences.mode
            prefs[KEY_HOUR] = preferences.hour
            prefs[KEY_MINUTE] = preferences.minute
            prefs[KEY_SOUND] = preferences.soundOption
            prefs[KEY_REPEAT] = preferences.repeatOption
            prefs[KEY_EASE_ME_AWAKE] = preferences.easeMeAwake
            prefs[KEY_IS_CONFIGURED] = preferences.isConfigured
            prefs[KEY_IS_SCHEDULED] = preferences.isScheduled
            if (preferences.scheduledTitle != null) {
                prefs[KEY_SCHEDULED_TITLE] = preferences.scheduledTitle
            } else {
                prefs.remove(KEY_SCHEDULED_TITLE)
            }
            if (preferences.triggerAtMillis != null) {
                prefs[KEY_TRIGGER_AT_MILLIS] = preferences.triggerAtMillis
            } else {
                prefs.remove(KEY_TRIGGER_AT_MILLIS)
            }
        }
    }

    override suspend fun clearSchedule() {
        dataStore.edit { prefs ->
            prefs[KEY_IS_SCHEDULED] = false
            prefs.remove(KEY_TRIGGER_AT_MILLIS)
            // Keep scheduledTitle / isConfigured — timer stays saved for Sleep.
        }
    }

    private companion object {
        val KEY_MODE = stringPreferencesKey("mode")
        val KEY_HOUR = intPreferencesKey("hour")
        val KEY_MINUTE = intPreferencesKey("minute")
        val KEY_SOUND = stringPreferencesKey("sound")
        val KEY_REPEAT = stringPreferencesKey("repeat")
        val KEY_EASE_ME_AWAKE = booleanPreferencesKey("ease_me_awake")
        val KEY_IS_CONFIGURED = booleanPreferencesKey("is_configured")
        val KEY_IS_SCHEDULED = booleanPreferencesKey("is_scheduled")
        val KEY_SCHEDULED_TITLE = stringPreferencesKey("scheduled_title")
        val KEY_TRIGGER_AT_MILLIS = longPreferencesKey("trigger_at_millis")
    }
}
