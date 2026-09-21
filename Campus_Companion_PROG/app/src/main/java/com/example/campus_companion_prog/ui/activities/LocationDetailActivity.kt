package com.example.campus_companion_prog.ui.activities

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.campus_companion_prog.R
import com.example.campus_companion_prog.api.CampusLocation
import com.example.campus_companion_prog.data.CampusRepository
import com.example.campus_companion_prog.data.FavouriteOfflineRepository
import com.example.campus_companion_prog.databinding.ActivityLocationDetailBinding
import com.google.android.material.chip.Chip
import kotlinx.coroutines.launch

class LocationDetailActivity : LocalizedActivity() {

    private lateinit var binding: ActivityLocationDetailBinding
    private val repository = CampusRepository()
    private var currentLocation: CampusLocation? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLocationDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }

        binding.btnGetDirections.setOnClickListener {
            val loc = currentLocation
            if (loc == null) {
                Toast.makeText(this, "Location not loaded yet", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            Toast.makeText(
                this,
                "Map: ${loc.latitude}, ${loc.longitude} (Maps SDK next)",
                Toast.LENGTH_SHORT
            ).show()
        }

        binding.btnBookmark.setOnClickListener {
            val loc = currentLocation ?: return@setOnClickListener
            lifecycleScope.launch {
                FavouriteOfflineRepository(this@LocationDetailActivity)
                    .saveFavourite("LOCATION", loc.id)
                    .onSuccess {
                        Toast.makeText(this@LocationDetailActivity, getString(R.string.saved_to_items), Toast.LENGTH_SHORT).show()
                    }
                    .onFailure { error ->
                        Toast.makeText(
                            this@LocationDetailActivity,
                            error.message ?: "Could not save",
                            Toast.LENGTH_LONG
                        ).show()
                    }
            }
        }

        val locationId = intent.getStringExtra(EXTRA_LOCATION_ID)
        if (locationId.isNullOrBlank()) {
            Toast.makeText(this, "Missing location id", Toast.LENGTH_LONG).show()
            finish()
            return
        }
        loadLocation(locationId)
    }

    private fun loadLocation(id: String) {
        lifecycleScope.launch {
            repository.getLocation(id)
                .onSuccess { location ->
                    Log.i(TAG, "Loaded location ${location.name}")
                    currentLocation = location
                    populateLocationDetails(location)
                }
                .onFailure { error ->
                    Toast.makeText(
                        this@LocationDetailActivity,
                        error.message ?: "Could not load location",
                        Toast.LENGTH_LONG
                    ).show()
                    finish()
                }
        }
    }

    private fun populateLocationDetails(location: CampusLocation) {
        binding.tvCategoryTag.text = location.category.replace('_', ' ')
        binding.tvLocationTitle.text = location.name
        binding.tvLocationSubtext.text =
            "Quiet ${location.quietLevel}/5 • ${if (location.isIndoor) "Indoor" else "Outdoor"}"
        binding.tvHours.text = location.openingHours
        binding.tvCapacity.text = if (location.hasWifi) "Wi‑Fi" else "No Wi‑Fi"
        binding.tvStatus.text = when {
            location.hasAirConditioning -> "Air‑con"
            location.isCovered -> "Covered"
            else -> "Open air"
        }
        binding.tvAboutDescription.text = location.description

        val facilities = buildList {
            if (location.hasWifi) add("Wi‑Fi")
            if (location.hasAirConditioning) add("Air conditioning")
            if (location.isIndoor) add("Indoor")
            if (location.isCovered) add("Covered")
            add("Quiet ${location.quietLevel}/5")
        }

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

    companion object {
        const val EXTRA_LOCATION_ID = "LOCATION_ID"
        private const val TAG = "LocationDetailActivity"
    }
}
