package com.crucix.android

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.crucix.android.databinding.ActivityMainBinding
import com.crucix.android.ui.dashboard.DashboardFragment
import com.crucix.android.ui.help.HelpFragment
import com.crucix.android.ui.settings.SettingsFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val dashboardFragment = DashboardFragment()
    private val settingsFragment = SettingsFragment()
    private val helpFragment = HelpFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupBottomNavigation()

        if (savedInstanceState == null) {
            loadFragment(dashboardFragment, "dashboard")
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dashboard -> {
                    loadFragment(dashboardFragment, "dashboard")
                    true
                }
                R.id.nav_settings -> {
                    loadFragment(settingsFragment, "settings")
                    true
                }
                R.id.nav_help -> {
                    loadFragment(helpFragment, "help")
                    true
                }
                else -> false
            }
        }
    }

    private fun loadFragment(fragment: Fragment, tag: String) {
        val existing = supportFragmentManager.findFragmentByTag(tag)
        val tx = supportFragmentManager.beginTransaction()
        supportFragmentManager.fragments.forEach { tx.hide(it) }
        if (existing == null) {
            tx.add(R.id.fragment_container, fragment, tag)
        } else {
            tx.show(existing)
        }
        tx.commit()
    }

    fun setStatusBarVisibility(visible: Boolean) {
        supportActionBar?.let {
            if (visible) it.show() else it.hide()
        }
    }
}
