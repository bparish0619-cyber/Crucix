package com.crucix.android.ui.settings

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.crucix.android.R
import com.crucix.android.data.CrucixConfig
import com.crucix.android.data.PreferencesManager
import com.crucix.android.databinding.FragmentSettingsBinding
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var prefsManager: PreferencesManager
    private var currentConfig = CrucixConfig()

    // Map from pref key -> EditText for batch save
    private val fieldMap = mutableMapOf<String, TextInputEditText>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefsManager = PreferencesManager(requireContext())
        currentConfig = prefsManager.loadConfig()

        buildSettingsUI()
        setupActionButtons()
        updateKeyCountBadge()
    }

    private fun buildSettingsUI() {
        val container = binding.settingsContainer
        container.removeAllViews()
        fieldMap.clear()

        // ── Server Connection ────────────────────────────────────────────────
        addGroupHeader(container, "01", "SERVER CONNECTION", "Connect to your running Crucix instance")
        addField(container, ApiKeyItem(
            label = "Server URL",
            hint = "http://192.168.1.100:3117",
            prefKey = "server_url",
            description = "Full URL to your Crucix server. Use your machine's local IP on the same network.",
            required = true
        ), currentConfig.serverUrl)

        addField(container, ApiKeyItem(
            label = "Port",
            hint = "3117",
            prefKey = "port",
            description = "Server port (default: 3117). Change only if you modified PORT in .env."
        ), currentConfig.port)

        addField(container, ApiKeyItem(
            label = "Refresh Interval (minutes)",
            hint = "15",
            prefKey = "refresh_interval_minutes",
            description = "How often the server sweeps all 26 sources."
        ), currentConfig.refreshIntervalMinutes)

        addDivider(container)

        // ── Core Data Sources ─────────────────────────────────────────────────
        addGroupHeader(container, "02", "CORE DATA SOURCES", "Free keys — unlock the highest-value intelligence feeds")

        addField(container, ApiKeyItem(
            label = "FRED API Key",
            hint = "Your FRED API key",
            prefKey = "fred_api_key",
            getKeyUrl = "https://fred.stlouisfed.org/docs/api/api_key.html",
            getKeyLabel = "Register at FRED",
            description = "Federal Reserve Economic Data — yield curve, CPI, VIX, M2, fed funds rate. Instant, free registration.",
            required = false
        ), currentConfig.fredApiKey)

        addField(container, ApiKeyItem(
            label = "NASA FIRMS Map Key",
            hint = "Your FIRMS map key",
            prefKey = "firms_map_key",
            getKeyUrl = "https://firms.modaps.eosdis.nasa.gov/api/area/",
            getKeyLabel = "Register at NASA FIRMS",
            description = "Satellite fire and thermal anomaly detection (3hr latency). Instant, free.",
            required = false
        ), currentConfig.firmsMapKey)

        addField(container, ApiKeyItem(
            label = "EIA API Key",
            hint = "Your EIA API key",
            prefKey = "eia_api_key",
            getKeyUrl = "https://www.eia.gov/opendata/register.php",
            getKeyLabel = "Register at EIA",
            description = "US Energy Information Administration — WTI/Brent crude, natural gas, inventories. Instant, free.",
            required = false
        ), currentConfig.eiaApiKey)

        addDivider(container)

        // ── Extended Data Sources ─────────────────────────────────────────────
        addGroupHeader(container, "03", "EXTENDED DATA SOURCES", "Additional intelligence layers")

        addField(container, ApiKeyItem(
            label = "ACLED Email",
            hint = "your@email.com",
            prefKey = "acled_email",
            getKeyUrl = "https://acleddata.com/register/",
            getKeyLabel = "Register at ACLED",
            description = "Armed Conflict Location & Event Data — battles, explosions, protests. Free OAuth2.",
            required = false
        ), currentConfig.acledEmail)

        addField(container, ApiKeyItem(
            label = "ACLED Password",
            hint = "ACLED account password",
            prefKey = "acled_password",
            isPassword = true,
            description = "Password for your ACLED account (used with ACLED Email above).",
            required = false
        ), currentConfig.acledPassword)

        addField(container, ApiKeyItem(
            label = "AISstream API Key",
            hint = "Your AISstream key",
            prefKey = "aisstream_api_key",
            getKeyUrl = "https://aisstream.io/",
            getKeyLabel = "Register at AISstream",
            description = "Maritime AIS vessel tracking — dark ships, sanctions evasion detection. Free.",
            required = false
        ), currentConfig.aisstreamApiKey)

        addField(container, ApiKeyItem(
            label = "ADS-B Exchange API Key",
            hint = "Your RapidAPI key for ADS-B",
            prefKey = "adsb_api_key",
            getKeyUrl = "https://rapidapi.com/adsbexchange/api/adsbexchange-com1",
            getKeyLabel = "Get on RapidAPI (~\$10/mo)",
            description = "Unfiltered flight tracking including military aircraft. Paid tier via RapidAPI.",
            required = false
        ), currentConfig.adsbApiKey)

        addDivider(container)

        // ── LLM Integration ───────────────────────────────────────────────────
        addGroupHeader(container, "04", "LLM INTEGRATION", "AI-enhanced signal analysis and trade ideas (optional)")

        addDropdownField(container, ApiKeyItem(
            label = "LLM Provider",
            hint = "Select provider",
            prefKey = "llm_provider",
            isDropdown = true,
            dropdownOptions = listOf("(disabled)", "anthropic", "openai", "gemini", "codex"),
            description = "AI provider for enhanced alert evaluation and trade idea generation. Leave disabled if not needed."
        ), currentConfig.llmProvider)

        addField(container, ApiKeyItem(
            label = "LLM API Key",
            hint = "Provider API key",
            prefKey = "llm_api_key",
            isPassword = true,
            getKeyUrl = null,
            description = "API key for your selected LLM provider. Not needed for 'codex' (uses local auth).",
            required = false
        ), currentConfig.llmApiKey, showProviderLinks = true)

        addField(container, ApiKeyItem(
            label = "LLM Model Override",
            hint = "e.g. claude-sonnet-4-6 (leave blank for default)",
            prefKey = "llm_model",
            description = "Override the default model. Leave blank to use the provider's default.",
            required = false
        ), currentConfig.llmModel)

        addDivider(container)

        // ── Telegram ──────────────────────────────────────────────────────────
        addGroupHeader(container, "05", "TELEGRAM BOT", "Two-way intelligence alerts and commands (optional)")

        addField(container, ApiKeyItem(
            label = "Bot Token",
            hint = "123456789:ABCdef...",
            prefKey = "telegram_bot_token",
            isPassword = true,
            getKeyUrl = "https://t.me/BotFather",
            getKeyLabel = "Create via @BotFather",
            description = "Create a new bot at @BotFather on Telegram to get this token.",
            required = false
        ), currentConfig.telegramBotToken)

        addField(container, ApiKeyItem(
            label = "Chat ID",
            hint = "Your Telegram chat ID",
            prefKey = "telegram_chat_id",
            getKeyUrl = "https://t.me/userinfobot",
            getKeyLabel = "Get via @userinfobot",
            description = "Your personal Telegram chat ID. Message @userinfobot to get it.",
            required = false
        ), currentConfig.telegramChatId)

        addField(container, ApiKeyItem(
            label = "Extra Channels (optional)",
            hint = "channel1,channel2",
            prefKey = "telegram_channels",
            description = "Comma-separated Telegram channel IDs to monitor beyond the 17 built-in OSINT channels.",
            required = false
        ), currentConfig.telegramChannels)

        addField(container, ApiKeyItem(
            label = "Poll Interval (ms)",
            hint = "5000",
            prefKey = "telegram_poll_interval",
            description = "How often to poll for bot commands in milliseconds. Default: 5000.",
            required = false
        ), currentConfig.telegramPollInterval)

        addDivider(container)

        // ── Discord ───────────────────────────────────────────────────────────
        addGroupHeader(container, "06", "DISCORD BOT", "Rich embed alerts with slash commands (optional)")

        addField(container, ApiKeyItem(
            label = "Bot Token",
            hint = "Discord bot token",
            prefKey = "discord_bot_token",
            isPassword = true,
            getKeyUrl = "https://discord.com/developers/applications",
            getKeyLabel = "Discord Developer Portal",
            description = "Create an application at the Discord Developer Portal → Bot → Reset Token.",
            required = false
        ), currentConfig.discordBotToken)

        addField(container, ApiKeyItem(
            label = "Channel ID",
            hint = "Discord channel ID",
            prefKey = "discord_channel_id",
            getKeyUrl = null,
            description = "Right-click the target channel in Discord (Developer Mode on) → Copy Channel ID.",
            required = false
        ), currentConfig.discordChannelId)

        addField(container, ApiKeyItem(
            label = "Guild ID (optional)",
            hint = "Discord server ID",
            prefKey = "discord_guild_id",
            description = "Right-click server → Copy Server ID. Enables instant slash command registration vs. up to 1hr global delay.",
            required = false
        ), currentConfig.discordGuildId)

        addField(container, ApiKeyItem(
            label = "Webhook URL (alert-only fallback)",
            hint = "https://discord.com/api/webhooks/...",
            prefKey = "discord_webhook_url",
            isPassword = false,
            description = "Channel Settings → Integrations → Webhooks. Use instead of bot token for one-way alerts only.",
            required = false
        ), currentConfig.discordWebhookUrl)
    }

    private fun addGroupHeader(container: LinearLayout, number: String, title: String, subtitle: String) {
        val view = layoutInflater.inflate(R.layout.item_group_header, container, false)
        view.findViewById<TextView>(R.id.tvGroupNumber).text = number
        view.findViewById<TextView>(R.id.tvGroupTitle).text = title
        view.findViewById<TextView>(R.id.tvGroupSubtitle).text = subtitle
        container.addView(view)
    }

    private fun addDivider(container: LinearLayout) {
        val view = View(requireContext())
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 1
        ).apply {
            topMargin = resources.getDimensionPixelSize(R.dimen.spacing_medium)
            bottomMargin = resources.getDimensionPixelSize(R.dimen.spacing_medium)
        }
        view.layoutParams = params
        view.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.surface_border))
        container.addView(view)
    }

    private fun addField(
        container: LinearLayout,
        item: ApiKeyItem,
        currentValue: String,
        showProviderLinks: Boolean = false
    ) {
        val view = layoutInflater.inflate(R.layout.item_api_key_field, container, false)

        val tilInput = view.findViewById<TextInputLayout>(R.id.tilApiKey)
        val etInput = view.findViewById<TextInputEditText>(R.id.etApiKey)
        val tvLabel = view.findViewById<TextView>(R.id.tvFieldLabel)
        val tvDescription = view.findViewById<TextView>(R.id.tvFieldDescription)
        val btnGetKey = view.findViewById<Button>(R.id.btnGetKey)

        tvLabel.text = item.label
        etInput.hint = item.hint
        etInput.setText(currentValue)

        if (item.description.isNotBlank()) {
            tvDescription.visibility = View.VISIBLE
            tvDescription.text = item.description
        } else {
            tvDescription.visibility = View.GONE
        }

        if (item.isPassword) {
            tilInput.endIconMode = TextInputLayout.END_ICON_PASSWORD_TOGGLE
            etInput.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }

        if (item.getKeyUrl != null) {
            btnGetKey.visibility = View.VISIBLE
            btnGetKey.text = item.getKeyLabel
            btnGetKey.setOnClickListener {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(item.getKeyUrl)))
            }
        } else if (showProviderLinks) {
            btnGetKey.visibility = View.VISIBLE
            btnGetKey.text = "Provider Keys"
            btnGetKey.setOnClickListener { showProviderLinksDialog() }
        } else {
            btnGetKey.visibility = View.GONE
        }

        fieldMap[item.prefKey] = etInput
        container.addView(view)
    }

    private fun addDropdownField(container: LinearLayout, item: ApiKeyItem, currentValue: String) {
        val view = layoutInflater.inflate(R.layout.item_api_key_dropdown, container, false)

        val tvLabel = view.findViewById<TextView>(R.id.tvFieldLabel)
        val tvDescription = view.findViewById<TextView>(R.id.tvFieldDescription)
        val spinner = view.findViewById<Spinner>(R.id.spinnerValue)

        tvLabel.text = item.label

        if (item.description.isNotBlank()) {
            tvDescription.visibility = View.VISIBLE
            tvDescription.text = item.description
        } else {
            tvDescription.visibility = View.GONE
        }

        val adapter = ArrayAdapter(
            requireContext(),
            R.layout.item_spinner,
            item.dropdownOptions
        )
        adapter.setDropDownViewResource(R.layout.item_spinner_dropdown)
        spinner.adapter = adapter

        val selectedIndex = item.dropdownOptions.indexOfFirst {
            it == currentValue
        }.takeIf { it >= 0 } ?: 0
        spinner.setSelection(selectedIndex)

        // Store spinner selection via a hidden EditText
        val hiddenEt = TextInputEditText(requireContext())
        hiddenEt.setText(currentValue)
        hiddenEt.visibility = View.GONE
        view.addView(hiddenEt)

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, v: View?, position: Int, id: Long) {
                val selected = item.dropdownOptions[position]
                hiddenEt.setText(if (selected == "(disabled)") "" else selected)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        fieldMap[item.prefKey] = hiddenEt
        container.addView(view)
    }

    private fun showProviderLinksDialog() {
        val providers = arrayOf(
            "Anthropic (Claude) - console.anthropic.com",
            "OpenAI (GPT) - platform.openai.com",
            "Google Gemini - aistudio.google.com",
            "OpenAI Codex (ChatGPT sub) - openai.com"
        )
        val urls = arrayOf(
            "https://console.anthropic.com/settings/keys",
            "https://platform.openai.com/api-keys",
            "https://aistudio.google.com/app/apikey",
            "https://openai.com"
        )
        AlertDialog.Builder(requireContext(), R.style.CrucixAlertDialog)
            .setTitle("Get LLM API Key")
            .setItems(providers) { _, which ->
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(urls[which])))
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun setupActionButtons() {
        binding.btnSave.setOnClickListener { saveConfig() }
        binding.btnCopyEnv.setOnClickListener { copyEnvToClipboard() }
        binding.btnClearAll.setOnClickListener { confirmClearAll() }
    }

    private fun saveConfig() {
        fun field(key: String) = fieldMap[key]?.text?.toString() ?: ""

        val config = CrucixConfig(
            serverUrl = field("server_url"),
            port = field("port"),
            refreshIntervalMinutes = field("refresh_interval_minutes"),
            fredApiKey = field("fred_api_key"),
            firmsMapKey = field("firms_map_key"),
            eiaApiKey = field("eia_api_key"),
            acledEmail = field("acled_email"),
            acledPassword = field("acled_password"),
            aisstreamApiKey = field("aisstream_api_key"),
            adsbApiKey = field("adsb_api_key"),
            llmProvider = field("llm_provider"),
            llmApiKey = field("llm_api_key"),
            llmModel = field("llm_model"),
            telegramBotToken = field("telegram_bot_token"),
            telegramChatId = field("telegram_chat_id"),
            telegramChannels = field("telegram_channels"),
            telegramPollInterval = field("telegram_poll_interval"),
            discordBotToken = field("discord_bot_token"),
            discordChannelId = field("discord_channel_id"),
            discordGuildId = field("discord_guild_id"),
            discordWebhookUrl = field("discord_webhook_url")
        )

        prefsManager.saveConfig(config)
        currentConfig = config
        updateKeyCountBadge()

        Snackbar.make(binding.root, "Configuration saved", Snackbar.LENGTH_SHORT)
            .setBackgroundTint(ContextCompat.getColor(requireContext(), R.color.crucix_cyan_dark))
            .setTextColor(ContextCompat.getColor(requireContext(), R.color.background_dark))
            .show()
    }

    private fun copyEnvToClipboard() {
        // Save first, then copy
        saveConfig()

        val envContent = currentConfig.toEnvString()
        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("crucix .env", envContent))

        Snackbar.make(binding.root, ".env content copied to clipboard — paste into your server's .env file", Snackbar.LENGTH_LONG)
            .setBackgroundTint(ContextCompat.getColor(requireContext(), R.color.crucix_cyan_dark))
            .setTextColor(ContextCompat.getColor(requireContext(), R.color.background_dark))
            .show()
    }

    private fun confirmClearAll() {
        AlertDialog.Builder(requireContext(), R.style.CrucixAlertDialog)
            .setTitle("Clear All Keys?")
            .setMessage("This will permanently delete all stored API keys and configuration. This cannot be undone.")
            .setPositiveButton("Clear") { _, _ ->
                prefsManager.clearAll()
                currentConfig = CrucixConfig()
                buildSettingsUI()
                updateKeyCountBadge()
                Snackbar.make(binding.root, "All keys cleared", Snackbar.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateKeyCountBadge() {
        val count = currentConfig.getConfiguredKeyCount()
        binding.tvKeyCount.text = "$count / 10 keys configured"
        binding.progressKeys.progress = (count * 100) / 10
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
