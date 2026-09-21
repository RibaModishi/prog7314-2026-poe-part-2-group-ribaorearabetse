package com.example.campus_companion_prog.ui.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.campus_companion_prog.R
import com.example.campus_companion_prog.auth.BiometricHelper
import com.example.campus_companion_prog.data.AuthRepository
import com.example.campus_companion_prog.data.LocalPreferences
import com.example.campus_companion_prog.databinding.ActivitySignInBinding
import kotlinx.coroutines.launch

/**
 * Existing users — email + password, or biometric unlock when a Firebase
 * session already exists and Settings biometric is on.
 */
class SignInActivity : LocalizedActivity() {

    private lateinit var binding: ActivitySignInBinding
    private val authRepository = AuthRepository()
    private lateinit var localPreferences: LocalPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        localPreferences = LocalPreferences(this)
        setContentView(binding.root)
        Log.d(TAG, "onCreate — signedIn=${authRepository.isSignedIn()}")

        binding.btnBack.setOnClickListener {
            startActivity(Intent(this, LaunchActivity::class.java))
            finish()
        }

        binding.btnMicrosoftSso.setOnClickListener {
            attemptSignIn()
        }

        val biometricReady = localPreferences.biometricEnabled &&
            authRepository.isSignedIn() &&
            BiometricHelper.canAuthenticate(this)
        binding.btnBiometric.visibility = if (biometricReady) View.VISIBLE else View.GONE

        binding.btnBiometric.setOnClickListener {
            BiometricHelper.prompt(
                activity = this,
                title = getString(R.string.biometric_login),
                subtitle = "Unlock with biometrics",
                onSuccess = {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                },
                onError = { msg ->
                    Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
                }
            )
        }
    }

    private fun attemptSignIn() {
        val email = binding.etStudentEmail.text?.toString().orEmpty()
        val password = binding.etPassword.text?.toString().orEmpty()

        setLoading(true)
        lifecycleScope.launch {
            val result = authRepository.signIn(email, password)
            setLoading(false)

            result.onSuccess { profile ->
                Log.i(TAG, "Sign-in success profile=${profile.uid}")
                Toast.makeText(
                    this@SignInActivity,
                    getString(R.string.welcome_back, profile.name),
                    Toast.LENGTH_SHORT
                ).show()
                startActivity(Intent(this@SignInActivity, MainActivity::class.java))
                finish()
            }.onFailure { error ->
                Log.w(TAG, "Sign-in failed: ${error.message}")
                Toast.makeText(
                    this@SignInActivity,
                    error.message ?: "Sign-in failed",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun setLoading(loading: Boolean) {
        binding.btnMicrosoftSso.isEnabled = !loading
        binding.btnMicrosoftSso.text = if (loading) getString(R.string.signing_in) else getString(R.string.sign_in)
        binding.etStudentEmail.isEnabled = !loading
        binding.etPassword.isEnabled = !loading
        val biometricReady = localPreferences.biometricEnabled &&
            authRepository.isSignedIn() &&
            BiometricHelper.canAuthenticate(this)
        binding.btnBiometric.visibility = when {
            loading -> View.INVISIBLE
            biometricReady -> View.VISIBLE
            else -> View.GONE
        }
    }

    companion object {
        private const val TAG = "SignInActivity"
    }
}
