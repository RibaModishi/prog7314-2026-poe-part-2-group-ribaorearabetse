package com.campuscompanion.api.services

import com.campuscompanion.api.config.AppConfig
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory

@Serializable
data class WeatherSnapshot(
    val condition: String,        // normalised, e.g. "RAIN", "CLEAR", "HOT", "COLD", "WIND"
    val rawDescription: String,
    val temperatureCelsius: Double,
    val windSpeedMs: Double,
    val isMock: Boolean = false
)

@Serializable
private data class OpenWeatherResponse(
    val weather: List<WeatherEntry> = emptyList(),
    val main: MainEntry,
    val wind: WindEntry = WindEntry()
)

@Serializable
private data class WeatherEntry(val main: String, val description: String)

@Serializable
private data class MainEntry(val temp: Double)

@Serializable
private data class WindEntry(val speed: Double = 0.0)

/**
 * Fetches current weather for the campus coordinates from OpenWeather and
 * normalises it into the small set of conditions Riba's recommendation rules
 * (isIndoor / isCovered / hasAirConditioning / isOutdoor) key off.
 *
 * If OPENWEATHER_API_KEY is not configured, returns a clearly-labelled mock
 * snapshot (overridable via the `mockCondition` query param on the route) so
 * the endpoint remains testable end-to-end before a real key is obtained.
 */
class WeatherService {

    private val logger = LoggerFactory.getLogger(WeatherService::class.java)

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    suspend fun currentWeather(mockCondition: String? = null): WeatherSnapshot {
        val apiKey = AppConfig.openWeatherApiKey

        if (apiKey == null) {
            val condition = mockCondition?.uppercase() ?: "CLEAR"
            logger.warn("OPENWEATHER_API_KEY not set - returning mock weather condition '{}'", condition)
            return mockSnapshot(condition)
        }

        return try {
            val response: OpenWeatherResponse = client.get("https://api.openweathermap.org/data/2.5/weather") {
                parameter("lat", AppConfig.campusLatitude)
                parameter("lon", AppConfig.campusLongitude)
                parameter("appid", apiKey)
                parameter("units", "metric")
            }.body()

            val main = response.weather.firstOrNull()?.main?.uppercase() ?: "CLEAR"
            WeatherSnapshot(
                condition = normaliseCondition(main, response.main.temp, response.wind.speed),
                rawDescription = response.weather.firstOrNull()?.description ?: main,
                temperatureCelsius = response.main.temp,
                windSpeedMs = response.wind.speed,
                isMock = false
            )
        } catch (ex: Exception) {
            logger.error("Failed to fetch weather from OpenWeather, falling back to mock CLEAR: {}", ex.message)
            mockSnapshot("CLEAR")
        }
    }

    private fun mockSnapshot(condition: String): WeatherSnapshot {
        val temp = when (condition) {
            "HOT" -> 32.0
            "COLD" -> 8.0
            "RAIN", "THUNDERSTORM" -> 18.0
            "WIND" -> 20.0
            else -> 22.0
        }
        val wind = if (condition == "WIND") 12.0 else 3.0
        return WeatherSnapshot(
            condition = condition,
            rawDescription = "Mock weather ($condition) - set OPENWEATHER_API_KEY for live data",
            temperatureCelsius = temp,
            windSpeedMs = wind,
            isMock = true
        )
    }

    /** Maps OpenWeather's raw `main` field + temperature/wind onto our 5 recommendation buckets. */
    private fun normaliseCondition(rawMain: String, tempCelsius: Double, windSpeedMs: Double): String {
        return when {
            rawMain.contains("RAIN") || rawMain.contains("THUNDERSTORM") || rawMain.contains("DRIZZLE") -> "RAIN"
            windSpeedMs >= 10.0 -> "WIND"
            tempCelsius >= 28.0 -> "HOT"
            tempCelsius <= 12.0 -> "COLD"
            else -> "CLEAR"
        }
    }
}
