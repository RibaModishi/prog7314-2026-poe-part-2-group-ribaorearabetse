package com.example.campus_companion_prog.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import com.example.campus_companion_prog.ui.activities.LocationDetailActivity
import com.example.campus_companion_prog.databinding.FragmentNavigateBinding

class NavigateFragment : Fragment() {

    private var _binding: FragmentNavigateBinding? = null
    private val binding get() = _binding!!

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

        setupSearchFilter()
        setupCategoryToggle()
    }

    private fun setupSearchFilter() {
        binding.etSearchLocations.addTextChangedListener { text ->
            val query = text?.toString()?.trim().orEmpty()
            // TODO: Pass query to your RecyclerView Adapter filter algorithm
        }
    }

    private fun setupCategoryToggle() {
        binding.btnFilterAll.setOnClickListener {
            // TODO: Filter list by All
        }

        binding.btnFilterStudy.setOnClickListener {
            // TODO: Filter list by Study category
        }

        binding.btnFilterChill.setOnClickListener {
            // TODO: Filter list by Chill category
        }
    }

    // Call this function inside your RecyclerView Adapter item click listener
    private fun openLocationDetails(locationId: String) {
        val intent = Intent(requireContext(), LocationDetailActivity::class.java).apply {
            putExtra("LOCATION_ID", locationId)
        }
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}