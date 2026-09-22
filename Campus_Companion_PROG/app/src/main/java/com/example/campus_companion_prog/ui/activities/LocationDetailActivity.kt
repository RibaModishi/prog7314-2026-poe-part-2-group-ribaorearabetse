package com.example.campus_companion_prog.ui.activities

import android.content.Intent
import android.net.Uri
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
                Toast.makeText(this, getString(R.string.could_not_load), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val uri = Uri.parse("geo:${loc.latitude},${loc.longitude}?q=${loc.latitude},${loc.longitude}(${Uri.encode(loc.name)})")
            val mapIntent = Intent(Intent.ACTION_VIEW, uri)
            if (mapIntent.resolveActivity(packageManager) != null) {
                startActivity(mapIntent)
            } else {
                Toast.makeText(this, getString(R.string.maps_coming, loc.name), Toast.LENGTH_SHORT).show()
            }
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
                            error.message ?: getString(R.string.could_not_save),
                            Toast.LENGTH_LONG
                        ).show()
                    }
            }
        }

        val locationId = intent.getStringExtra(EXTRA_LOCATION_ID)
        if (locationId.isNullOrBlank()) {
            Toast.makeText(this, getString(R.string.could_not_load), Toast.LENGTH_LONG).show()
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
                        error.message ?: getString(R.string.could_not_load),
                        Toast.LENGTH_LONG
                    ).show()
                    finish()
                }
        }
    }

    private fun populateLocationDetails(location: CampusLocation) {
        binding.tvCategoryTag.text = location.category.replace('_', ' ')
        binding.tvLocationTitle.text = location.name
        binding.tvLocationSubtext.text = getString(
            R.string.quiet_level_value,
            location.quietLevel
        ) + " • " + if (location.isIndoor) {
            getString(R.string.marker_indoor)
        } else {
            getString(R.string.marker_outdoor)
        }

        binding.tvHours.text = location.openingHours
        // Middle card = quiet level (API has no seat capacity)
        binding.tvCapacity.text = getString(R.string.quiet_level_value, location.quietLevel)
        // Right card = indoor/outdoor format
        binding.tvStatus.text = if (location.isIndoor) {
            getString(R.string.marker_indoor)
        } else {
            getString(R.string.marker_outdoor)
        }
        binding.tvAboutDescription.text = location.description

        val facilities = buildList {
            if (location.hasWifi) add(getString(R.string.wifi_yes))
            if (location.hasAirConditioning) add(getString(R.string.air_con))
            if (location.isIndoor) add(getString(R.string.marker_indoor))
            if (location.isCovered) add(getString(R.string.covered))
            if (!location.isIndoor && !location.isCovered) add(getString(R.string.open_air))
            add(getString(R.string.quiet_level_value, location.quietLevel))
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
