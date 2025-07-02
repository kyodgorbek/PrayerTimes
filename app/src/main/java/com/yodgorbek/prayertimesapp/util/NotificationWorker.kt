package com.yodgorbek.prayertimesapp.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.yodgorbek.prayertimesapp.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class NotificationWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val prayerName = inputData.getString("prayerName") ?: return Result.failure()
        val prayerTime = inputData.getString("prayerTime") ?: return Result.failure()
        val azanSound = inputData.getString("azanSound") ?: return Result.failure()
        val azanSoundEnabled = inputData.getBoolean("azanSoundEnabled", true)

        sendNotification(prayerName, prayerTime, azanSound, azanSoundEnabled)
        return Result.success()
    }

    private suspend fun sendNotification(prayerName: String, prayerTime: String, azanSound: String, azanSoundEnabled: Boolean) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "prayer_notifications"
        val channel = NotificationChannel(
            channelId,
            "Prayer Times",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            if (!azanSoundEnabled) {
                setSound(null, null)
            }
        }
        notificationManager.createNotificationChannel(channel)

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(applicationContext.getString(R.string.notification_title, prayerName))
            .setContentText(applicationContext.getString(R.string.notification_text, prayerName, prayerTime))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .apply {
                if (!azanSoundEnabled) {
                    setSound(null)
                }
            }
            .build()

        if (azanSoundEnabled) {
            withContext(Dispatchers.IO) {
                playAzanSound(azanSound)
            }
        }

        notificationManager.notify(prayerName.hashCode(), notification)
    }

    private fun playAzanSound(azanSound: String) {
        try {
            val assetFileDescriptor = applicationContext.assets.openFd(azanSound)
            MediaPlayer().apply {
                setDataSource(assetFileDescriptor.fileDescriptor, assetFileDescriptor.startOffset, assetFileDescriptor.length)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .build()
                )
                prepare()
                start()
                setOnCompletionListener { release() }
                assetFileDescriptor.close()
            }
        } catch (e: Exception) {
            // Log error
        }
    }
}
