package com.example.schedulleapps

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.schedulleapps.api.ApiClient
import com.example.schedulleapps.databinding.ItemScheduleBinding
import com.example.schedulleapps.databinding.ActivityDetailScheduleBinding
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

            // klik item untuk update
            root.setOnClickListener {
                showUpdateDialog(schedule)
            }

            // klik btnDetail untuk lihat detail
            btnDetail.setOnClickListener {
                showDetailDialog(schedule)
            }
        }
    }

    override fun getItemCount(): Int = schedules.size

    fun updateData(newSchedules: List<Schedule>) {
        schedules = newSchedules
        notifyDataSetChanged()
    }

    /**
     * Menampilkan dialog update schedule
     */
    private fun showUpdateDialog(schedule: Schedule) {
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 40, 50, 10)
        }

        val inputCatatan = EditText(context).apply {
            hint = "Catatan"
            setText(schedule.catatan ?: "")
        }
        layout.addView(inputCatatan)

        val inputLink = EditText(context).apply {
            hint = "Link GDrive"
            setText(schedule.linkGdrive ?: "")
        }
        layout.addView(inputLink)

        val sharedPref = context.getSharedPreferences("APP", Context.MODE_PRIVATE)
        val token = sharedPref.getString("TOKEN", null)

        AlertDialog.Builder(context)
            .setTitle("Update Schedule")
            .setView(layout)
            .setPositiveButton("Simpan") { _, _ ->
                if (token == null) {
                    Toast.makeText(context, "Token tidak ditemukan", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val request = UpdateScheduleRequest(
                    catatan = inputCatatan.text.toString(),
                    linkGdrive = inputLink.text.toString()
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
                                val errorBody = response.errorBody()?.string()
                                android.util.Log.e(
                                    "UpdateSchedule",
                                    "Gagal update: code=${response.code()}, body=$errorBody"
                                )
                                Toast.makeText(
                                    context,
                                    "Gagal update: ${response.code()}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }

                        override fun onFailure(call: Call<UpdateScheduleResponse>, t: Throwable) {
                            android.util.Log.e("UpdateSchedule", "Error network: ${t.message}")
                            Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT)
                                .show()
                        }
                    })

            }
            .setNegativeButton("Batal", null)
            .show()
    }

    /**
     * Menampilkan popup detail (pakai layout activity_detail_schedule.xml)
     */
    private fun showDetailDialog(schedule: Schedule) {
        val detailBinding =
            ActivityDetailScheduleBinding.inflate(LayoutInflater.from(context))

        detailBinding.apply {
            tvNamaEventDetail.text = schedule.namaEvent
            tvTanggalDetail.text = schedule.tanggal
            tvJamDetail.text = "${schedule.jamMulai} - ${schedule.jamSelesai}"
            tvLapangan.text = schedule.lapangan?.nama_lapangan ?: "-"   // ✅ lapangan object
//            tvCatatan.text = schedule.catatan ?: "-"
//            tvLink.text = schedule.linkGdrive ?: "-"
        }

        val dialog = AlertDialog.Builder(context)
            .setView(detailBinding.root)
            .setCancelable(false)
            .create()

        detailBinding.btnClose.setOnClickListener { dialog.dismiss() }
        detailBinding.btnSelesai.setOnClickListener { dialog.dismiss() }

        dialog.show()
    }
}
