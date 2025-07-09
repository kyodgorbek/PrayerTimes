package com.yodgorbek.prayertimesapp.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.Worker
import androidx.work.WorkerParameters

class NotificationWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    override fun doWork(): Result {
        val prayerName = inputData.getString("prayerName") ?: return Result.failure()
        val prayerTime = inputData.getString("prayerTime") ?: return Result.failure()
        val azanSound = inputData.getString("azanSound") ?: return Result.failure()
        val azanSoundEnabled = inputData.getBoolean("azanSoundEnabled", false)

        Log.d("NotificationWorker", "Triggered for $prayerName at $prayerTime | Azan: $azanSound")

        // 🔊 Play Azan sound if enabled
        if (azanSoundEnabled) {
            try {
                val resId = applicationContext.resources.getIdentifier(
                    azanSound.removeSuffix(".mp3"), // Remove extension if passed
                    "raw",
                    applicationContext.packageName
                )

                if (resId != 0) {
                    val mediaPlayer = MediaPlayer.create(applicationContext, resId)
                    mediaPlayer?.apply {
                        setOnCompletionListener { release() }
                        start()
                    }
                } else {
                    Log.e("NotificationWorker", "Sound resource not found in res/raw: $azanSound")
                }
            } catch (e: Exception) {
                Log.e("NotificationWorker", "Failed to play azan sound: ${e.message}", e)
                return Result.retry()
            }
        }

        // 🔔 Show notification if permission granted or version < Android 13
        if (ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
        ) {
            createNotificationChannelIfNeeded()

            val builder = NotificationCompat.Builder(applicationContext, "prayer_channel")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Prayer Time")
                .setContentText("$prayerName at $prayerTime")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)

            NotificationManagerCompat.from(applicationContext)
                .notify(prayerName.hashCode(), builder.build())
        } else {
            Log.w("NotificationWorker", "Notification permission not granted")
        }

        return Result.success()
    }

    private fun createNotificationChannelIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "prayer_channel",
                "Prayer Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Azan notifications for prayer times"
            }

            val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }
}
