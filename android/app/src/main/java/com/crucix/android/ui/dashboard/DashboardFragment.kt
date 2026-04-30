package com.crucix.android.ui.dashboard

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.crucix.android.R
import com.crucix.android.data.PreferencesManager
import com.crucix.android.databinding.FragmentDashboardBinding
import com.google.android.material.snackbar.Snackbar

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private lateinit var prefsManager: PreferencesManager
    private var serverUrl: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefsManager = PreferencesManager(requireContext())
        setupWebView()
        setupUI()
        loadDashboard()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        binding.webView.apply {
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                loadWithOverviewMode = true
                useWideViewPort = true
                setSupportZoom(true)
                builtInZoomControls = true
                displayZoomControls = false
                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                cacheMode = WebSettings.LOAD_DEFAULT
                mediaPlaybackRequiresUserGesture = false
            }

            webViewClient = object : WebViewClient() {
                override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                    binding.loadingIndicator.visibility = View.VISIBLE
                    binding.errorLayout.visibility = View.GONE
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    binding.swipeRefresh.isRefreshing = false
                    binding.loadingIndicator.visibility = View.GONE
                }

                override fun onReceivedError(
                    view: WebView?,
                    request: WebResourceRequest?,
                    error: WebResourceError?
                ) {
                    if (request?.isForMainFrame == true) {
                        binding.loadingIndicator.visibility = View.GONE
                        binding.swipeRefresh.isRefreshing = false
                        showErrorState()
                    }
                }

                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {
                    val url = request?.url?.toString() ?: return false
                    // Keep internal Crucix navigation in WebView; open external links in browser
                    return if (url.startsWith(serverUrl)) {
                        false
                    } else {
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                        true
                    }
                }
            }

            webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    binding.progressBar.progress = newProgress
                    binding.progressBar.visibility = if (newProgress < 100) View.VISIBLE else View.GONE
                }
            }
        }
    }

    private fun setupUI() {
        binding.swipeRefresh.apply {
            setColorSchemeResources(R.color.crucix_cyan, R.color.crucix_teal)
            setBackgroundColor(resources.getColor(R.color.background_dark, null))
            setOnRefreshListener { loadDashboard() }
        }

        binding.btnRetry.setOnClickListener { loadDashboard() }

        binding.btnOpenSettings.setOnClickListener {
            (activity as? com.crucix.android.MainActivity)?.let {
                it.binding.bottomNavigation.selectedItemId = R.id.nav_settings
            }
        }

        binding.fabOpenBrowser.setOnClickListener {
            if (serverUrl.isNotBlank()) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(serverUrl))
                startActivity(intent)
            }
        }

        binding.btnOpenApi.setOnClickListener {
            if (serverUrl.isNotBlank()) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("$serverUrl/api/health"))
                startActivity(intent)
            }
        }
    }

    private fun loadDashboard() {
        serverUrl = prefsManager.getServerUrl()
        binding.tvServerUrl.text = serverUrl

        if (serverUrl.isBlank()) {
            showErrorState("No server URL configured. Go to Settings to add your Crucix server address.")
            return
        }

        binding.errorLayout.visibility = View.GONE
        binding.webView.visibility = View.VISIBLE
        binding.webView.loadUrl(serverUrl)
    }

    private fun showErrorState(message: String = "Cannot connect to Crucix server.\nVerify the server is running and the URL in Settings is correct.") {
        binding.webView.visibility = View.GONE
        binding.errorLayout.visibility = View.VISIBLE
        binding.tvErrorMessage.text = message
    }

    override fun onResume() {
        super.onResume()
        // Reload if server URL changed in settings
        val currentUrl = prefsManager.getServerUrl()
        if (currentUrl != serverUrl) {
            loadDashboard()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.webView.destroy()
        _binding = null
    }
}
