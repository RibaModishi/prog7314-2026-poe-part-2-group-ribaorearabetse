package com.example.campus_companion_prog.ui.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.campus_companion_prog.R
import com.example.campus_companion_prog.auth.BiometricHelper
import com.example.campus_companion_prog.data.AuthRepository
import com.example.campus_companion_prog.data.LocalPreferences
import com.example.campus_companion_prog.databinding.ActivitySettingsBinding
import com.example.campus_companion_prog.i18n.LocaleHelper
import kotlinx.coroutines.launch

/**
 * Settings — profile from API, preferred language (API + local LocaleHelper),
 * push/biometric toggles stored on-device.
 */
class SettingsActivity : LocalizedActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private val authRepository = AuthRepository()
    private lateinit var localPreferences: LocalPreferences

    /** Avoid firing switch listeners while we hydrate UI from disk/API. */
    private var suppressSwitchCallbacks = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivitySettingsBinding.inflate(layoutInflater)
        localPreferences = LocalPreferences(this)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.btnSettingsBack.setOnClickListener { finish() }

        binding.btnDisplayNamePhoto.setOnClickListener {
            Toast.makeText(
                this,
                getString(R.string.profile_photo_later),
                Toast.LENGTH_SHORT
            ).show()
        }

        binding.btnLanguage.setOnClickListener { showLanguagePicker() }

        suppressSwitchCallbacks = true
        binding.switchPushNotifications.isChecked = localPreferences.pushNotificationsEnabled
        binding.switchBiometric.isChecked = localPreferences.biometricEnabled
        suppressSwitchCallbacks = false

        binding.switchPushNotifications.setOnCheckedChangeListener { _, isChecked ->
            if (suppressSwitchCallbacks) return@setOnCheckedChangeListener
            localPreferences.pushNotificationsEnabled = isChecked
            Toast.makeText(
                this,
                if (isChecked) getString(R.string.push_enabled)
                else getString(R.string.push_disabled),
                Toast.LENGTH_SHORT
            ).show()
        }

        binding.switchBiometric.setOnCheckedChangeListener { _, isChecked ->
            if (suppressSwitchCallbacks) return@setOnCheckedChangeListener
            if (isChecked) {
                if (!BiometricHelper.canAuthenticate(this)) {
                    suppressSwitchCallbacks = true
                    binding.switchBiometric.isChecked = false
                    suppressSwitchCallbacks = false
                    Toast.makeText(this, getString(R.string.biometric_unavailable), Toast.LENGTH_LONG).show()
                    return@setOnCheckedChangeListener
                }
                BiometricHelper.prompt(
                    activity = this,
                    title = getString(R.string.biometric_login),
                    subtitle = getString(R.string.confirm_biometric),
                    onSuccess = {
                        localPreferences.biometricEnabled = true
                        Toast.makeText(this, getString(R.string.biometric_enabled), Toast.LENGTH_SHORT).show()
                    },
                    onError = { msg ->
                        suppressSwitchCallbacks = true
                        binding.switchBiometric.isChecked = false
                        suppressSwitchCallbacks = false
                        localPreferences.biometricEnabled = false
                        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
                    }
                )
            } else {
                localPreferences.biometricEnabled = false
                Toast.makeText(this, getString(R.string.biometric_disabled), Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnClearCache.setOnClickListener {
            localPreferences.clearLocalCache()
            suppressSwitchCallbacks = true
            binding.switchPushNotifications.isChecked = true
            binding.switchBiometric.isChecked = false
            suppressSwitchCallbacks = false
            Toast.makeText(this, getString(R.string.cache_cleared), Toast.LENGTH_SHORT).show()
        }

        binding.btnSignOut.setOnClickListener { signOut() }

        loadProfile()
    }

    private fun loadProfile() {
        lifecycleScope.launch {
            authRepository.getProfile()
                .onSuccess { profile ->
                    Log.i(TAG, "Loaded settings profile name=${profile.name} lang=${profile.preferredLanguage}")
                    binding.tvAccountName.text = profile.name
                    binding.tvAccountEmail.text = profile.email
                    binding.tvAccountInitials.text = initialsFor(profile.name)
                    localPreferences.preferredLanguage = profile.preferredLanguage
                    binding.tvSelectedLanguage.text = languageLabel(profile.preferredLanguage)
                }
                .onFailure { error ->
                    Log.w(TAG, "Failed to load profile: ${error.message}")
                    binding.tvSelectedLanguage.text = languageLabel(localPreferences.preferredLanguage)
                    Toast.makeText(
                        this@SettingsActivity,
                        error.message ?: "Could not load profile",
                        Toast.LENGTH_LONG
                    ).show()
                }
        }
    }

    private fun showLanguagePicker() {
        val options = arrayOf(
            getString(R.string.lang_english),
            getString(R.string.lang_zulu),
            getString(R.string.lang_afrikaans)
        )
        val codes = arrayOf("en", "zu", "af")

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.preferred_language))
            .setItems(options) { _, which ->
                saveLanguage(codes[which])
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun saveLanguage(code: String) {
        lifecycleScope.launch {
            localPreferences.preferredLanguage = code
            authRepository.updatePreferredLanguage(code)
                .onSuccess { profile ->
                    binding.tvSelectedLanguage.text = languageLabel(profile.preferredLanguage)
                    Toast.makeText(
                        this@SettingsActivity,
                        getString(R.string.language_updated),
                        Toast.LENGTH_SHORT
                    ).show()
                    LocaleHelper.applyAppLocale(code)
                }
                .onFailure {
                    binding.tvSelectedLanguage.text = languageLabel(code)
                    Toast.makeText(
                        this@SettingsActivity,
                        getString(R.string.language_device_only),
                        Toast.LENGTH_LONG
                    ).show()
                    LocaleHelper.applyAppLocale(code)
                }
        }
    }

    private fun signOut() {
        authRepository.signOut()
        Toast.makeText(this, getString(R.string.sign_out), Toast.LENGTH_SHORT).show()
        startActivity(
            Intent(this, LaunchActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        )
        finish()
    }

    private fun languageLabel(code: String): String = when (code.lowercase()) {
        "zu" -> "isiZulu ∨"
        "af" -> "Afrikaans ∨"
        else -> "English ∨"
    }

    private fun initialsFor(name: String): String {
        val parts = name.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
        return when {
            parts.size >= 2 -> "${parts[0].first().uppercaseChar()}${parts[1].first().uppercaseChar()}"
            parts.size == 1 && parts[0].isNotEmpty() -> parts[0].take(2).uppercase()
            else -> "?"
        }
    }

    companion object {
        private const val TAG = "SettingsActivity"
    }
}
