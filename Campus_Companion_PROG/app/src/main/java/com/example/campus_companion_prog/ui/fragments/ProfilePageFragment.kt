package com.example.campus_companion_prog.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.campus_companion_prog.databinding.FragmentProfilepageBinding
import com.example.campus_companion_prog.ui.activities.SettingsActivity

class ProfilePageFragment : Fragment() {

    private var _binding: FragmentProfilepageBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfilepageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.btnSettings.setOnClickListener {
            val intent = Intent(requireContext(), SettingsActivity::class.java)
            startActivity(intent)
        }

        binding.menuEditProfile.setOnClickListener {
            Toast.makeText(requireContext(), "Edit Profile clicked", Toast.LENGTH_SHORT).show()
        }

        binding.menuSavedItems.setOnClickListener {
            Toast.makeText(requireContext(), "Opening Saved Items", Toast.LENGTH_SHORT).show()
        }

        binding.menuNotifications.setOnClickListener {
            Toast.makeText(requireContext(), "Opening Notifications", Toast.LENGTH_SHORT).show()
        }

        binding.menuChangePhoto.setOnClickListener {
            Toast.makeText(requireContext(), "Change Photo clicked", Toast.LENGTH_SHORT).show()
        }

        binding.menuLogout.setOnClickListener {
            Toast.makeText(requireContext(), "Logging out...", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}