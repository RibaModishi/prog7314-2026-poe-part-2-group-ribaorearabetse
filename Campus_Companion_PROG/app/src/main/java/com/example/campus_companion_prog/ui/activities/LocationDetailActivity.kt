package com.example.campus_companion_prog.ui.activities

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.campus_companion_prog.databinding.ActivityLocationDetailBinding
import com.google.android.material.chip.Chip

class LocationDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLocationDetailBinding
    private var isBookmarked = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLocationDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnGetDirections.setOnClickListener {
            // TODO: Launch maps intent or in-app navigation engine
            Toast.makeText(this, "Opening navigation...", Toast.LENGTH_SHORT).show()
        }

        binding.btnBookmark.setOnClickListener {
            isBookmarked = !isBookmarked
            // Update UI state dynamically
            Toast.makeText(
                this,
                if (isBookmarked) "Saved to items" else "Removed from saved",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // Call this method when location data is retrieved from database/intent
    fun populateLocationDetails(
        category: String,
        title: String,
        rating: Float,
        distance: String,
        hours: String,
        capacity: Int,
        status: String,
        about: String,
        facilities: List<String>
    ) {
        binding.tvCategoryTag.text = category
        binding.tvLocationTitle.text = title
        binding.tvLocationSubtext.text = "⭐ $rating • $distance"
        binding.tvHours.text = hours
        binding.tvCapacity.text = "$capacity seats"
        binding.tvStatus.text = status
        binding.tvAboutDescription.text = about

        // Dynamically add chips to the ChipGroup
        binding.cgFacilities.removeAllViews()
        for (facility in facilities) {
            val chip = Chip(this).apply {
                text = facility
                isClickable = false
                isCheckable = false
            }
            binding.cgFacilities.addView(chip)
        }
    }
}