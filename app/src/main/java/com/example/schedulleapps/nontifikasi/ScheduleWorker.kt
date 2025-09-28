package com.example.schedulleapps.worker

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.schedulleapps.api.ApiClient
import com.example.schedulleapps.model.Schedule
import com.example.schedulleapps.nontifikasi.NotificationScheduler
import java.text.SimpleDateFormat
import java.util.*

class ScheduleWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {

    override fun doWork(): Result {
        val prefs = applicationContext.getSharedPreferences("APP", Context.MODE_PRIVATE)
        val token = prefs.getString("TOKEN", "") ?: ""
        val role = prefs.getString("ROLE", "") ?: ""

        if (token.isEmpty()) return Result.retry()

        try {
            val response = ApiClient.instance.getSchedules("Bearer $token").execute()
            if (response.isSuccessful && response.body() != null) {
                val schedules = response.body()!!.data
                Log.d("ScheduleWorker", "Role: $role → total jadwal: ${schedules.size}")

                // 🔹 Cek apakah ada schedule baru
                checkNewSchedules(schedules)

                // 🔹 Semua role tetap dapat notifikasi pengingat
                scheduleNotifications(role, schedules)
            } else {
                Log.e("ScheduleWorker", "API Response failed: ${response.code()}")
                return Result.retry()
            }
        } catch (e: Exception) {
            Log.e("ScheduleWorker", "API Error: ${e.message}")
            return Result.retry()
        }

        return Result.success()
    }

    /**
     * Cek apakah ada schedule baru dibandingkan dengan yang tersimpan di SharedPreferences
     */
    private fun checkNewSchedules(schedules: List<Schedule>) {
        val prefs = applicationContext.getSharedPreferences("APP", Context.MODE_PRIVATE)
        val savedIds = prefs.getStringSet("SAVED_SCHEDULE_IDS", emptySet())?.toMutableSet() ?: mutableSetOf()

        val newSchedules = schedules.filter { !savedIds.contains(it.id.toString()) }

        if (newSchedules.isNotEmpty()) {
            for (s in newSchedules) {
                // Kirim notifikasi untuk schedule baru
                NotificationScheduler.showNotification(
                    applicationContext,
                    "Jadwal Baru",
                    "Event: ${s.namaEvent} pada ${s.tanggal} jam ${s.jamMulai}"
                )
                Log.d("ScheduleWorker", "Notif jadwal baru dikirim untuk schedule ${s.id}")

                // Simpan id ke SharedPreferences supaya tidak notifikasi ulang
                savedIds.add(s.id.toString())
            }

            prefs.edit().putStringSet("SAVED_SCHEDULE_IDS", savedIds).apply()
        }
    }

    private fun scheduleNotifications(role: String, schedules: List<Schedule>) {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val now = System.currentTimeMillis()

        for (s in schedules) {
            try {
                val dateTime = "${s.tanggal} ${s.jamMulai}"
                val startTime = sdf.parse(dateTime) ?: continue
                val calStart = Calendar.getInstance().apply {
                    time = startTime
                    set(Calendar.SECOND, 0)
                }

                // H-3 hari
                scheduleIfFuture(calStart.clone() as Calendar, -3, "H-3", s, now)
                // H-1 hari
                scheduleIfFuture(calStart.clone() as Calendar, -1, "H-1", s, now)
                // H-2 jam
                scheduleIfFutureHour(calStart.clone() as Calendar, -2, "H-2 jam", s, now)
                // H-2 menit
                scheduleIfFutureMinute(calStart.clone() as Calendar, -2, "H-2 menit", s, now)

            } catch (e: Exception) {
                Log.e("ScheduleWorker", "Error parsing schedule ${s.id}: ${e.message}")
            }
        }
    }

    private fun scheduleIfFuture(cal: Calendar, daysOffset: Int, prefix: String, s: Schedule, now: Long) {
        cal.add(Calendar.DAY_OF_YEAR, daysOffset)
        if (cal.timeInMillis > now) {
            NotificationScheduler.scheduleNotification(
                applicationContext,
                "Pengingat Kegiatan ($prefix)",
                "Event: ${s.namaEvent} pada ${s.tanggal} jam ${s.jamMulai}",
                cal.timeInMillis,
                s.id * 1000 + daysOffset
            )
            Log.d("ScheduleWorker", "Notif $prefix dijadwalkan untuk schedule ${s.id}")
        }
    }

    private fun scheduleIfFutureHour(cal: Calendar, hoursOffset: Int, prefix: String, s: Schedule, now: Long) {
        cal.add(Calendar.HOUR_OF_DAY, hoursOffset)
        if (cal.timeInMillis > now) {
            NotificationScheduler.scheduleNotification(
                applicationContext,
                "Pengingat Kegiatan ($prefix)",
                "Event: ${s.namaEvent} dimulai jam ${s.jamMulai}",
                cal.timeInMillis,
                s.id * 10000 + hoursOffset
            )
            Log.d("ScheduleWorker", "Notif $prefix dijadwalkan untuk schedule ${s.id}")
        }
    }

    private fun scheduleIfFutureMinute(cal: Calendar, minutesOffset: Int, prefix: String, s: Schedule, now: Long) {
        cal.add(Calendar.MINUTE, minutesOffset)
        if (cal.timeInMillis > now) {
            NotificationScheduler.scheduleNotification(
                applicationContext,
                "Pengingat Kegiatan ($prefix)",
                "Event: ${s.namaEvent} sebentar lagi (${s.jamMulai})",
                cal.timeInMillis,
                s.id * 100000 + minutesOffset
            )
            Log.d("ScheduleWorker", "Notif $prefix dijadwalkan untuk schedule ${s.id}")
        }
    }
}
