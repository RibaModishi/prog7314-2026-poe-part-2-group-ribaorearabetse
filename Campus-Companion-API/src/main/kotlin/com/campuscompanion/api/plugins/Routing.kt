package com.campuscompanion.api.plugins

import com.campuscompanion.api.repositories.EventRepository
import com.campuscompanion.api.repositories.FavouriteRepository
import com.campuscompanion.api.repositories.LocationRepository
import com.campuscompanion.api.repositories.UserRepository
import com.campuscompanion.api.routes.authRoutes
import com.campuscompanion.api.routes.eventRoutes
import com.campuscompanion.api.routes.favouriteRoutes
import com.campuscompanion.api.routes.healthRoutes
import com.campuscompanion.api.routes.locationRoutes
import com.campuscompanion.api.routes.recommendationRoutes
import com.campuscompanion.api.services.WeatherService
import io.ktor.server.application.Application
import io.ktor.server.routing.routing

fun Application.configureRouting() {
    val userRepository = UserRepository()
    val locationRepository = LocationRepository()
    val eventRepository = EventRepository()
    val favouriteRepository = FavouriteRepository()
    val weatherService = WeatherService()

    routing {
        healthRoutes()
        authRoutes(userRepository)
        locationRoutes(locationRepository)
        eventRoutes(eventRepository)
        favouriteRoutes(favouriteRepository, locationRepository, eventRepository)
        recommendationRoutes(locationRepository, weatherService, eventRepository)
    }
}
