package com.example.schedulleapps

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.schedulleapps.api.ApiClient
import com.example.schedulleapps.databinding.ItemScheduleBinding
import com.example.schedulleapps.model.Schedule
import com.example.schedulleapps.model.UpdateScheduleRequest
import com.example.schedulleapps.model.UpdateScheduleResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ScheduleAdapter(
    private val context: Context,
    private var schedules: List<Schedule>
) : RecyclerView.Adapter<ScheduleAdapter.ScheduleViewHolder>() {

    inner class ScheduleViewHolder(val binding: ItemScheduleBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleViewHolder {
        val binding =
            ItemScheduleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ScheduleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ScheduleViewHolder, position: Int) {
        val schedule = schedules[position]
        holder.binding.apply {
            tvNamaEvent.text = schedule.namaEvent
            tvTanggal.text = schedule.tanggal
            tvJam.text = "${schedule.jamMulai} - ${schedule.jamSelesai}"
        }

        // Klik → buka dialog update
        holder.itemView.setOnClickListener {
            showUpdateDialog(schedule)
        }
    }

    override fun getItemCount(): Int = schedules.size

    fun updateData(newSchedules: List<Schedule>) {
        schedules = newSchedules
        notifyDataSetChanged()
    }

    private fun showUpdateDialog(schedule: Schedule) {
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 40, 50, 10)
        }

        val inputCatatan = EditText(context).apply {
            hint = "Catatan"
            setText(schedule.catatan ?: "")
        }
        val inputFotografer = EditText(context).apply {
            hint = "Link GDrive Fotografer"
            setText(schedule.linkGdriveFotografer ?: "")
        }
        val inputEditor = EditText(context).apply {
            hint = "Link GDrive Editor"
            setText(schedule.linkGdriveEditor ?: "")
        }

        layout.addView(inputCatatan)
        layout.addView(inputFotografer)
        layout.addView(inputEditor)

        AlertDialog.Builder(context)
            .setTitle("Update Schedule")
            .setView(layout)
            .setPositiveButton("Simpan") { _, _ ->
                val sharedPref = context.getSharedPreferences("APP", 0)
                val token = sharedPref.getString("TOKEN", null)

                if (token == null) {
                    Toast.makeText(context, "Token tidak ditemukan", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val request = UpdateScheduleRequest(
                    catatan = inputCatatan.text.toString(),
                    linkGdriveFotografer = inputFotografer.text.toString(),
                    linkGdriveEditor = inputEditor.text.toString()
                )

                ApiClient.instance.updateSchedule(schedule.id, "Bearer $token", request)
                    .enqueue(object : Callback<UpdateScheduleResponse> {
                        override fun onResponse(
                            call: Call<UpdateScheduleResponse>,
                            response: Response<UpdateScheduleResponse>
                        ) {
                            if (response.isSuccessful) {
                                Toast.makeText(context, "Update berhasil", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Gagal update", Toast.LENGTH_SHORT).show()
                            }
                        }

                        override fun onFailure(call: Call<UpdateScheduleResponse>, t: Throwable) {
                            Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                        }
                    })
            }
            .setNegativeButton("Batal", null)
            .show()
    }
}
