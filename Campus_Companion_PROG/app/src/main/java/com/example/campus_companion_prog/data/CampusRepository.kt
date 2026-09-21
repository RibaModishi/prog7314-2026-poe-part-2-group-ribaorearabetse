package com.example.campus_companion_prog.data

import android.util.Log
import com.example.campus_companion_prog.api.CampusLocation
import com.example.campus_companion_prog.api.CreateFavouriteRequest
import com.example.campus_companion_prog.api.EventOrOpportunity
import com.example.campus_companion_prog.api.Favourite
import com.example.campus_companion_prog.api.RetrofitClient
import com.example.campus_companion_prog.api.toApiError
import retrofit2.HttpException

/**
 * Loads campus locations and events/opportunities from the Campus Companion API.
 */
class CampusRepository {

    companion object {
        private const val TAG = "CampusRepository"
    }

    suspend fun getLocations(category: String? = null): Result<List<CampusLocation>> = try {
        Log.i(TAG, "GET /api/locations category=$category")
        Result.success(RetrofitClient.api.getLocations(category))
    } catch (ex: HttpException) {
        Result.failure(apiError(ex))
    } catch (ex: Exception) {
        Log.e(TAG, "getLocations failed", ex)
        Result.failure(IllegalStateException(ex.message ?: "Could not load locations."))
    }

    suspend fun getLocation(id: String): Result<CampusLocation> = try {
        Log.i(TAG, "GET /api/locations/$id")
        Result.success(RetrofitClient.api.getLocation(id))
    } catch (ex: HttpException) {
        Result.failure(apiError(ex))
    } catch (ex: Exception) {
        Result.failure(IllegalStateException(ex.message ?: "Could not load location."))
    }

    suspend fun getEvents(category: String? = null): Result<List<EventOrOpportunity>> = try {
        Log.i(TAG, "GET /api/events category=$category")
        Result.success(RetrofitClient.api.getEvents(category = category))
    } catch (ex: HttpException) {
        Result.failure(apiError(ex))
    } catch (ex: Exception) {
        Log.e(TAG, "getEvents failed", ex)
        Result.failure(IllegalStateException(ex.message ?: "Could not load events."))
    }

    suspend fun getEvent(id: String): Result<EventOrOpportunity> = try {
        Log.i(TAG, "GET /api/events/$id")
        Result.success(RetrofitClient.api.getEvent(id))
    } catch (ex: HttpException) {
        Result.failure(apiError(ex))
    } catch (ex: Exception) {
        Result.failure(IllegalStateException(ex.message ?: "Could not load event."))
    }

    suspend fun addFavourite(itemType: String, itemId: String): Result<Favourite> = try {
        Log.i(TAG, "POST /api/favourites type=$itemType id=$itemId")
        Result.success(
            RetrofitClient.api.addFavourite(CreateFavouriteRequest(itemType, itemId))
        )
    } catch (ex: HttpException) {
        Result.failure(apiError(ex))
    } catch (ex: Exception) {
        Result.failure(IllegalStateException(ex.message ?: "Could not save item."))
    }

    suspend fun getFavourites(): Result<List<Favourite>> = try {
        Log.i(TAG, "GET /api/favourites")
        Result.success(RetrofitClient.api.getFavourites())
    } catch (ex: HttpException) {
        Result.failure(apiError(ex))
    } catch (ex: Exception) {
        Result.failure(IllegalStateException(ex.message ?: "Could not load saved items."))
    }

    suspend fun deleteFavourite(favouriteId: String): Result<Unit> = try {
        Log.i(TAG, "DELETE /api/favourites/$favouriteId")
        val response = RetrofitClient.api.deleteFavourite(favouriteId)
        if (response.isSuccessful) Result.success(Unit)
        else Result.failure(IllegalStateException("Could not remove saved item (${response.code()})"))
    } catch (ex: HttpException) {
        Result.failure(apiError(ex))
    } catch (ex: Exception) {
        Result.failure(IllegalStateException(ex.message ?: "Could not remove saved item."))
    }

    suspend fun getWeatherRecommendation(mockCondition: String? = null) = try {
        Log.i(TAG, "GET /api/recommendations/weather mock=$mockCondition")
        Result.success(RetrofitClient.api.getWeatherRecommendation(mockCondition))
    } catch (ex: HttpException) {
        Result.failure(apiError(ex))
    } catch (ex: Exception) {
        Result.failure(IllegalStateException(ex.message ?: "Could not load weather recommendations."))
    }

    suspend fun getEventWeatherWarnings(mockCondition: String? = null) = try {
        Log.i(TAG, "GET /api/recommendations/events-weather mock=$mockCondition")
        Result.success(RetrofitClient.api.getEventWeatherWarnings(mockCondition))
    } catch (ex: HttpException) {
        Result.failure(apiError(ex))
    } catch (ex: Exception) {
        Result.failure(IllegalStateException(ex.message ?: "Could not load event weather."))
    }

    private fun apiError(ex: HttpException): IllegalStateException {
        val msg = ex.toApiError()?.message ?: "API error (${ex.code()})"
        Log.e(TAG, msg, ex)
        return IllegalStateException(msg)
    }
}
