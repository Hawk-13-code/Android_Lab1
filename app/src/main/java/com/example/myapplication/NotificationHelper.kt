package com.example.myapplication

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

object NotificationHelper {
    private const val CHANNEL_ID = "weather_change_channel"
    private const val CHANNEL_NAME = "Изменение погоды"
    private const val NOTIFICATION_ID_BASE = 2000

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Уведомления об изменении погоды в избранных городах"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    fun showWeatherChangeNotification(
        context: Context,
        cityName: String,
        oldTemp: Double,
        newTemp: Double,
        description: String
    ) {
        val message = "В городе $cityName температура изменилась с ${oldTemp.toInt()}° до ${newTemp.toInt()}°. $description"
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // можно заменить на свою иконку
            .setContentTitle("🌦️ Изменение погоды")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        with(NotificationManagerCompat.from(context)) {
            notify("weather_$cityName".hashCode(), notification)
        }
    }
}