package com.example.campus_companion_prog.ui.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.campus_companion_prog.R
import com.example.campus_companion_prog.auth.BiometricHelper
import com.example.campus_companion_prog.data.AuthRepository
import com.example.campus_companion_prog.data.LocalPreferences
import com.example.campus_companion_prog.databinding.ActivityLaunchBinding

class LaunchActivity : LocalizedActivity() {

    private lateinit var binding: ActivityLaunchBinding
    private val authRepository = AuthRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLaunchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (authRepository.isSignedIn()) {
            val prefs = LocalPreferences(this)
            if (prefs.biometricEnabled && BiometricHelper.canAuthenticate(this)) {
                Log.i(TAG, "Existing session — biometric gate")
                binding.root.alpha = 0.3f
                BiometricHelper.prompt(
                    activity = this,
                    title = getString(R.string.biometric_login),
                    subtitle = getString(R.string.unlock_campus),
                    onSuccess = { goMain() },
                    onError = { msg ->
                        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
                        binding.root.alpha = 1f
                        wireLaunchButtons()
                    }
                )
                return
            }
            Log.i(TAG, "Existing session — routing to MainActivity")
            goMain()
            return
        }

        wireLaunchButtons()
    }

    private fun wireLaunchButtons() {
        Log.d(TAG, "showing launch options")
        binding.btnGetStarted.setOnClickListener {
            Log.i(TAG, "Get Started → CreateAccountActivity")
            startActivity(Intent(this, CreateAccountActivity::class.java))
            finish()
        }
        binding.btnAlreadyHaveAccount.setOnClickListener {
            Log.i(TAG, "Already have account → SignInActivity")
            startActivity(Intent(this, SignInActivity::class.java))
            finish()
        }
    }

    private fun goMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    companion object {
        private const val TAG = "LaunchActivity"
    }
}
