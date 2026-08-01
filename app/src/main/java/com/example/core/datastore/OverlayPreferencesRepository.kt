package com.example.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "tft_overlay_settings")

data class OverlaySettings(
    val posX: Int = 100,
    val posY: Int = 300,
    val alpha: Float = 0.90f,
    val scale: Float = 1.0f,
    val touchPassthrough: Boolean = false,
    val isLocked: Boolean = false,
    val language: String = "vi",
    val playerLevel: Int = 7,
    val riotApiKey: String = "",
    val proxyBackendUrl: String = "https://tft-proxy-demo.workers.dev",
    val selectedClientVersion: String = "TFT_VNG",
    val activeSetVersion: String = "Set 13 (Live)"
)

class OverlayPreferencesRepository(private val context: Context) {

    companion object {
        private val KEY_POS_X = intPreferencesKey("pos_x")
        private val KEY_POS_Y = intPreferencesKey("pos_y")
        private val KEY_ALPHA = floatPreferencesKey("alpha")
        private val KEY_SCALE = floatPreferencesKey("scale")
        private val KEY_PASSTHROUGH = booleanPreferencesKey("touch_passthrough")
        private val KEY_LOCKED = booleanPreferencesKey("is_locked")
        private val KEY_LANGUAGE = stringPreferencesKey("language")
        private val KEY_PLAYER_LEVEL = intPreferencesKey("player_level")
        private val KEY_RIOT_API_KEY = stringPreferencesKey("riot_api_key")
        private val KEY_PROXY_URL = stringPreferencesKey("proxy_backend_url")
        private val KEY_CLIENT_VERSION = stringPreferencesKey("selected_client_version")
        private val KEY_ACTIVE_SET = stringPreferencesKey("active_set_version")
    }

    val overlaySettingsFlow: Flow<OverlaySettings> = context.dataStore.data.map { prefs ->
        OverlaySettings(
            posX = prefs[KEY_POS_X] ?: 100,
            posY = prefs[KEY_POS_Y] ?: 300,
            alpha = prefs[KEY_ALPHA] ?: 0.90f,
            scale = prefs[KEY_SCALE] ?: 1.0f,
            touchPassthrough = prefs[KEY_PASSTHROUGH] ?: false,
            isLocked = prefs[KEY_LOCKED] ?: false,
            language = prefs[KEY_LANGUAGE] ?: "vi",
            playerLevel = prefs[KEY_PLAYER_LEVEL] ?: 7,
            riotApiKey = prefs[KEY_RIOT_API_KEY] ?: "",
            proxyBackendUrl = prefs[KEY_PROXY_URL] ?: "https://tft-proxy-demo.workers.dev",
            selectedClientVersion = prefs[KEY_CLIENT_VERSION] ?: "TFT_VNG",
            activeSetVersion = prefs[KEY_ACTIVE_SET] ?: "Set 13 (Live)"
        )
    }

    suspend fun updateClientVersion(version: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_CLIENT_VERSION] = version
        }
    }

    suspend fun updateActiveSetVersion(setVersion: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_ACTIVE_SET] = setVersion
        }
    }

    suspend fun updatePosition(x: Int, y: Int) {
        context.dataStore.edit { prefs ->
            prefs[KEY_POS_X] = x
            prefs[KEY_POS_Y] = y
        }
    }

    suspend fun updateAlpha(alpha: Float) {
        context.dataStore.edit { prefs ->
            prefs[KEY_ALPHA] = alpha.coerceIn(0.3f, 1.0f)
        }
    }

    suspend fun updateScale(scale: Float) {
        context.dataStore.edit { prefs ->
            prefs[KEY_SCALE] = scale.coerceIn(0.7f, 1.3f)
        }
    }

    suspend fun updateTouchPassthrough(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_PASSTHROUGH] = enabled
        }
    }

    suspend fun updateLocked(locked: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_LOCKED] = locked
        }
    }

    suspend fun updateLanguage(lang: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_LANGUAGE] = lang
        }
    }

    suspend fun updatePlayerLevel(level: Int) {
        context.dataStore.edit { prefs ->
            prefs[KEY_PLAYER_LEVEL] = level.coerceIn(1, 11)
        }
    }

    suspend fun updateRiotApiKey(apiKey: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_RIOT_API_KEY] = apiKey
        }
    }

    suspend fun updateProxyUrl(url: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_PROXY_URL] = url
        }
    }
}
