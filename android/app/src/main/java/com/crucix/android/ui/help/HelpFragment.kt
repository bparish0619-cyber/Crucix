package com.crucix.android.ui.help

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import com.crucix.android.R
import com.crucix.android.databinding.FragmentHelpBinding

data class FaqItem(
    val question: String,
    val answer: String,
    var isExpanded: Boolean = false
)

class HelpFragment : Fragment() {

    private var _binding: FragmentHelpBinding? = null
    private val binding get() = _binding!!

    private val faqItems = listOf(
        FaqItem(
            "What is Crucix?",
            "Crucix is a self-hosted OSINT (Open-Source Intelligence) dashboard that aggregates data from 26 real-time sources including satellite fire detection, flight tracking, maritime AIS, conflict events, economic indicators, radiation monitoring, and social sentiment — and renders them on a Jarvis-style web dashboard. It runs as a Node.js server on your own machine, polling all sources every 15 minutes."
        ),
        FaqItem(
            "Do I need all the API keys to use Crucix?",
            "No. Crucix works with zero API keys — 18+ sources require no authentication. The three highest-value free keys are FRED (economic data), NASA FIRMS (satellite fire/thermal), and EIA (energy prices). Each takes about 60 seconds to register. Sources that lack keys return structured errors and the sweep continues normally."
        ),
        FaqItem(
            "How do I run Crucix?",
            "1. Install Node.js 22+ on your machine\n2. Clone your fork: git clone https://github.com/bparish0619-cyber/Crucix\n3. cd Crucix\n4. npm install\n5. Copy .env.example to .env and add your keys\n6. Run: node server.mjs\n\nThe dashboard will be available at http://localhost:3117. To access it from this app over your local network, use your machine's IP address (e.g., http://192.168.1.100:3117)."
        ),
        FaqItem(
            "How do I find my server's IP address?",
            "On the machine running Crucix:\n\n• Windows: Open Command Prompt → type ipconfig → look for IPv4 Address under your active adapter (usually 192.168.x.x)\n\n• macOS: System Settings → Network → your connection → shows IP address\n\n• Linux: Open terminal → type ip addr or hostname -I\n\nYour phone and computer must be on the same WiFi network."
        ),
        FaqItem(
            "The dashboard shows empty panels after loading. Is that normal?",
            "Yes, this is expected behavior on first launch. The initial sweep queries all 26 sources in parallel and takes 30–60 seconds to complete. The dashboard will populate automatically once the sweep finishes and pushes data via Server-Sent Events (SSE). After that, it auto-refreshes every 15 minutes."
        ),
        FaqItem(
            "npm run dev exits with no output. What's wrong?",
            "This is a known npm issue on some systems (especially Windows PowerShell) where errors are swallowed. Try these:\n\n1. Run directly: node --trace-warnings server.mjs\n2. Run diagnostics: node diag.mjs (checks Node version, imports, port availability)\n3. Check Node.js version: node --version — requires 22+\n4. Check if port 3117 is in use:\n   Windows: netstat -ano | findstr 3117\n   macOS/Linux: lsof -ti:3117"
        ),
        FaqItem(
            "How do I access Crucix from outside my home network?",
            "You have several options:\n\n• VPN: Set up WireGuard or OpenVPN on your home router to access your home network remotely\n• Cloudflare Tunnel: Free, no port forwarding needed — creates a secure public URL pointing to your Crucix server\n• Railway / Render: Deploy Crucix to a cloud platform using the included Dockerfile. Update your server URL in Settings accordingly.\n\nNever directly expose port 3117 to the internet without authentication."
        ),
        FaqItem(
            "How do I set up the Telegram bot?",
            "1. Open Telegram and message @BotFather\n2. Send /newbot and follow prompts to create your bot\n3. Copy the bot token → paste into Telegram Bot Token in Settings\n4. Message @userinfobot to get your Chat ID → paste into Telegram Chat ID\n5. Start your bot (message it once from your account)\n6. Add TELEGRAM_BOT_TOKEN and TELEGRAM_CHAT_ID to your server's .env file\n7. Restart the Crucix server\n\nOnce running, send /help to your bot from Telegram."
        ),
        FaqItem(
            "How do I set up the Discord bot?",
            "1. Go to discord.com/developers/applications → New Application\n2. Go to Bot → Reset Token → copy token to Discord Bot Token in Settings\n3. Enable Message Content Intent under Privileged Gateway Intents\n4. OAuth2 → URL Generator: select 'bot' + 'applications.commands' scopes, 'Send Messages' + 'Embed Links' permissions\n5. Open the generated URL to invite the bot to your server\n6. Right-click your alerts channel → Copy Channel ID → Discord Channel ID in Settings\n7. (Optional) Right-click server → Copy Server ID → Discord Guild ID for instant slash command registration\n8. On your server: npm install discord.js\n9. Add all Discord keys to .env and restart Crucix\n\nAlternative (alerts only, no bot): set only DISCORD_WEBHOOK_URL."
        ),
        FaqItem(
            "How does the .env export work in this app?",
            "The 'Copy .env' button in Settings saves your current configuration and generates the complete .env file content in your clipboard. You can then paste it directly into the .env file on your Crucix server.\n\nAll keys are stored encrypted on your device using Android's EncryptedSharedPreferences (AES-256-GCM). They never leave your device except when you explicitly copy/share them."
        ),
        FaqItem(
            "Which LLM provider should I use?",
            "All four providers (Anthropic Claude, OpenAI GPT, Google Gemini, OpenAI Codex) enable the same two features: smarter FLASH/PRIORITY/ROUTINE alert classification and AI trade idea generation.\n\n• Anthropic Claude: Best context understanding, strong at cross-domain correlation\n• OpenAI GPT: Widely used, good trade idea quality\n• Google Gemini: Free tier available via AI Studio\n• OpenAI Codex: Free if you have a ChatGPT subscription (uses local auth, no API key needed)\n\nIf you're unsure, start with Gemini for free testing or Codex if you already have ChatGPT Plus."
        ),
        FaqItem(
            "Some sources show errors in the dashboard. What does that mean?",
            "Structured source errors are expected and intentional — sources that require missing API keys return a structured error object and the sweep continues. Check the Source Integrity section on the dashboard to see which sources failed and why.\n\nThe most common causes:\n• Missing API key for that source (add it in Settings → Copy .env → update server .env)\n• Source API is temporarily down or rate-limited (auto-recovers next sweep)\n• Network connectivity issue from the server"
        )
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHelpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        buildFaqList()
        setupLinks()
    }

    private fun buildFaqList() {
        val container = binding.faqContainer
        container.removeAllViews()

        faqItems.forEachIndexed { index, item ->
            val faqView = layoutInflater.inflate(R.layout.item_faq, container, false)

            val tvQuestion = faqView.findViewById<android.widget.TextView>(R.id.tvQuestion)
            val tvAnswer = faqView.findViewById<android.widget.TextView>(R.id.tvAnswer)
            val ivChevron = faqView.findViewById<android.widget.ImageView>(R.id.ivChevron)
            val headerRow = faqView.findViewById<View>(R.id.faqHeader)
            val answerLayout = faqView.findViewById<View>(R.id.answerLayout)

            tvQuestion.text = item.question
            tvAnswer.text = item.answer

            answerLayout.visibility = if (item.isExpanded) View.VISIBLE else View.GONE
            ivChevron.rotation = if (item.isExpanded) 180f else 0f

            headerRow.setOnClickListener {
                item.isExpanded = !item.isExpanded
                if (item.isExpanded) {
                    answerLayout.visibility = View.VISIBLE
                    ivChevron.animate().rotation(180f).setDuration(200).start()
                } else {
                    answerLayout.visibility = View.GONE
                    ivChevron.animate().rotation(0f).setDuration(200).start()
                }
            }

            container.addView(faqView)
        }
    }

    private fun setupLinks() {
        binding.btnOpenRepo.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/bparish0619-cyber/Crucix")))
        }

        binding.btnOpenUpstream.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/calesthio/Crucix")))
        }

        binding.btnOpenNodeJs.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://nodejs.org/")))
        }

        binding.btnRailwayDeploy.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://railway.com/deploy/crucix")))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
