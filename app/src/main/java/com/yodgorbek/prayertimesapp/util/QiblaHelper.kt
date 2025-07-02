package com.yodgorbek.prayertimesapp.util

import java.lang.Math.toDegrees
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin


object QiblaHelper {
    private const val KAABA_LATITUDE = 21.4225
    private const val KAABA_LONGITUDE = 39.8262

    fun calculateQiblaDirection(latitude: Double, longitude: Double): Float {
        val latRad = Math.toRadians(latitude)
        val lonRad = Math.toRadians(longitude)
        val kaabaLatRad = Math.toRadians(KAABA_LATITUDE)
        val kaabaLonRad = Math.toRadians(KAABA_LONGITUDE)

        val deltaLon = kaabaLonRad - lonRad
        val y = sin(deltaLon) * cos(kaabaLatRad)
        val x = cos(latRad) * sin(kaabaLatRad) - sin(latRad) * cos(kaabaLatRad) * cos(deltaLon)
        val bearingRad = atan2(y, x)
        var bearingDeg = toDegrees(bearingRad).toFloat()
        if (bearingDeg < 0) bearingDeg += 360
        return bearingDeg
    }
}
