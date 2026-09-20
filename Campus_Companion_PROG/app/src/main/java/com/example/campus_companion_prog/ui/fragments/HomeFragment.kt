package com.example.campus_companion_prog.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.campus_companion_prog.databinding.FragmentHomeBinding
import java.util.Calendar

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

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

        // Set dynamic time-based greeting
        binding.tvGreeting.text = getDynamicGreeting()

        // Set up click actions for "See all" buttons
        binding.btnSeeAllSpaces.setOnClickListener {
            // TODO: Navigate to full Spaces list
        }

        binding.btnSeeAllOpportunities.setOnClickListener {
            // TODO: Navigate to full Opportunities list
        }

        binding.btnCheckWeather.setOnClickListener {
            android.widget.Toast.makeText(requireContext(), "Checking weather...", android.widget.Toast.LENGTH_SHORT).show()
        }

        // Set up dynamic metrics (Pass values from your database/ViewModel here)
        updateSummaryMetrics(openSpaces = 12, newEvents = 5, savedItems = 3)
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

    fun updateSummaryMetrics(openSpaces: Int, newEvents: Int, savedItems: Int) {
        binding.tvOpenSpacesCount.text = openSpaces.toString()
        binding.tvNewEventsCount.text = newEvents.toString()
        binding.tvSavedItemsCount.text = savedItems.toString()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}