package com.example.campus_companion_prog.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.campus_companion_prog.R
import com.example.campus_companion_prog.api.CampusLocation
import com.example.campus_companion_prog.data.CampusRepository
import com.example.campus_companion_prog.databinding.FragmentNavigateBinding
import com.example.campus_companion_prog.domain.WeatherRules
import com.example.campus_companion_prog.ui.activities.LocationDetailActivity
import com.example.campus_companion_prog.ui.adapters.LocationAdapter
import kotlinx.coroutines.launch

class NavigateFragment : Fragment() {

    private var _binding: FragmentNavigateBinding? = null
    private val binding get() = _binding!!

    private val repository = CampusRepository()
    private lateinit var adapter: LocationAdapter

    private var allLocations: List<CampusLocation> = emptyList()
    private var statusById: Map<String, String> = emptyMap()
    private var weatherCondition: String = "CLEAR"
    private var categoryFilter: CategoryFilter = CategoryFilter.ALL
    private var searchQuery: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNavigateBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = LocationAdapter { location ->
            openLocationDetails(location.id)
        }
        binding.rvLocations.layoutManager = LinearLayoutManager(requireContext())
        binding.rvLocations.isNestedScrollingEnabled = false
        binding.rvLocations.adapter = adapter

        setupSearchFilter()
        setupCategoryToggle()
        loadLocationsWithWeather()
    }

    private fun loadLocationsWithWeather() {
        viewLifecycleOwner.lifecycleScope.launch {
            val weatherResult = repository.getWeatherRecommendation()
            val locationsResult = repository.getLocations()

            locationsResult.onFailure { error ->
                Toast.makeText(requireContext(), error.message ?: "Could not load locations", Toast.LENGTH_LONG).show()
                return@launch
            }

            allLocations = locationsResult.getOrThrow().orEmpty()

            weatherResult.onSuccess { weather ->
                weatherCondition = weather.condition.orEmpty().ifBlank { "CLEAR" }
                statusById = weather.allLocationsWithStatus.orEmpty().associate {
                    it.location.id to it.status
                }
                if (statusById.isEmpty()) {
                    // Fallback: compute locally with Riba's rules
                    statusById = allLocations.associate { it.id to WeatherRules.locationStatus(it, weather.condition.orEmpty()) }
                }
                binding.tvMapPlaceholder.text =
                    "${getString(R.string.smart_markers)} • ${WeatherRules.localizedCondition(requireContext(), weather.condition.orEmpty())}"
                Log.i(TAG, "Weather=${weather.condition} markers=${statusById.size}")
            }.onFailure {
                statusById = allLocations.associate { it.id to WeatherRules.locationStatus(it, weatherCondition) }
            }

            applyFilters()
        }
    }

    private fun setupSearchFilter() {
        binding.etSearchLocations.addTextChangedListener { text ->
            searchQuery = text?.toString()?.trim().orEmpty()
            applyFilters()
        }
    }

    private fun setupCategoryToggle() {
        binding.btnFilterAll.setOnClickListener {
            categoryFilter = CategoryFilter.ALL
            applyFilters()
        }
        binding.btnFilterStudy.setOnClickListener {
            categoryFilter = CategoryFilter.STUDY
            applyFilters()
        }
        binding.btnFilterChill.setOnClickListener {
            categoryFilter = CategoryFilter.CHILL
            applyFilters()
        }
    }

    private fun applyFilters() {
        val filtered = allLocations.filter { location ->
            val matchesCategory = when (categoryFilter) {
                CategoryFilter.ALL -> true
                CategoryFilter.STUDY -> location.category in STUDY_CATEGORIES
                CategoryFilter.CHILL -> location.category == "CHILL_SPOT"
            }
            val matchesSearch = searchQuery.isBlank() ||
                location.name.contains(searchQuery, ignoreCase = true) ||
                location.description.contains(searchQuery, ignoreCase = true) ||
                location.category.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }
        adapter.submitList(
            filtered.map { loc ->
                LocationAdapter.Row(
                    location = loc,
                    weatherStatus = statusById[loc.id] ?: WeatherRules.locationStatus(loc, weatherCondition)
                )
            }
        )
    }

    private fun openLocationDetails(locationId: String) {
        startActivity(
            Intent(requireContext(), LocationDetailActivity::class.java).apply {
                putExtra(LocationDetailActivity.EXTRA_LOCATION_ID, locationId)
            }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private enum class CategoryFilter { ALL, STUDY, CHILL }

    companion object {
        private const val TAG = "NavigateFragment"
        private val STUDY_CATEGORIES = setOf("STUDY_SPACE", "LIBRARY", "LAB", "LECTURE_VENUE")
    }
}
