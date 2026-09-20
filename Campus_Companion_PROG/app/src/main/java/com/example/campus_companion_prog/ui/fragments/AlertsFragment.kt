package com.example.campus_companion_prog.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.campus_companion_prog.R
import com.example.campus_companion_prog.databinding.FragmentAlertsBinding
import com.example.campus_companion_prog.ui.adapters.NotificationAdapter
import com.example.campus_companion_prog.ui.adapters.NotificationItem

class AlertsFragment : Fragment() {

    private var _binding: FragmentAlertsBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: NotificationAdapter
    private var notificationList = mutableListOf<NotificationItem>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAlertsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        loadSampleNotifications()

        binding.btnMarkAllRead.setOnClickListener {
            notificationList = notificationList.map { it.copy(isUnread = false) }.toMutableList()
            adapter.updateList(notificationList)
            updateUnreadCount()
            Toast.makeText(requireContext(), "All notifications marked as read", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupRecyclerView() {
        adapter = NotificationAdapter(notificationList) { selectedItem ->
            Toast.makeText(requireContext(), "Clicked: ${selectedItem.title}", Toast.LENGTH_SHORT).show()
        }
        binding.rvNotifications.layoutManager = LinearLayoutManager(requireContext())
        binding.rvNotifications.adapter = adapter
    }

    private fun loadSampleNotifications() {
        notificationList = mutableListOf(
            NotificationItem(
                "1",
                "New Event Added",
                "Freshers' Welcome Braai has been added — Fri 5 Sep, Amphitheatre",
                "Just now",
                true,
                R.drawable.ic_school
            ),
            NotificationItem(
                "2",
                "New Opportunity",
                "Deloitte Internship Programme is now open. Deadline: 15 Oct 2025.",
                "2 hrs ago",
                true,
                R.drawable.ic_bookmark_filled
            ),
            NotificationItem(
                "3",
                "Saved Event Reminder",
                "ABSA Bursary closes in 5 days — don't forget to apply!",
                "Yesterday",
                false,
                R.drawable.ic_notifications
            ),
            NotificationItem(
                "4",
                "Campus Announcement",
                "The Main Library will be closed on 8 Sep for scheduled maintenance.",
                "Yesterday",
                false,
                R.drawable.ic_person
            )
        )
        adapter.updateList(notificationList)
        updateUnreadCount()
    }

    private fun updateUnreadCount() {
        val unreadCount = notificationList.count { it.isUnread }
        binding.tvUnreadCount.text = if (unreadCount > 0) "$unreadCount unread" else "All read"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}