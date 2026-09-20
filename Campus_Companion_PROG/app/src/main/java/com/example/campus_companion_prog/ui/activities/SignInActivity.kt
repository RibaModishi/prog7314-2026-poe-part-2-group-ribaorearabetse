package com.example.campus_companion_prog.ui.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.campus_companion_prog.databinding.ActivitySignInBinding

class SignInActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignInBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Microsoft SSO Action
        binding.btnMicrosoftSso.setOnClickListener {
            val email = binding.etStudentEmail.text.toString().trim()
            if (email.isNotEmpty()) {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Please enter your student email", Toast.LENGTH_SHORT).show()
            }
        }

        // Biometric Login Action
        binding.btnBiometric.setOnClickListener {
            Toast.makeText(this, "Authenticating with Biometrics...", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}