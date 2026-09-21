package com.example.campus_companion_prog.ui.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.campus_companion_prog.data.AuthRepository
import com.example.campus_companion_prog.databinding.ActivityCreateAccountBinding
import kotlinx.coroutines.launch

/**
 * Get Started → create a new campus account with personal details.
 */
class CreateAccountActivity : LocalizedActivity() {

    private lateinit var binding: ActivityCreateAccountBinding
    private val authRepository = AuthRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Log.d(TAG, "onCreate")

        binding.btnBack.setOnClickListener {
            startActivity(Intent(this, LaunchActivity::class.java))
            finish()
        }

        binding.tvAlreadyHaveAccount.setOnClickListener {
            startActivity(Intent(this, SignInActivity::class.java))
            finish()
        }

        binding.btnCreateAccount.setOnClickListener {
            createAccount()
        }
    }

    private fun createAccount() {
        val name = binding.etFullName.text?.toString().orEmpty()
        val email = binding.etStudentEmail.text?.toString().orEmpty()
        val password = binding.etPassword.text?.toString().orEmpty()

        setLoading(true)
        lifecycleScope.launch {
            val result = authRepository.register(name, email, password)
            setLoading(false)

            result.onSuccess { profile ->
                Log.i(TAG, "Account created for ${profile.name}")
                Toast.makeText(
                    this@CreateAccountActivity,
                    "Welcome, ${profile.name}",
                    Toast.LENGTH_SHORT
                ).show()
                startActivity(Intent(this@CreateAccountActivity, MainActivity::class.java))
                finish()
            }.onFailure { error ->
                Log.w(TAG, "Create account failed: ${error.message}")
                Toast.makeText(
                    this@CreateAccountActivity,
                    error.message ?: "Could not create account",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun setLoading(loading: Boolean) {
        binding.btnCreateAccount.isEnabled = !loading
        binding.btnCreateAccount.text = if (loading) "Creating…" else "Create Account"
        binding.etFullName.isEnabled = !loading
        binding.etStudentEmail.isEnabled = !loading
        binding.etPassword.isEnabled = !loading
    }

    companion object {
        private const val TAG = "CreateAccountActivity"
    }
}
