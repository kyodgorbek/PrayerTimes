package com.yodgorbek.prayertimesapp.util

import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.delay
import java.util.Locale
import android.Manifest
import kotlinx.coroutines.tasks.await

class LocationHelper(private val context: Context) {
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    suspend fun getLastLocation(attempts: Int = 3): Pair<Double, Double>? {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return null
        }
        repeat(attempts) { attempt ->
            try {
                val location = fusedLocationClient.lastLocation.await()
                return location?.let { Pair(it.latitude, it.longitude) }
            } catch (e: Exception) {
                delay(1000L * (attempt + 1)) // Exponential backoff
            }
        }
        return null
    }

    suspend fun getCityFromLocation(latitude: Double, longitude: Double): Triple<String, String, String>? {
        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses: List<Address>? = geocoder.getFromLocation(latitude, longitude, 1)
            val address = addresses?.firstOrNull()
            return address?.let {
                Triple(
                    it.locality ?: "Unknown City",
                    it.countryName ?: "Unknown Country",
                    it.countryCode ?: "Unknown"
                )
            }
        } catch (e: Exception) {
            return null
        }
    }
}