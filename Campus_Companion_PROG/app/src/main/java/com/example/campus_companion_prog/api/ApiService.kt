package com.example.campus_companion_prog.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit interface for every Campus Companion API endpoint.
 * Auth headers are attached by RetrofitClient — do not pass tokens manually.
 *
 * Source: docs/android-integration/ApiService.kt (Khumo handover kit).
 */
interface CampusCompanionApi {

    @POST("api/auth/profile")
    suspend fun upsertProfile(@Body body: UpsertProfileRequest): UserProfile

    @GET("api/users/me")
    suspend fun getMyProfile(): UserProfile

    @PUT("api/users/me/settings")
    suspend fun updateSettings(@Body body: UpdateSettingsRequest): UserProfile

    @GET("api/locations")
    suspend fun getLocations(@Query("category") category: String? = null): List<CampusLocation>

    @GET("api/locations/{id}")
    suspend fun getLocation(@Path("id") id: String): CampusLocation

    @GET("api/events")
    suspend fun getEvents(
        @Query("category") category: String? = null,
        @Query("date") date: String? = null
    ): List<EventOrOpportunity>

    @GET("api/events/{id}")
    suspend fun getEvent(@Path("id") id: String): EventOrOpportunity

    @GET("api/favourites")
    suspend fun getFavourites(): List<Favourite>

    @POST("api/favourites")
    suspend fun addFavourite(@Body body: CreateFavouriteRequest): Favourite

    @DELETE("api/favourites/{id}")
    suspend fun deleteFavourite(@Path("id") id: String): Response<Unit>

    @POST("api/favourites/sync")
    suspend fun syncFavourites(@Body body: SyncRequest): SyncResponse

    @GET("api/recommendations/weather")
    suspend fun getWeatherRecommendation(
        @Query("mockCondition") mockCondition: String? = null
    ): WeatherRecommendation

    @GET("api/recommendations/events-weather")
    suspend fun getEventWeatherWarnings(
        @Query("mockCondition") mockCondition: String? = null
    ): List<EventWeatherWarning>
}
