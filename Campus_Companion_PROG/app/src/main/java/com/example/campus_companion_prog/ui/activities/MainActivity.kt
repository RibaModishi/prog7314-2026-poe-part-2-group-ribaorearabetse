package com.example.campus_companion_prog.ui.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.campus_companion_prog.R
import com.example.campus_companion_prog.data.AuthRepository
import com.example.campus_companion_prog.databinding.ActivityMainBinding

class MainActivity : LocalizedActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Apply dynamic padding onto the top custom header bar so it fits clear of the notch/status bar cutout
        ViewCompat.setOnApplyWindowInsetsListener(binding.topHeaderBar) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(v.paddingLeft, systemBars.top, v.paddingRight, v.paddingBottom)
            insets
        }

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        // Connects the bottom nav with your Navigation Graph
        binding.bottomNavigation.setupWithNavController(navController)
        
        // Connects the side navigation view with Navigation Graph for shared destinations
        binding.sideNavigationView.setupWithNavController(navController)

        // Set up click actions for top header actions to toggle the side menu drawer
        binding.btnSideMenu.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }

        binding.btnLogout.setOnClickListener {
            logout()
        }

        // Handle specific actions for custom items in the side menu drawer
        binding.sideNavigationView.setNavigationItemSelectedListener { menuItem ->
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            when (menuItem.itemId) {
                R.id.nav_home -> navController.navigate(R.id.nav_home)
                R.id.nav_navigate -> navController.navigate(R.id.nav_navigate)
                R.id.nav_events -> navController.navigate(R.id.nav_events)
                R.id.nav_saved -> navController.navigate(R.id.nav_saved)
                R.id.nav_alerts -> navController.navigate(R.id.nav_alerts)
                R.id.nav_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                }
                R.id.nav_profile -> navController.navigate(R.id.nav_profile)
                R.id.nav_help -> {
                    Toast.makeText(this, "Help & Feedback feature option selected", Toast.LENGTH_SHORT).show()
                }
                R.id.nav_logout -> logout()
            }
            true
        }
    }

    fun navigateToTab(destinationId: Int) {
        binding.bottomNavigation.selectedItemId = destinationId
    }

    private fun logout() {
        AuthRepository().signOut()
        Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show()
        startActivity(Intent(this, LaunchActivity::class.java))
        finish()
    }
}