package com.yodgorbek.prayertimesapp.util

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.io.IOException

class NotificationWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    override fun doWork(): Result {
        val prayerName = inputData.getString("prayerName") ?: return Result.failure()
        val prayerTime = inputData.getString("prayerTime") ?: return Result.failure()
        val azanSound = inputData.getString("azanSound") ?: return Result.failure()
        val azanSoundEnabled = inputData.getBoolean("azanSoundEnabled", false)

        // Play Azan sound if enabled
        if (azanSoundEnabled) {
            try {
                val assetFileDescriptor = applicationContext.assets.openFd(azanSound)
                MediaPlayer().apply {
                    setDataSource(assetFileDescriptor.fileDescriptor, assetFileDescriptor.startOffset, assetFileDescriptor.length)
                    prepare()
                    start()
                    setOnCompletionListener { release() }
                }
            } catch (e: IOException) {
                Log.e("NotificationWorker", "Failed to play $azanSound: ${e.message}")
                return Result.retry() // Retry if file not found or error occurs
            }
        }

        // Show notification
        val builder = NotificationCompat.Builder(applicationContext, "prayer_channel")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Prayer Time")
            .setContentText("$prayerName at $prayerTime")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(applicationContext)) {
            notify(prayerName.hashCode(), builder.build())
        }

        return Result.success()
    }
}