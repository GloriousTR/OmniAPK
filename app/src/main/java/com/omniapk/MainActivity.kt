package com.omniapk

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.omniapk.databinding.ActivityMainBinding
import com.omniapk.ui.home.HomeFragment
import com.omniapk.ui.opensource.OpenSourceFragment
import com.omniapk.ui.search.SearchFragment
import com.omniapk.ui.settings.SettingsFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    
    // Keep fragment instances to preserve state
    private val homeFragment = HomeFragment()
    private val openSourceFragment = OpenSourceFragment()
    private val searchFragment = SearchFragment()
    private val settingsFragment = SettingsFragment()
    
    private var activeFragment: Fragment = homeFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupFragments()
        setupBottomNavigation()
    }
    
    private fun setupFragments() {
        // Add all fragments but hide them initially
        supportFragmentManager.beginTransaction().apply {
            add(R.id.fragmentContainer, homeFragment, "home")
            add(R.id.fragmentContainer, openSourceFragment, "opensource").hide(openSourceFragment)
            add(R.id.fragmentContainer, searchFragment, "search").hide(searchFragment)
            add(R.id.fragmentContainer, settingsFragment, "settings").hide(settingsFragment)
        }.commit()
    }
    
    private fun setupBottomNavigation() {
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    switchFragment(homeFragment)
                    binding.tvTitle.text = getString(R.string.app_name)
                    true
                }
                R.id.nav_opensource -> {
                    switchFragment(openSourceFragment)
                    binding.tvTitle.text = getString(R.string.nav_opensource)
                    true
                }
                R.id.nav_search -> {
                    switchFragment(searchFragment)
                    binding.tvTitle.text = getString(R.string.nav_search)
                    true
                }
                R.id.nav_settings -> {
                    switchFragment(settingsFragment)
                    binding.tvTitle.text = getString(R.string.nav_settings)
                    true
                }
                else -> false
            }
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
