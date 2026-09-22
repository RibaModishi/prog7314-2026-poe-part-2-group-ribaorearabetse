package com.example.campus_companion_prog.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.campus_companion_prog.R
import com.example.campus_companion_prog.data.CampusRepository
import com.example.campus_companion_prog.data.SavedEventItem
import com.example.campus_companion_prog.data.SavedLocationItem
import com.example.campus_companion_prog.databinding.FragmentSavedBinding
import com.example.campus_companion_prog.ui.activities.EventDetailActivity
import com.example.campus_companion_prog.ui.activities.LocationDetailActivity
import com.example.campus_companion_prog.ui.adapters.SavedEventAdapter
import com.example.campus_companion_prog.ui.adapters.SavedLocationAdapter
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

/**
 * Saved Items — loads favourites from the API and resolves each to a full
 * location or event for display (per handover: never talk to Firestore from the app).
 */
class SavedFragment : Fragment() {

    private var _binding: FragmentSavedBinding? = null
    private val binding get() = _binding!!

    private val repository = CampusRepository()
    private lateinit var locationAdapter: SavedLocationAdapter
    private lateinit var eventAdapter: SavedEventAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSavedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        locationAdapter = SavedLocationAdapter(
            onOpen = { item ->
                startActivity(
                    Intent(requireContext(), LocationDetailActivity::class.java).apply {
                        putExtra(LocationDetailActivity.EXTRA_LOCATION_ID, item.location.id)
                    }
                )
            },
            onRemove = { item -> removeFavourite(item.favourite.id, isLocation = true) }
        )
        eventAdapter = SavedEventAdapter(
            onOpen = { item ->
                startActivity(
                    Intent(requireContext(), EventDetailActivity::class.java).apply {
                        putExtra(EventDetailActivity.EXTRA_EVENT_ID, item.event.id)
                    }
                )
            },
            onRemove = { item -> removeFavourite(item.favourite.id, isLocation = false) }
        )

        binding.rvSavedLocations.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSavedLocations.adapter = locationAdapter
        binding.rvSavedOpportunities.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSavedOpportunities.adapter = eventAdapter
    }

    override fun onResume() {
        super.onResume()
        // Refresh whenever the tab becomes visible (e.g. after saving from details).
        loadSavedItems()
    }

    private fun loadSavedItems() {
        viewLifecycleOwner.lifecycleScope.launch {
            val favouritesDeferred = async { repository.getFavourites() }
            val locationsDeferred = async { repository.getLocations() }
            val eventsDeferred = async { repository.getEvents() }

            val favouritesResult = favouritesDeferred.await()
            val locationsResult = locationsDeferred.await()
            val eventsResult = eventsDeferred.await()

            favouritesResult.onFailure { error ->
                Log.e(TAG, "Favourites failed: ${error.message}")
                Toast.makeText(
                    requireContext(),
                    error.message ?: "Could not load saved items (are you signed in?)",
                    Toast.LENGTH_LONG
                ).show()
                return@launch
            }

            val favourites = favouritesResult.getOrThrow().orEmpty()
            val locationsById = locationsResult.getOrDefault(emptyList()).associateBy { it.id }
            val eventsById = eventsResult.getOrDefault(emptyList()).associateBy { it.id }

            val savedLocations = favourites
                .filter { it.itemType == "LOCATION" }
                .mapNotNull { fav ->
                    locationsById[fav.itemId]?.let { SavedLocationItem(fav, it) }
                }

            val savedEvents = favourites
                .filter { it.itemType == "EVENT" }
                .mapNotNull { fav ->
                    eventsById[fav.itemId]?.let { SavedEventItem(fav, it) }
                }

            locationAdapter.submitList(savedLocations)
            eventAdapter.submitList(savedEvents)
            Log.i(TAG, "Saved locations=${savedLocations.size} events=${savedEvents.size}")

            if (favourites.isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    "No saved items yet — save from Navigate or Events.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun removeFavourite(favouriteId: String, isLocation: Boolean) {
        viewLifecycleOwner.lifecycleScope.launch {
            repository.deleteFavourite(favouriteId)
                .onSuccess {
                    Toast.makeText(requireContext(), getString(R.string.removed_from_saved), Toast.LENGTH_SHORT).show()
                    loadSavedItems()
                }
                .onFailure { error ->
                    Toast.makeText(
                        requireContext(),
                        error.message ?: "Could not remove",
                        Toast.LENGTH_LONG
                    ).show()
                }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG = "SavedFragment"
    }
}
