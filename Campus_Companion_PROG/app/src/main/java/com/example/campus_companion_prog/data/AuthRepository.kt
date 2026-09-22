package com.example.campus_companion_prog.data

import android.util.Log
import com.example.campus_companion_prog.api.RetrofitClient
import com.example.campus_companion_prog.api.UpdateSettingsRequest
import com.example.campus_companion_prog.api.UpsertProfileRequest
import com.example.campus_companion_prog.api.UserProfile
import com.example.campus_companion_prog.api.toApiError
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import retrofit2.HttpException

/**
 * Auth + profile business rules for Campus Companion.
 *
 * - [register]: Get Started flow — collects personal info, creates Firebase user + API profile
 * - [signIn]: Existing account flow — email/password only
 */
class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {

    companion object {
        private const val TAG = "AuthRepository"
        private const val AUTH_TIMEOUT_MS = 20_000L
        private const val TIMEOUT_MESSAGE =
            "That took too long. Check your connection, then try again."
        private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    }

    val currentUser: FirebaseUser? get() = auth.currentUser

    fun isSignedIn(): Boolean = currentUser != null

    /** Create a new campus account (Get Started). */
    suspend fun register(fullName: String, email: String, password: String): Result<UserProfile> {
        val name = fullName.trim()
        val normalisedEmail = email.trim().lowercase()

        validateName(name)?.let { return Result.failure(IllegalArgumentException(it)) }
        validateCredentials(normalisedEmail, password)?.let {
            return Result.failure(IllegalArgumentException(it))
        }

        return try {
            Log.i(TAG, "Registering new account for $normalisedEmail")
            val user = withTimeout(AUTH_TIMEOUT_MS) {
                val authResult = auth.createUserWithEmailAndPassword(normalisedEmail, password).await()
                val created = authResult.user
                    ?: throw IllegalStateException("Firebase returned no user")
                created.updateProfile(
                    UserProfileChangeRequest.Builder()
                        .setDisplayName(name)
                        .build()
                ).await()
                created
            }

            Log.i(TAG, "Firebase user created uid=${user.uid} — upserting API profile")
            val profile = syncProfileOrLocal(user, name)
            Log.i(TAG, "Profile ready uid=${profile.uid} name=${profile.name}")
            Result.success(profile)
        } catch (ex: TimeoutCancellationException) {
            Log.e(TAG, "Register timed out", ex)
            Result.failure(IllegalStateException(TIMEOUT_MESSAGE))
        } catch (ex: CancellationException) {
            throw ex
        } catch (ex: FirebaseAuthUserCollisionException) {
            Log.e(TAG, "Email already registered", ex)
            Result.failure(
                IllegalArgumentException("An account with this email already exists. Use Sign In instead.")
            )
        } catch (ex: HttpException) {
            Result.failure(apiFailure(ex))
        } catch (ex: FirebaseAuthException) {
            Log.e(TAG, "Register failed code=${ex.errorCode}", ex)
            Result.failure(IllegalStateException(friendlyFirebaseMessage(ex)))
        } catch (ex: Exception) {
            Log.e(TAG, "Register failed", ex)
            Result.failure(IllegalStateException(ex.message ?: "Could not create account."))
        }
    }

    /** Sign in an existing account (I already have an account). */
    suspend fun signIn(email: String, password: String): Result<UserProfile> {
        val normalisedEmail = email.trim().lowercase()
        validateCredentials(normalisedEmail, password)?.let {
            return Result.failure(IllegalArgumentException(it))
        }

        return try {
            Log.i(TAG, "Signing in $normalisedEmail")
            val user = withTimeout(AUTH_TIMEOUT_MS) {
                val authResult = auth.signInWithEmailAndPassword(normalisedEmail, password).await()
                authResult.user ?: throw IllegalStateException("Firebase returned no user")
            }

            val profile = syncProfileOrLocal(user, nameOverride = null)
            Log.i(TAG, "Sign-in OK uid=${profile.uid} name=${profile.name}")
            Result.success(profile)
        } catch (ex: TimeoutCancellationException) {
            Log.e(TAG, "Sign-in timed out", ex)
            Result.failure(IllegalStateException(TIMEOUT_MESSAGE))
        } catch (ex: CancellationException) {
            throw ex
        } catch (ex: FirebaseAuthInvalidCredentialsException) {
            Log.e(TAG, "Invalid credentials code=${ex.errorCode}", ex)
            Result.failure(IllegalArgumentException("Incorrect email or password."))
        } catch (ex: HttpException) {
            Result.failure(apiFailure(ex))
        } catch (ex: FirebaseAuthException) {
            Log.e(TAG, "Sign-in failed code=${ex.errorCode}", ex)
            Result.failure(IllegalStateException(friendlyFirebaseMessage(ex)))
        } catch (ex: Exception) {
            Log.e(TAG, "Sign-in failed", ex)
            Result.failure(IllegalStateException(ex.message ?: "Sign-in failed."))
        }
    }

    /** Load the signed-in user's API profile for UI (dashboard name, etc.). */
    suspend fun getProfile(): Result<UserProfile> {
        if (!isSignedIn()) {
            return Result.failure(IllegalStateException("Not signed in."))
        }
        return try {
            Result.success(RetrofitClient.api.getMyProfile())
        } catch (ex: HttpException) {
            Result.failure(apiFailure(ex))
        } catch (ex: Exception) {
            Result.failure(IllegalStateException(ex.message ?: "Could not load profile."))
        }
    }

    /** Persist preferred language (en / zu / af) via PUT /api/users/me/settings. */
    suspend fun updatePreferredLanguage(languageCode: String): Result<UserProfile> {
        if (!isSignedIn()) {
            return Result.failure(IllegalStateException("Not signed in."))
        }
        val code = languageCode.trim().lowercase()
        if (code !in setOf("en", "zu", "af")) {
            return Result.failure(IllegalArgumentException("Language must be English, isiZulu, or Afrikaans."))
        }
        return try {
            Log.i(TAG, "Updating preferredLanguage=$code")
            val profile = RetrofitClient.api.updateSettings(UpdateSettingsRequest(preferredLanguage = code))
            Log.i(TAG, "Settings updated lang=${profile.preferredLanguage}")
            Result.success(profile)
        } catch (ex: HttpException) {
            Result.failure(apiFailure(ex))
        } catch (ex: Exception) {
            Result.failure(IllegalStateException(ex.message ?: "Could not update settings."))
        }
    }

    /**
     * Saves the profile on the API. If that server is down, still return a
     * local profile so signup can open the app instead of sitting on Creating….
     */
    private suspend fun syncProfileOrLocal(user: FirebaseUser, nameOverride: String?): UserProfile {
        return try {
            upsertProfileFor(user, nameOverride)
        } catch (ex: CancellationException) {
            throw ex
        } catch (ex: Exception) {
            Log.e(TAG, "API profile sync failed; continuing with the Firebase profile", ex)
            localProfile(user, nameOverride)
        }
    }

    private fun localProfile(user: FirebaseUser, nameOverride: String?): UserProfile {
        val now = System.currentTimeMillis()
        return UserProfile(
            uid = user.uid,
            name = displayNameFor(user, nameOverride),
            email = user.email.orEmpty(),
            preferredLanguage = "en",
            createdAt = now,
            updatedAt = now
        )
    }

    private fun displayNameFor(user: FirebaseUser, nameOverride: String?): String {
        return nameOverride
            ?.takeIf { it.isNotBlank() }
            ?: user.displayName?.takeIf { it.isNotBlank() }
            ?: user.email?.substringBefore("@")?.replaceFirstChar { it.uppercase() }
            ?: "Student"
    }

    suspend fun upsertProfileFor(user: FirebaseUser, nameOverride: String? = null): UserProfile {
        val displayName = displayNameFor(user, nameOverride)

        return RetrofitClient.api.upsertProfile(
            UpsertProfileRequest(
                name = displayName,
                email = user.email.orEmpty(),
                preferredLanguage = "en"
            )
        )
    }

    fun signOut() {
        Log.i(TAG, "Signing out uid=${currentUser?.uid}")
        auth.signOut()
    }

    private fun validateName(name: String): String? = when {
        name.isBlank() -> "Please enter your full name."
        name.length < 2 -> "Name looks too short."
        else -> null
    }

    private fun validateCredentials(email: String, password: String): String? = when {
        email.isBlank() -> "Please enter your student email."
        !EMAIL_REGEX.matches(email) -> "Please enter a valid email address."
        password.isBlank() -> "Please enter your password."
        password.length < 6 -> "Password must be at least 6 characters."
        else -> null
    }

    private fun apiFailure(ex: HttpException): IllegalStateException {
        val apiError = ex.toApiError()
        Log.e(TAG, "API failed status=${ex.code()} msg=${apiError?.message}", ex)
        return IllegalStateException(
            apiError?.message
                ?: "Could not reach the Campus Companion API (${ex.code()})."
        )
    }

    private fun friendlyFirebaseMessage(ex: FirebaseAuthException): String = when (ex.errorCode) {
        "ERROR_OPERATION_NOT_ALLOWED" ->
            "Email/password sign-in is disabled in Firebase. Enable it under Authentication → Sign-in method."
        "ERROR_NETWORK_REQUEST_FAILED" ->
            "Network error talking to Firebase. Check emulator internet."
        "ERROR_WEAK_PASSWORD" ->
            "Password is too weak. Use at least 6 characters."
        "ERROR_INVALID_EMAIL" ->
            "That email address is not valid."
        "ERROR_USER_NOT_FOUND" ->
            "No account found for that email. Tap Get Started to create one."
        else ->
            ex.message ?: "Authentication failed (${ex.errorCode})."
    }
}
