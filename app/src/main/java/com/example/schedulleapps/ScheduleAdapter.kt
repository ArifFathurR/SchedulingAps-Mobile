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
     * Menampilkan dialog update (biarkan seperti kode lama)
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

        val sharedPref = context.getSharedPreferences("APP", Context.MODE_PRIVATE)
        val role = sharedPref.getString("ROLE", null)

        var inputFotografer: EditText? = null
        var inputEditor: EditText? = null

        when {
            role.equals("fotografer", ignoreCase = true) -> {
                inputFotografer = EditText(context).apply {
                    hint = "Link GDrive Fotografer"
                    setText(schedule.linkGdriveFotografer ?: "")
                }
                layout.addView(inputFotografer)
            }
            role.equals("editor", ignoreCase = true) -> {
                inputEditor = EditText(context).apply {
                    hint = "Link GDrive Editor"
                    setText(schedule.linkGdriveEditor ?: "")
                }
                layout.addView(inputEditor)
            }
        }

        AlertDialog.Builder(context)
            .setTitle("Update Schedule")
            .setView(layout)
            .setPositiveButton("Simpan") { _, _ ->
                val token = sharedPref.getString("TOKEN", null)

                if (token == null) {
                    Toast.makeText(context, "Token tidak ditemukan", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val request = UpdateScheduleRequest(
                    catatan = inputCatatan.text.toString(),
                    linkGdriveFotografer = inputFotografer?.text?.toString(),
                    linkGdriveEditor = inputEditor?.text?.toString()
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
//            tvDurasi.text = schedule.durasi ?: "-"
            tvLapangan.text = schedule.lapangan ?: "-"
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
