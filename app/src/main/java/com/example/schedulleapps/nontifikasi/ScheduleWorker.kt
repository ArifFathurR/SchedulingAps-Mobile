package com.example.schedulleapps.worker

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.schedulleapps.api.ApiClient
import com.example.schedulleapps.model.Assist
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
            // 🔹 Ambil Schedule
            val response = ApiClient.instance.getSchedules("Bearer $token").execute()
            if (response.isSuccessful && response.body() != null) {
                val schedules = response.body()!!.data
                Log.d("ScheduleWorker", "Role: $role → total jadwal: ${schedules.size}")

                checkNewSchedules(schedules)
                scheduleNotifications(role, schedules)
            } else {
                Log.e("ScheduleWorker", "API Response failed: ${response.code()}")
                return Result.retry()
            }

            // 🔹 Ambil Assist
            val assistResponse = ApiClient.instance.getAssists("Bearer $token").execute()
            if (assistResponse.isSuccessful && assistResponse.body() != null) {
                val assists = assistResponse.body()!!.data
                checkNewAssists(assists)
                scheduleAssistNotifications(assists)
            }

        } catch (e: Exception) {
            Log.e("ScheduleWorker", "API Error: ${e.message}")
            return Result.retry()
        }

        return Result.success()
    }

    /** NOTIF SCHEDULE BARU */
    private fun checkNewSchedules(schedules: List<Schedule>) {
        val prefs = applicationContext.getSharedPreferences("APP", Context.MODE_PRIVATE)
        val savedIds = prefs.getStringSet("SAVED_SCHEDULE_IDS", emptySet())?.toMutableSet() ?: mutableSetOf()
        val newSchedules = schedules.filter { !savedIds.contains(it.id.toString()) }

        newSchedules.forEach { s ->
            NotificationScheduler.showNotification(
                applicationContext,
                "Jadwal Baru",
                "Event: ${s.namaEvent} pada ${s.tanggal} jam ${s.jamMulai}"
            )
            savedIds.add(s.id.toString())
            Log.d("ScheduleWorker", "Notif jadwal baru dikirim: ${s.id}")
        }
        prefs.edit().putStringSet("SAVED_SCHEDULE_IDS", savedIds).apply()
    }

    /** NOTIF ASSIST BARU */
    private fun checkNewAssists(assists: List<Assist>) {
        val prefs = applicationContext.getSharedPreferences("APP", Context.MODE_PRIVATE)
        val savedIds = prefs.getStringSet("SAVED_ASSIST_IDS", emptySet())?.toMutableSet() ?: mutableSetOf()
        val newAssists = assists.filter { !savedIds.contains(it.id.toString()) }

        newAssists.forEach { a ->
            NotificationScheduler.showNotification(
                applicationContext,
                "Jadwal Assist Baru",
                "Anda terlibat di suatu event pada ${a.tanggal} jam ${a.jamMulai}"
            )
            savedIds.add(a.id.toString())
            Log.d("ScheduleWorker", "Notif assist baru dikirim: ${a.id}")
        }
        prefs.edit().putStringSet("SAVED_ASSIST_IDS", savedIds).apply()
    }

    /** SCHEDULE NOTIF PERIODIK H-3,H-1,H-2 JAM,H-2 MENIT */
    private fun scheduleNotifications(role: String, schedules: List<Schedule>) {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val now = System.currentTimeMillis()

        schedules.forEach { s ->
            try {
                val dateTime = "${s.tanggal} ${s.jamMulai}"
                val startTime = sdf.parse(dateTime) ?: return@forEach
                val calStart = Calendar.getInstance().apply { time = startTime; set(Calendar.SECOND, 0) }

                scheduleOrShow(calStart.clone() as Calendar, -3, 0, "H-3", s, now, isDayOffset = true)
                scheduleOrShow(calStart.clone() as Calendar, -1, 1, "H-1", s, now, isDayOffset = true)
                scheduleOrShow(calStart.clone() as Calendar, -2, 2, "H-2 jam", s, now, isHourOffset = true)
                scheduleOrShow(calStart.clone() as Calendar, -2, 3, "H-2 menit", s, now, isMinuteOffset = true)
            } catch (e: Exception) {
                Log.e("ScheduleWorker", "Error parsing schedule ${s.id}: ${e.message}")
            }
        }
    }

    /** ASSIST NOTIF PERIODIK H-3,H-1,H-2 MENIT */
    private fun scheduleAssistNotifications(assists: List<Assist>) {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val now = System.currentTimeMillis()

        assists.forEach { a ->
            if (a.tanggal == null || a.jamMulai == null) return@forEach
            try {
                val dateTime = "${a.tanggal} ${a.jamMulai}"
                val startTime = sdf.parse(dateTime) ?: return@forEach
                val calStart = Calendar.getInstance().apply { time = startTime; set(Calendar.SECOND, 0) }

                scheduleOrShowAssist(calStart.clone() as Calendar, -3, 0, "H-3", a, now, isDayOffset = true)
                scheduleOrShowAssist(calStart.clone() as Calendar, -1, 1, "H-1", a, now, isDayOffset = true)
                scheduleOrShowAssist(calStart.clone() as Calendar, -2, 2, "H-2 menit", a, now, isMinuteOffset = true)
            } catch (e: Exception) {
                Log.e("ScheduleWorker","Error schedule assist ${a.id}: ${e.message}")
            }
        }
    }

    /** Utility untuk schedule atau langsung show jika lewat ≤5 menit - Schedule */
    private fun scheduleOrShow(
        cal: Calendar,
        offset: Int,
        idOffset: Int,
        prefix: String,
        s: Schedule,
        now: Long,
        isDayOffset: Boolean = false,
        isHourOffset: Boolean = false,
        isMinuteOffset: Boolean = false
    ) {
        when {
            isDayOffset -> cal.add(Calendar.DAY_OF_YEAR, offset)
            isHourOffset -> cal.add(Calendar.HOUR_OF_DAY, offset)
            isMinuteOffset -> cal.add(Calendar.MINUTE, offset)
        }

        val title = "Pengingat Kegiatan ($prefix)"
        val message = "Event: ${s.namaEvent} pada ${s.tanggal} jam ${s.jamMulai}"
        val diff = now - cal.timeInMillis
        val prefs = applicationContext.getSharedPreferences("APP", Context.MODE_PRIVATE)
        val sentIds = prefs.getStringSet("SENT_NOTIF_IDS", mutableSetOf())?.toMutableSet() ?: mutableSetOf()
        val notifId = "schedule_${s.id}_$prefix"

        if (sentIds.contains(notifId)) {
            Log.d("ScheduleWorker", "Notif $prefix untuk schedule ${s.id} sudah dikirim sebelumnya → skip")
            return
        }

        if (cal.timeInMillis > now) {
            NotificationScheduler.scheduleNotification(
                applicationContext,
                title,
                message,
                cal.timeInMillis,
                s.id * 1000 + idOffset
            )
            Log.d("ScheduleWorker", "Notif $prefix dijadwalkan untuk schedule ${s.id}")
        } else if (diff <= 5 * 60 * 1000) {
            NotificationScheduler.showNotification(applicationContext, title, message)
            Log.d("ScheduleWorker", "Notif $prefix langsung dikirim untuk schedule ${s.id} (telat ≤5 menit)")
        } else {
            Log.d("ScheduleWorker", "Notif $prefix TIDAK dikirim untuk schedule ${s.id} (telat >5 menit)")
            return
        }

        // Simpan ID notifikasi agar tidak dikirim lagi
        sentIds.add(notifId)
        prefs.edit().putStringSet("SENT_NOTIF_IDS", sentIds).apply()
    }

    private fun scheduleOrShowAssist(
        cal: Calendar,
        offset: Int,
        idOffset: Int,
        prefix: String,
        a: Assist,
        now: Long,
        isDayOffset: Boolean = false,
        isHourOffset: Boolean = false,
        isMinuteOffset: Boolean = false
    ) {
        when {
            isDayOffset -> cal.add(Calendar.DAY_OF_YEAR, offset)
            isHourOffset -> cal.add(Calendar.HOUR_OF_DAY, offset)
            isMinuteOffset -> cal.add(Calendar.MINUTE, offset)
        }

        val title = "Pengingat Assist ($prefix)"
        val message = "Anda terlibat di suatu event pada ${a.tanggal} jam ${a.jamMulai}"
        val diff = now - cal.timeInMillis
        val prefs = applicationContext.getSharedPreferences("APP", Context.MODE_PRIVATE)
        val sentIds = prefs.getStringSet("SENT_NOTIF_IDS", mutableSetOf())?.toMutableSet() ?: mutableSetOf()
        val notifId = "assist_${a.id}_$prefix"

        if (sentIds.contains(notifId)) {
            Log.d("ScheduleWorker", "Notif $prefix untuk assist ${a.id} sudah dikirim sebelumnya → skip")
            return
        }

        if (cal.timeInMillis > now) {
            NotificationScheduler.scheduleNotification(
                applicationContext,
                title,
                message,
                cal.timeInMillis,
                a.id * 1000 + idOffset
            )
            Log.d("ScheduleWorker", "Notif $prefix dijadwalkan untuk assist ${a.id}")
        } else if (diff <= 5 * 60 * 1000) {
            NotificationScheduler.showNotification(applicationContext, title, message)
            Log.d("ScheduleWorker", "Notif $prefix langsung dikirim untuk assist ${a.id} (telat ≤5 menit)")
        } else {
            Log.d("ScheduleWorker", "Notif $prefix TIDAK dikirim untuk assist ${a.id} (telat >5 menit)")
            return
        }

        sentIds.add(notifId)
        prefs.edit().putStringSet("SENT_NOTIF_IDS", sentIds).apply()
    }

}
