package com.campuscompanion.api.util

import com.campuscompanion.api.models.FavouriteItemType
import com.campuscompanion.api.models.ItemType
import com.campuscompanion.api.models.LocationCategory
import com.campuscompanion.api.models.SupportedLanguage

/** Shared, backend-side validation rules for incoming request payloads. */
object Validation {

    private val emailRegex = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    fun requireNonBlank(value: String, fieldName: String) {
        if (value.isBlank()) throw BadRequestException("'$fieldName' is required and cannot be blank")
    }

    fun requireValidEmail(email: String) {
        requireNonBlank(email, "email")
        if (!emailRegex.matches(email)) throw BadRequestException("'$email' is not a valid email address")
    }

    fun requireValidLanguage(code: String) {
        if (code !in SupportedLanguage.ALL) {
            throw BadRequestException("'$code' is not a supported language. Supported: ${SupportedLanguage.ALL}")
        }
    }

    fun requireValidLocationCategory(category: String) {
        if (category !in LocationCategory.ALL) {
            throw BadRequestException("'$category' is not a valid location category. Valid categories: ${LocationCategory.ALL}")
        }
    }

    fun requireValidItemType(type: String) {
        if (type !in ItemType.ALL) {
            throw BadRequestException("'$type' is not a valid event/opportunity type. Valid types: ${ItemType.ALL}")
        }
    }

    fun requireValidFavouriteItemType(type: String) {
        if (type !in FavouriteItemType.ALL) {
            throw BadRequestException("'$type' is not a valid favourite item type. Valid types: ${FavouriteItemType.ALL}")
        }
    }

    fun requireValidCoordinates(latitude: Double, longitude: Double) {
        if (latitude < -90.0 || latitude > 90.0) throw BadRequestException("latitude must be between -90 and 90")
        if (longitude < -180.0 || longitude > 180.0) throw BadRequestException("longitude must be between -180 and 180")
    }

    fun requireQuietLevel(level: Int) {
        if (level < 1 || level > 5) throw BadRequestException("quietLevel must be between 1 and 5")
    }
}
