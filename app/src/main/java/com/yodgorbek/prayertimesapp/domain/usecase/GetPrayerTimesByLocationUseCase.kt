package com.yodgorbek.prayertimesapp.domain.usecase

import com.yodgorbek.prayertimesapp.domain.model.PrayerTime
import com.yodgorbek.prayertimesapp.domain.repository.PrayerTimeRepository
import javax.inject.Inject

class GetPrayerTimesByLocationUseCase @Inject constructor(
    private val repository: PrayerTimeRepository
) {
    suspend operator fun invoke(latitude: Double, longitude: Double): Result<PrayerTime> {
        return try {
            Result.success(repository.getPrayerTimesByLocation(latitude, longitude))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}