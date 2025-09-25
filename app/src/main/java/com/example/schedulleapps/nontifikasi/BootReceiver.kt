package com.example.schedulleapps.nontifikasi

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.schedulleapps.worker.ScheduleWorker

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("BootReceiver", "Device rebooted, scheduling notifications again")
            val workRequest = OneTimeWorkRequestBuilder<ScheduleWorker>().build()
            WorkManager.getInstance(context).enqueue(workRequest)
        }
    }
}
