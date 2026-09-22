package com.example.campus_companion_prog.ui.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.campus_companion_prog.R
import com.example.campus_companion_prog.api.EventOrOpportunity
import com.example.campus_companion_prog.data.CampusRepository
import com.example.campus_companion_prog.data.FavouriteOfflineRepository
import com.example.campus_companion_prog.databinding.ActivityEventDetailBinding
import kotlinx.coroutines.launch

class EventDetailActivity : LocalizedActivity() {

    private lateinit var binding: ActivityEventDetailBinding
    private val repository = CampusRepository()
    private var currentEvent: EventOrOpportunity? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEventDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }

        binding.btnBookmark.setOnClickListener {
            val event = currentEvent ?: return@setOnClickListener
            lifecycleScope.launch {
                FavouriteOfflineRepository(this@EventDetailActivity)
                    .saveFavourite("EVENT", event.id)
                    .onSuccess {
                        Toast.makeText(this@EventDetailActivity, getString(R.string.saved_to_items), Toast.LENGTH_SHORT).show()
                    }
                    .onFailure { error ->
                        Toast.makeText(
                            this@EventDetailActivity,
                            error.message ?: getString(R.string.could_not_save),
                            Toast.LENGTH_LONG
                        ).show()
                    }
            }
        }

        val eventId = intent.getStringExtra(EXTRA_EVENT_ID)
        if (eventId.isNullOrBlank()) {
            Toast.makeText(this, getString(R.string.could_not_load), Toast.LENGTH_LONG).show()
            finish()
            return
        }
        loadEvent(eventId)
    }

    private fun loadEvent(id: String) {
        lifecycleScope.launch {
            repository.getEvent(id)
                .onSuccess { event ->
                    Log.i(TAG, "Loaded event ${event.title}")
                    currentEvent = event
                    populateEventDetails(event)
                    configureActionButton(event)
                }
                .onFailure { error ->
                    Toast.makeText(
                        this@EventDetailActivity,
                        error.message ?: getString(R.string.could_not_load),
                        Toast.LENGTH_LONG
                    ).show()
                    finish()
                }
        }
    }

    private fun populateEventDetails(event: EventOrOpportunity) {
        binding.tvTag.text = event.type.replace('_', ' ')
        binding.tvTitle.text = event.title
        binding.tvOrganisation.text = event.organiser
        binding.tvDeadline.text = event.endDateTime?.take(10)
            ?: event.startDateTime.take(10)
        // "Format" card — indoor vs outdoor (not financial value)
        binding.tvValue.text = if (event.isOutdoor) {
            getString(R.string.marker_outdoor)
        } else {
            getString(R.string.indoor_online)
        }
        binding.tvEligibility.text = event.tags.orEmpty().joinToString(", ")
            .ifBlank { getString(R.string.open_to_all) }
        binding.tvAbout.text = event.description
    }

    private fun configureActionButton(event: EventOrOpportunity) {
        when (event.type) {
            "BURSARY", "INTERNSHIP" -> {
                binding.btnApplyNow.text = getString(R.string.apply_now)
                binding.btnApplyNow.setOnClickListener {
                    val url = event.externalUrl
                    if (url.isNullOrBlank()) {
                        Toast.makeText(this, getString(R.string.could_not_load), Toast.LENGTH_SHORT).show()
                    } else {
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    }
                }
            }
            else -> {
                binding.btnApplyNow.text = getString(R.string.view_details)
                binding.btnApplyNow.setOnClickListener {
                    val url = event.externalUrl
                    if (!url.isNullOrBlank()) {
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    } else {
                        Toast.makeText(this, event.description, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    companion object {
        const val EXTRA_EVENT_ID = "EVENT_ID"
        private const val TAG = "EventDetailActivity"
    }
}
