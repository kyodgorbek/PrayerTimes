package com.yodgorbek.prayertimesapp.data.remote

import com.yodgorbek.prayertimesapp.data.model.PrayerTimeResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlinx.coroutines.delay
import javax.inject.Inject

class ApiService @Inject constructor(private val client: HttpClient) {
    suspend fun getPrayerTimesByCity(city: String, country: String): PrayerTimeResponse {
        return retry(3) {
            client.get("https://api.aladhan.com/v1/timingsByCity") {
                parameter("city", city)
                parameter("country", country)
                parameter("method", "2") // ISNA
            }.body()
        }
    }

    suspend fun getPrayerTimesByLocation(latitude: Double, longitude: Double): PrayerTimeResponse {
        return retry(3) {
            client.get("https://api.aladhan.com/v1/timings") {
                parameter("latitude", latitude)
                parameter("longitude", longitude)
                parameter("method", "2")
            }.body()
        }
    }

    private suspend fun <T> retry(attempts: Int, block: suspend () -> T): T {
        var lastException: Exception? = null
        repeat(attempts) { attempt ->
            try {
                return block()
            } catch (e: Exception) {
                lastException = e
                delay(1000L * (attempt + 1)) // Exponential backoff
            }
        }
        throw lastException ?: IllegalStateException("Retry failed")
    }
}