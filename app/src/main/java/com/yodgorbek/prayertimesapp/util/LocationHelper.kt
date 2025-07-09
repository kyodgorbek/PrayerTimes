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
import android.annotation.SuppressLint
import android.os.Looper
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resume

class LocationHelper(private val context: Context) {
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    suspend fun getLastLocation(): Pair<Double, Double>? {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return null
        }

        val last = fusedLocationClient.lastLocation.await()
        if (last != null) {
            return Pair(last.latitude, last.longitude)
        }

        return suspendCancellableCoroutine { continuation ->
            val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000)
                .setMaxUpdates(1)
                .build()

            val callback = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    fusedLocationClient.removeLocationUpdates(this)
                    val location = result.lastLocation
                    if (location != null) {
                        continuation.resume(Pair(location.latitude, location.longitude))
                    } else {
                        continuation.resume(null)
                    }
                }

                override fun onLocationAvailability(p0: LocationAvailability) {
                    if (!p0.isLocationAvailable) {
                        fusedLocationClient.removeLocationUpdates(this)
                        continuation.resume(null)
                    }
                }
            }

            fusedLocationClient.requestLocationUpdates(request, callback, Looper.getMainLooper())
            continuation.invokeOnCancellation {
                fusedLocationClient.removeLocationUpdates(callback)
            }
        }
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