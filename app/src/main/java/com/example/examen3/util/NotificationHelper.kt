package com.example.examen3.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.examen3.R

object NotificationHelper {
    private const val CHANNEL_ID = "exposure_alert"

    fun showExposureAlert(ctx: Context, n: Int) {
        val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                "Alertas de Exposición",
                NotificationManager.IMPORTANCE_HIGH
            )
        )
        val notif = NotificationCompat.Builder(ctx, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_notify_error)      // crea un simple vector
            .setContentTitle("Posible exposición")
            .setContentText("Has estado cerca de $n caso(s) positivo(s).")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        NotificationManagerCompat.from(ctx).notify(1, notif)
    }
}
