package com.example.campus_companion_prog.api

import android.util.Log
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Builds the Retrofit client used to talk to the Campus Companion API.
 *
 * BASE_URL:
 *  - Emulator → host machine: http://10.0.2.2:8090/
 *  - Physical phone (same Wi-Fi): http://<pc-lan-ip>:8090/
 *  - Deployed API: swap for the HTTPS Cloud Run / Render URL
 *
 * Source: docs/android-integration/RetrofitClient.kt (Khumo handover kit).
 */
object RetrofitClient {

    private const val TAG = "RetrofitClient"

    /** Emulator-friendly default — change when testing on a physical device. */
    private const val BASE_URL = "http://10.0.2.2:8090/"

    private val authInterceptor = Interceptor { chain ->
        val user = FirebaseAuth.getInstance().currentUser
        val request = if (user != null) {
            val token = try {
                Tasks.await(user.getIdToken(false)).token
            } catch (ex: Exception) {
                Log.w(TAG, "Failed to fetch Firebase ID token", ex)
                null
            }
            if (token != null) {
                Log.d(TAG, "Attaching Bearer token for uid=${user.uid}")
                chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer $token")
                    .build()
            } else {
                chain.request()
            }
        } else {
            Log.d(TAG, "No signed-in user — sending request without auth header")
            chain.request()
        }
        chain.proceed(request)
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    val api: CampusCompanionApi by lazy {
        Log.i(TAG, "Creating Retrofit client baseUrl=$BASE_URL")
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CampusCompanionApi::class.java)
    }
}

fun HttpException.toApiError(): ApiError? = try {
    val body = response()?.errorBody()?.string()
    if (body != null) Gson().fromJson(body, ApiError::class.java) else null
} catch (ex: Exception) {
    null
}
