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
import com.example.campus_companion_prog.api.EventOrOpportunity
import com.example.campus_companion_prog.data.CampusRepository
import com.example.campus_companion_prog.databinding.FragmentEventsBinding
import com.example.campus_companion_prog.domain.WeatherRules
import com.example.campus_companion_prog.ui.activities.EventDetailActivity
import com.example.campus_companion_prog.ui.adapters.EventAdapter
import kotlinx.coroutines.launch

class EventsFragment : Fragment() {

    private var _binding: FragmentEventsBinding? = null
    private val binding get() = _binding!!

    private val repository = CampusRepository()
    private lateinit var adapter: EventAdapter

    private var allEvents: List<EventOrOpportunity> = emptyList()
    private var warningById: Map<String, String?> = emptyMap()
    private var weatherCondition: String = "CLEAR"
    private var typeFilter: TypeFilter = TypeFilter.ALL
    private var searchQuery: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEventsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = EventAdapter { event ->
            openEventDetails(event.id)
        }
        binding.rvEvents.layoutManager = LinearLayoutManager(requireContext())
        binding.rvEvents.isNestedScrollingEnabled = false
        binding.rvEvents.adapter = adapter

        setupSearch()
        setupFilters()
        loadEventsWithWeather()
    }

    private fun loadEventsWithWeather() {
        viewLifecycleOwner.lifecycleScope.launch {
            val eventsResult = repository.getEvents()
            val weatherResult = repository.getWeatherRecommendation()
            val warningsResult = repository.getEventWeatherWarnings()

            eventsResult.onFailure { error ->
                Toast.makeText(requireContext(), error.message ?: "Could not load events", Toast.LENGTH_LONG).show()
                return@launch
            }
            allEvents = eventsResult.getOrThrow().orEmpty()

            weatherResult.onSuccess { weather ->
                weatherCondition = weather.condition.orEmpty().ifBlank { weatherCondition }
            }

            warningById = warningsResult.getOrNull()
                ?.associate { it.eventId to it.warning }
                ?: emptyMap()

            // Fill gaps with local Riba rules (resource ids → localized text)
            if (warningById.isEmpty()) {
                warningById = allEvents.associate { event ->
                    event.id to WeatherRules.localizedEventBanner(requireContext(), event, weatherCondition)
                }
            } else {
                warningById = allEvents.associate { event ->
                    val apiWarning = warningById[event.id]
                    event.id to (apiWarning
                        ?: WeatherRules.localizedEventBanner(requireContext(), event, weatherCondition))
                }
            }

            applyFilters()
            Log.i(TAG, "Events=${allEvents.size} weather=$weatherCondition")
        }
    }

    private fun setupSearch() {
        binding.etSearchEvents.addTextChangedListener { text ->
            searchQuery = text?.toString()?.trim().orEmpty()
            applyFilters()
        }
    }

    private fun setupFilters() {
        binding.btnFilterAll.setOnClickListener {
            typeFilter = TypeFilter.ALL
            applyFilters()
        }
        binding.btnFilterSocial.setOnClickListener {
            typeFilter = TypeFilter.SOCIAL
            applyFilters()
        }
        binding.btnFilterSchool.setOnClickListener {
            typeFilter = TypeFilter.SCHOOL
            applyFilters()
        }
        binding.btnFilterOpportunity.setOnClickListener {
            typeFilter = TypeFilter.OPPORTUNITY
            applyFilters()
        }
    }

    private fun applyFilters() {
        val filtered = allEvents.filter { event ->
            val matchesType = when (typeFilter) {
                TypeFilter.ALL -> true
                TypeFilter.SOCIAL -> event.type in setOf("SOCIETY_ACTIVITY", "CAMPUS_EVENT")
                TypeFilter.SCHOOL -> event.type in setOf("CAMPUS_EVENT", "WORKSHOP")
                TypeFilter.OPPORTUNITY -> event.type in setOf("BURSARY", "INTERNSHIP")
            }
            val matchesSearch = searchQuery.isBlank() ||
                event.title.contains(searchQuery, ignoreCase = true) ||
                event.description.contains(searchQuery, ignoreCase = true) ||
                event.organiser.contains(searchQuery, ignoreCase = true) ||
                event.type.contains(searchQuery, ignoreCase = true) ||
                event.tags.orEmpty().any { it.contains(searchQuery, ignoreCase = true) }
            matchesType && matchesSearch
        }
        adapter.submitList(
            filtered.map { event ->
                EventAdapter.Row(
                    event = event,
                    weatherIndicator = warningById[event.id]
                        ?: WeatherRules.localizedEventBanner(requireContext(), event, weatherCondition)
                )
            }
        )
    }

    private fun openEventDetails(eventId: String) {
        startActivity(
            Intent(requireContext(), EventDetailActivity::class.java).apply {
                putExtra(EventDetailActivity.EXTRA_EVENT_ID, eventId)
            }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private enum class TypeFilter { ALL, SOCIAL, SCHOOL, OPPORTUNITY }

    companion object {
        private const val TAG = "EventsFragment"
    }
}
