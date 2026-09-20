package com.example.campus_companion_prog.ui.activities

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.campus_companion_prog.databinding.ActivityEventDetailBinding

class EventDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEventDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEventDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }

        // Example trigger: pass "Opportunity", "Social", or "School" via Intent
        val categoryType = intent.getStringExtra("CATEGORY_TYPE") ?: "Opportunity"
        configureActionButton(categoryType)
    }

    fun configureActionButton(categoryType: String) {
        when (categoryType.lowercase()) {
            "opportunity", "bursary", "internship" -> {
                binding.btnApplyNow.text = "Apply Now"
                binding.btnApplyNow.setOnClickListener {
                    Toast.makeText(this, "Redirecting to application link...", Toast.LENGTH_SHORT).show()
                }
            }
            "social", "school", "event", "location" -> {
                binding.btnApplyNow.text = "Get Directions"
                binding.btnApplyNow.setOnClickListener {
                    Toast.makeText(this, "Opening map directions...", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}