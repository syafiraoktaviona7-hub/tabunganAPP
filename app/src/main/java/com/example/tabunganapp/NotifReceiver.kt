package com.example.tabunganapp

import android.net.Uri
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

class NotifReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        val channelId = "tabungan_channel_v2"

        val manager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 🔥 PINDAHKAN KE SINI (BIAR BISA DIPAKAI SEMUA)
        val soundUri = Uri.parse(
            "android.resource://" + context.packageName + "/" + R.raw.notif_sound
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val attributes = android.media.AudioAttributes.Builder()
                .setUsage(android.media.AudioAttributes.USAGE_NOTIFICATION)
                .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val channel = NotificationChannel(
                channelId,
                "Pengingat Nabung",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                setSound(soundUri, attributes)
            }

            manager.createNotificationChannel(channel)
        }

        val notif = NotificationCompat.Builder(context, channelId)
            .setContentTitle("💰 Waktunya Nabung!")
            .setContentText("Yuk isi tabungan kamu hari ini ✨")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            // .setSound(soundUri) // sekarang sudah aman
            .build()

        manager.notify(System.currentTimeMillis().toInt(), notif)
    }
}