package com.example.campus_companion_prog.ui.activities

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.campus_companion_prog.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Back button
        binding.btnSettingsBack.setOnClickListener {
            finish()
        }

        // Display Name & Photo click
        binding.btnDisplayNamePhoto.setOnClickListener {
            Toast.makeText(this, "Display Name & Photo clicked", Toast.LENGTH_SHORT).show()
        }

        // Clear Cache click
        binding.btnClearCache.setOnClickListener {
            Toast.makeText(this, "Cache cleared successfully", Toast.LENGTH_SHORT).show()
        }

        // Sign Out button
        binding.btnSignOut.setOnClickListener {
            Toast.makeText(this, "Signing out...", Toast.LENGTH_SHORT).show()
        }
    }
}