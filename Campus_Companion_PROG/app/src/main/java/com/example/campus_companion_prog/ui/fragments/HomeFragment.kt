package com.example.campus_companion_prog.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.campus_companion_prog.R
import com.example.campus_companion_prog.data.AuthRepository
import com.example.campus_companion_prog.data.CampusRepository
import com.example.campus_companion_prog.databinding.FragmentHomeBinding
import com.example.campus_companion_prog.domain.WeatherRules
import com.example.campus_companion_prog.ui.activities.EventDetailActivity
import com.example.campus_companion_prog.ui.activities.LocationDetailActivity
import com.example.campus_companion_prog.ui.activities.MainActivity
import com.example.campus_companion_prog.ui.adapters.EventAdapter
import com.example.campus_companion_prog.ui.adapters.LocationAdapter
import kotlinx.coroutines.launch
import java.util.Calendar

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val authRepository = AuthRepository()
    private val campusRepository = CampusRepository()

    private lateinit var spacesAdapter: LocationAdapter
    private lateinit var opportunitiesAdapter: EventAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvGreeting.text = getDynamicGreeting()
        binding.tvUserName.text =
            authRepository.currentUser?.displayName?.takeIf { it.isNotBlank() } ?: "Student"
        loadProfileName()

        spacesAdapter = LocationAdapter { location ->
            startActivity(
                Intent(requireContext(), LocationDetailActivity::class.java).apply {
                    putExtra(LocationDetailActivity.EXTRA_LOCATION_ID, location.id)
                }
            )
        }
        opportunitiesAdapter = EventAdapter { event ->
            startActivity(
                Intent(requireContext(), EventDetailActivity::class.java).apply {
                    putExtra(EventDetailActivity.EXTRA_EVENT_ID, event.id)
                }
            )
        }

        binding.rvNearbySpaces.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvNearbySpaces.adapter = spacesAdapter
        binding.rvOpportunities.layoutManager = LinearLayoutManager(requireContext())
        binding.rvOpportunities.isNestedScrollingEnabled = false
        binding.rvOpportunities.adapter = opportunitiesAdapter

        binding.btnSeeAllSpaces.setOnClickListener {
            (activity as? MainActivity)?.navigateToTab(R.id.nav_navigate)
        }
        binding.btnSeeAllOpportunities.setOnClickListener {
            (activity as? MainActivity)?.navigateToTab(R.id.nav_events)
        }

        binding.btnCheckWeather.setOnClickListener { showWeatherPicker() }

        loadDashboard(mockCondition = null)
        refreshSavedCount()
    }

    private fun showWeatherPicker() {
        val labels = arrayOf("Live / default", "Rain", "Hot", "Cold", "Wind", "Clear")
        val codes = arrayOf(null, "RAIN", "HOT", "COLD", "WIND", "CLEAR")
        AlertDialog.Builder(requireContext())
            .setTitle("Campus weather")
            .setItems(labels) { _, which ->
                loadDashboard(mockCondition = codes[which])
            }
            .show()
    }

    private fun loadDashboard(mockCondition: String?) {
        viewLifecycleOwner.lifecycleScope.launch {
            campusRepository.getWeatherRecommendation(mockCondition)
                .onSuccess { weather ->
                    val locationsWithStatus = weather.allLocationsWithStatus.orEmpty()
                    val recommendedLocations = weather.recommendedLocations.orEmpty()
                    Log.i(TAG, "Weather ${weather.condition} recommended=${recommendedLocations.size}")
                    val temp = weather.temperatureCelsius?.let { "${it.toInt()}°C" } ?: ""
                    binding.tvWeatherCondition.text =
                        "${WeatherRules.conditionDisplayName(weather.condition.orEmpty())} $temp".trim()
                    binding.tvWeatherMessage.text = weather.message.orEmpty()

                    val statusById = locationsWithStatus.associate {
                        it.location.id to it.status
                    }
                    val recommended = recommendedLocations.ifEmpty {
                        locationsWithStatus
                            .filter { it.status == WeatherRules.STATUS_RECOMMENDED }
                            .map { it.location }
                    }

                    spacesAdapter.submitList(
                        recommended.take(8).map { loc ->
                            LocationAdapter.Row(
                                location = loc,
                                weatherStatus = statusById[loc.id]
                                    ?: WeatherRules.locationStatus(loc, weather.condition.orEmpty())
                            )
                        }
                    )
                    binding.tvOpenSpacesCount.text = recommended.size.toString()

                    // Opportunities with weather indicators
                    campusRepository.getEvents().onSuccess { events ->
                        val safeEvents = events.orEmpty()
                        binding.tvNewEventsCount.text = safeEvents.size.toString()
                        val opportunities = safeEvents.filter {
                            it.type in setOf("BURSARY", "INTERNSHIP", "WORKSHOP", "CAMPUS_EVENT", "SOCIETY_ACTIVITY")
                        }.take(5)
                        opportunitiesAdapter.submitList(
                            opportunities.map { event ->
                                EventAdapter.Row(
                                    event = event,
                                    weatherIndicator = WeatherRules.eventWeatherIndicator(event, weather.condition.orEmpty())
                                )
                            }
                        )
                    }
                }
                .onFailure { error ->
                    Toast.makeText(
                        requireContext(),
                        error.message ?: "Could not load weather",
                        Toast.LENGTH_LONG
                    ).show()
                }
        }
    }

    private fun refreshSavedCount() {
        viewLifecycleOwner.lifecycleScope.launch {
            campusRepository.getFavourites()
                .onSuccess { favs ->
                    binding.tvSavedItemsCount.text = favs.orEmpty().size.toString()
                }
                .onFailure {
                    // Not signed in or API down — leave placeholder
                }
        }
    }

    private fun loadProfileName() {
        viewLifecycleOwner.lifecycleScope.launch {
            authRepository.getProfile()
                .onSuccess { profile ->
                    binding.tvUserName.text = profile.name
                }
        }
    }

    private fun getDynamicGreeting(): String {
        return when (Calendar.getInstance()[Calendar.HOUR_OF_DAY]) {
            in 5..11 -> "Good morning 👋"
            12 -> "Good noon ☀️"
            in 13..17 -> "Good afternoon ☀️"
            in 18..22 -> "Good evening 🌙"
            else -> "Good night 🌙"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG = "HomeFragment"
    }
}
