package com.omniapk

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.omniapk.databinding.ActivityMainBinding
import com.omniapk.ui.apps.AppsFragment
import com.omniapk.ui.games.GamesFragment
import com.omniapk.ui.opensource.OpenSourceFragment
import com.omniapk.ui.search.SearchActivity
import com.omniapk.ui.settings.SettingsFragment
import com.omniapk.ui.updates.UpdatesFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    
    // Keep fragment instances to preserve state
    private val appsFragment = AppsFragment()
    private val gamesFragment = GamesFragment()
    private val openSourceFragment = OpenSourceFragment()
    private val updatesFragment = UpdatesFragment()
    private val settingsFragment = SettingsFragment()
    
    private var activeFragment: Fragment = appsFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupFragments()
        setupBottomNavigation()
        setupTopBar()
    }
    
    private fun setupFragments() {
        // Add all fragments but hide them initially
        supportFragmentManager.beginTransaction().apply {
            add(R.id.fragmentContainer, appsFragment, "apps")
            add(R.id.fragmentContainer, gamesFragment, "games").hide(gamesFragment)
            add(R.id.fragmentContainer, openSourceFragment, "opensource").hide(openSourceFragment)
            add(R.id.fragmentContainer, updatesFragment, "updates").hide(updatesFragment)
            add(R.id.fragmentContainer, settingsFragment, "settings").hide(settingsFragment)
        }.commit()
    }
    
    private fun setupBottomNavigation() {
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_apps -> {
                    switchFragment(appsFragment)
                    binding.tvTitle.text = getString(R.string.nav_apps)
                    true
                }
                R.id.nav_games -> {
                    switchFragment(gamesFragment)
                    binding.tvTitle.text = getString(R.string.nav_games)
                    true
                }
                R.id.nav_opensource -> {
                    switchFragment(openSourceFragment)
                    binding.tvTitle.text = getString(R.string.nav_opensource)
                    true
                }
                R.id.nav_updates -> {
                    switchFragment(updatesFragment)
                    binding.tvTitle.text = getString(R.string.nav_updates)
                    true
                }
                else -> false
            }
        }
    }
    
    private fun setupTopBar() {
        // Search icon
        binding.btnSearch.setOnClickListener {
            startActivity(Intent(this, SearchActivity::class.java))
        }
        
        // Settings icon
        binding.btnSettings.setOnClickListener {
            switchFragment(settingsFragment)
            binding.tvTitle.text = getString(R.string.nav_settings)
            binding.bottomNav.selectedItemId = -1 // Deselect bottom nav
        }
        
        // Downloads icon
        binding.btnDownloads.setOnClickListener {
            // TODO: Open downloads screen
        }
    }
    
    private fun switchFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().apply {
            hide(activeFragment)
            show(fragment)
        }.commit()
        activeFragment = fragment
    }
}
