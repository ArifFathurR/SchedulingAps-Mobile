package com.example.schedulleapps.nontifikasi

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("title") ?: "Pengingat"
        val message = intent.getStringExtra("message") ?: "Ada kegiatan hari ini"
        NotificationScheduler.showNotification(context, title, message)
    }
}
