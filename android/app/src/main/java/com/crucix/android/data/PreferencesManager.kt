package com.crucix.android.data

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class PreferencesManager(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "crucix_secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    companion object {
        private const val KEY_SERVER_URL = "server_url"
        private const val KEY_FRED_API = "fred_api_key"
        private const val KEY_FIRMS_MAP = "firms_map_key"
        private const val KEY_EIA_API = "eia_api_key"
        private const val KEY_ACLED_EMAIL = "acled_email"
        private const val KEY_ACLED_PASSWORD = "acled_password"
        private const val KEY_AISSTREAM_API = "aisstream_api_key"
        private const val KEY_ADSB_API = "adsb_api_key"
        private const val KEY_LLM_PROVIDER = "llm_provider"
        private const val KEY_LLM_API = "llm_api_key"
        private const val KEY_LLM_MODEL = "llm_model"
        private const val KEY_TELEGRAM_TOKEN = "telegram_bot_token"
        private const val KEY_TELEGRAM_CHAT_ID = "telegram_chat_id"
        private const val KEY_TELEGRAM_CHANNELS = "telegram_channels"
        private const val KEY_TELEGRAM_POLL_INTERVAL = "telegram_poll_interval"
        private const val KEY_DISCORD_BOT_TOKEN = "discord_bot_token"
        private const val KEY_DISCORD_CHANNEL_ID = "discord_channel_id"
        private const val KEY_DISCORD_GUILD_ID = "discord_guild_id"
        private const val KEY_DISCORD_WEBHOOK = "discord_webhook_url"
        private const val KEY_PORT = "port"
        private const val KEY_REFRESH_INTERVAL = "refresh_interval_minutes"
    }

    fun saveConfig(config: CrucixConfig) {
        prefs.edit().apply {
            putString(KEY_SERVER_URL, config.serverUrl)
            putString(KEY_FRED_API, config.fredApiKey)
            putString(KEY_FIRMS_MAP, config.firmsMapKey)
            putString(KEY_EIA_API, config.eiaApiKey)
            putString(KEY_ACLED_EMAIL, config.acledEmail)
            putString(KEY_ACLED_PASSWORD, config.acledPassword)
            putString(KEY_AISSTREAM_API, config.aisstreamApiKey)
            putString(KEY_ADSB_API, config.adsbApiKey)
            putString(KEY_LLM_PROVIDER, config.llmProvider)
            putString(KEY_LLM_API, config.llmApiKey)
            putString(KEY_LLM_MODEL, config.llmModel)
            putString(KEY_TELEGRAM_TOKEN, config.telegramBotToken)
            putString(KEY_TELEGRAM_CHAT_ID, config.telegramChatId)
            putString(KEY_TELEGRAM_CHANNELS, config.telegramChannels)
            putString(KEY_TELEGRAM_POLL_INTERVAL, config.telegramPollInterval)
            putString(KEY_DISCORD_BOT_TOKEN, config.discordBotToken)
            putString(KEY_DISCORD_CHANNEL_ID, config.discordChannelId)
            putString(KEY_DISCORD_GUILD_ID, config.discordGuildId)
            putString(KEY_DISCORD_WEBHOOK, config.discordWebhookUrl)
            putString(KEY_PORT, config.port)
            putString(KEY_REFRESH_INTERVAL, config.refreshIntervalMinutes)
            apply()
        }
    }

    fun loadConfig(): CrucixConfig {
        return CrucixConfig(
            serverUrl = prefs.getString(KEY_SERVER_URL, "http://192.168.1.100:3117") ?: "http://192.168.1.100:3117",
            fredApiKey = prefs.getString(KEY_FRED_API, "") ?: "",
            firmsMapKey = prefs.getString(KEY_FIRMS_MAP, "") ?: "",
            eiaApiKey = prefs.getString(KEY_EIA_API, "") ?: "",
            acledEmail = prefs.getString(KEY_ACLED_EMAIL, "") ?: "",
            acledPassword = prefs.getString(KEY_ACLED_PASSWORD, "") ?: "",
            aisstreamApiKey = prefs.getString(KEY_AISSTREAM_API, "") ?: "",
            adsbApiKey = prefs.getString(KEY_ADSB_API, "") ?: "",
            llmProvider = prefs.getString(KEY_LLM_PROVIDER, "") ?: "",
            llmApiKey = prefs.getString(KEY_LLM_API, "") ?: "",
            llmModel = prefs.getString(KEY_LLM_MODEL, "") ?: "",
            telegramBotToken = prefs.getString(KEY_TELEGRAM_TOKEN, "") ?: "",
            telegramChatId = prefs.getString(KEY_TELEGRAM_CHAT_ID, "") ?: "",
            telegramChannels = prefs.getString(KEY_TELEGRAM_CHANNELS, "") ?: "",
            telegramPollInterval = prefs.getString(KEY_TELEGRAM_POLL_INTERVAL, "5000") ?: "5000",
            discordBotToken = prefs.getString(KEY_DISCORD_BOT_TOKEN, "") ?: "",
            discordChannelId = prefs.getString(KEY_DISCORD_CHANNEL_ID, "") ?: "",
            discordGuildId = prefs.getString(KEY_DISCORD_GUILD_ID, "") ?: "",
            discordWebhookUrl = prefs.getString(KEY_DISCORD_WEBHOOK, "") ?: "",
            port = prefs.getString(KEY_PORT, "3117") ?: "3117",
            refreshIntervalMinutes = prefs.getString(KEY_REFRESH_INTERVAL, "15") ?: "15"
        )
    }

    fun clearAll() {
        prefs.edit().clear().apply()
    }

    fun getServerUrl(): String {
        return prefs.getString(KEY_SERVER_URL, "http://192.168.1.100:3117") ?: "http://192.168.1.100:3117"
    }
}
