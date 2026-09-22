package com.campuscompanion.android.api

import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Builds the Retrofit client used to talk to the Campus Companion API.
 * Copy into your Android app and adjust the package name + BASE_URL.
 *
 * BASE_URL notes:
 *  - Android Emulator hitting a Ktor server running on your PC: use "http://10.0.2.2:8090/"
 *    (10.0.2.2 is the emulator's alias for your machine's localhost - "localhost" from
 *    the emulator points at the emulator itself, not your PC). 8090 is the API's
 *    default local port (see AppConfig.kt / README) - adjust if you set $PORT differently.
 *  - Physical device on the same Wi-Fi as your PC: use "http://<your-pc-lan-ip>:8090/"
 *  - Once Khumo deploys the API (Cloud Run/Render), swap this for the real HTTPS URL.
 */
object RetrofitClient {

    private const val BASE_URL = "http://10.0.2.2:8090/" // TODO: replace with deployed URL when available

    /**
     * Adds "Authorization: Bearer <firebase-id-token>" to every outgoing request.
     * Uses Tasks.await() because OkHttp interceptors run synchronously (off the main
     * thread already, since Retrofit calls happen inside your suspend/repository layer).
     * If there's no signed-in user, the request is sent without the header - the public
     * endpoints (locations/events/weather) will still work; user-scoped endpoints will
     * correctly come back as 401 (see ApiModels.ApiError).
     */
    private val authInterceptor = Interceptor { chain ->
        val user = FirebaseAuth.getInstance().currentUser
        val request = if (user != null) {
            val token = try {
                Tasks.await(user.getIdToken(false)).token
            } catch (ex: Exception) {
                null
            }
            if (token != null) {
                chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer $token")
                    .build()
            } else {
                chain.request()
            }
        } else {
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
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CampusCompanionApi::class.java)
    }
}

/**
 * Every failed call throws HttpException (for HTTP error codes) - use this to turn it
 * into the structured ApiError the backend always returns, so you can show a friendly
 * message instead of a raw stack trace. See docs/API_DOCUMENTATION.md for the full
 * "Standard error shape" table (400/401/403/404/500).
 *
 * Usage:
 *   try {
 *       val profile = RetrofitClient.api.getMyProfile()
 *   } catch (ex: HttpException) {
 *       val apiError = ex.toApiError()
 *       showError(apiError?.message ?: "Something went wrong")
 *   }
 */
fun HttpException.toApiError(): ApiError? = try {
    val body = response()?.errorBody()?.string()
    if (body != null) Gson().fromJson(body, ApiError::class.java) else null
} catch (ex: Exception) {
    null
}
