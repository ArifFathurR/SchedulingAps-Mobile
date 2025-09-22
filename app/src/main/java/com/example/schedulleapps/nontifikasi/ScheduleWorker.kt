package com.example.schedulleapps.worker

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.schedulleapps.api.ApiClient
import com.example.schedulleapps.model.Schedule
import com.example.schedulleapps.model.ScheduleResponse
import com.example.schedulleapps.nontifikasi.NotificationScheduler
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class ScheduleWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {

    override fun doWork(): Result {
        val prefs = applicationContext.getSharedPreferences("APP", Context.MODE_PRIVATE)
        val token = prefs.getString("TOKEN", "") ?: ""
        if (token.isEmpty()) return Result.retry()

        ApiClient.instance.getSchedules("Bearer $token")
            .enqueue(object : Callback<ScheduleResponse> {
                override fun onResponse(call: Call<ScheduleResponse>, response: Response<ScheduleResponse>) {
                    if (response.isSuccessful && response.body() != null) {
                        scheduleNotifications(response.body()!!.data)
                    }
                }
                override fun onFailure(call: Call<ScheduleResponse>, t: Throwable) {
                    Log.e("ScheduleWorker", "API Error: ${t.message}")
                }
            })

        return Result.success()
    }

    private fun scheduleNotifications(schedules: List<Schedule>) {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val now = System.currentTimeMillis()

        for (s in schedules) {
            try {
                val dateTime = "${s.tanggal} ${s.jamMulai}"
                val startTime = sdf.parse(dateTime) ?: continue

                val cal = Calendar.getInstance().apply {
                    time = startTime
                    add(Calendar.HOUR_OF_DAY, -2) // 2 jam sebelum
                    set(Calendar.SECOND, 0)
                }

                if (cal.timeInMillis > now) {
                    NotificationScheduler.scheduleNotification(
                        applicationContext,
                        "Pengingat Kegiatan",
                        "Event: ${s.namaEvent} dimulai jam ${s.jamMulai}",
                        cal.timeInMillis,
                        s.id
                    )
                }
            } catch (e: Exception) {
                Log.e("ScheduleWorker", "Error parsing: ${e.message}")
            }
        }
    }
}
