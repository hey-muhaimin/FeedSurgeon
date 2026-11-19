package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.example.MainActivity
import com.example.R

object ForegroundProtectionNotifier {

    const val CHANNEL_ID = "feed_surgeon_protection_channel"
    const val NOTIFICATION_ID = 4107

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            ?: return
        if (manager.getNotificationChannel(CHANNEL_ID) != null) return

        val channel = NotificationChannel(
            CHANNEL_ID,
            "On-Device Protection",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Keeps Stillspace active in the background to protect your focus on-device even when cleared from recents."
            setShowBadge(false)
            enableLights(false)
            enableVibration(false)
        }
        manager.createNotificationChannel(channel)
    }

    fun buildNotification(context: Context, activeInstalledCount: Int): Notification {
        ensureChannel(context)
        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentIntent = PendingIntent.getActivity(
            context,
            0,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val contentText = if (activeInstalledCount > 0) {
            val noun = if (activeInstalledCount == 1) "app" else "apps"
            "Protecting focus across $activeInstalledCount installed $noun on-device"
        } else {
            "On-device focus guard ready \u00B7 tap to configure"
        }

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_shield_notification)
            .setContentTitle("Stillspace active")
            .setContentText(contentText)
            .setContentIntent(contentIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .build()
    }

    fun startForegroundProtection(service: Service, activeInstalledCount: Int) {
        val notification = buildNotification(service, activeInstalledCount)
        val serviceType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
        } else {
            0
        }
        runCatching {
            ServiceCompat.startForeground(service, NOTIFICATION_ID, notification, serviceType)
        }
    }

    fun updateNotification(context: Context, activeInstalledCount: Int) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            ?: return
        val notification = buildNotification(context, activeInstalledCount)
        runCatching {
            manager.notify(NOTIFICATION_ID, notification)
        }
    }

    fun stopForegroundProtection(service: Service) {
        runCatching {
            ServiceCompat.stopForeground(service, ServiceCompat.STOP_FOREGROUND_REMOVE)
        }
    }
}
