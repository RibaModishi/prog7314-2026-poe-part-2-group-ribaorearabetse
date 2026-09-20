package com.example.campus_companion_prog.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.campus_companion_prog.ui.activities.EventDetailActivity
import com.example.campus_companion_prog.databinding.FragmentEventsBinding

class EventsFragment : Fragment() {

    private var _binding: FragmentEventsBinding? = null
    private val binding get() = _binding!!

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
        setupFilters()
    }

    private fun setupFilters() {
        binding.btnFilterAll.setOnClickListener { /* TODO: Filter All */ }
        binding.btnFilterSocial.setOnClickListener { /* TODO: Filter Social */ }
    }

    // Call this inside your item click listener or "View Details" button click
    private fun openEventDetails(eventId: String) {
        val intent = Intent(requireContext(), EventDetailActivity::class.java).apply {
            putExtra("EVENT_ID", eventId)
        }
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}