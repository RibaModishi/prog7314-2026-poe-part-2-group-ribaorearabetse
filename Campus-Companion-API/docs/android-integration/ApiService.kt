package com.campuscompanion.android.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit interface covering every Campus Companion API endpoint.
 * Auth headers are added automatically by the OkHttp interceptor in
 * RetrofitClient.kt - you do NOT need to pass the token manually here.
 *
 * Copy into your Android app and adjust the package name.
 */
interface CampusCompanionApi {

    // --- Auth & profile (require auth) ---

    @POST("api/auth/profile")
    suspend fun upsertProfile(@Body body: UpsertProfileRequest): UserProfile

    @GET("api/users/me")
    suspend fun getMyProfile(): UserProfile

    @PUT("api/users/me/settings")
    suspend fun updateSettings(@Body body: UpdateSettingsRequest): UserProfile

    // --- Campus locations (public, no auth needed) ---

    @GET("api/locations")
    suspend fun getLocations(@Query("category") category: String? = null): List<CampusLocation>

    @GET("api/locations/{id}")
    suspend fun getLocation(@Path("id") id: String): CampusLocation

    // --- Events & opportunities (public, no auth needed) ---

    @GET("api/events")
    suspend fun getEvents(
        @Query("category") category: String? = null,
        @Query("date") date: String? = null
    ): List<EventOrOpportunity>

    @GET("api/events/{id}")
    suspend fun getEvent(@Path("id") id: String): EventOrOpportunity

    // --- Favourites / Saved Items (require auth) ---

    @GET("api/favourites")
    suspend fun getFavourites(): List<Favourite>

    @POST("api/favourites")
    suspend fun addFavourite(@Body body: CreateFavouriteRequest): Favourite

    @DELETE("api/favourites/{id}")
    suspend fun deleteFavourite(@Path("id") id: String): Response<Unit>

    /** Call this once connectivity returns, with the Room-queued offline actions. */
    @POST("api/favourites/sync")
    suspend fun syncFavourites(@Body body: SyncRequest): SyncResponse

    // --- Weather recommendations (public, no auth needed) ---

    @GET("api/recommendations/weather")
    suspend fun getWeatherRecommendation(@Query("mockCondition") mockCondition: String? = null): WeatherRecommendation
}
