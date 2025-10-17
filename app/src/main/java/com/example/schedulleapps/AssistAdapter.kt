package com.example.schedulleapps

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.recyclerview.widget.RecyclerView
import com.example.schedulleapps.databinding.ActivityDetailScheduleAssistBinding
import com.example.schedulleapps.databinding.ItemScheduleAssistBinding
import com.example.schedulleapps.model.Assist

class AssistAdapter(
    private var assistList: List<Assist>
) : RecyclerView.Adapter<AssistAdapter.AssistViewHolder>() {

    class AssistViewHolder(val binding: ItemScheduleAssistBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AssistViewHolder {
        val binding = ItemScheduleAssistBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AssistViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AssistViewHolder, position: Int) {
        val assist = assistList[position]

        holder.binding.apply {
            tvNamaEvent.text = "Jadwal Sebagai Assist"
            tvJam.text = "${assist.jamMulai ?: "-"} - ${assist.jamSelesai ?: "-"}"
            tvTanggal.text = assist.tanggal ?: "-"

            btnDetail.setOnClickListener {
                showAssistDetailDialog(holder.itemView.context, assist)
            }
        }
    }

    override fun getItemCount(): Int = assistList.size

    fun updateData(newList: List<Assist>) {
        assistList = newList
        notifyDataSetChanged()
    }

    /** Fungsi untuk menampilkan popup detail */
    private fun showAssistDetailDialog(context: Context, assist: Assist) {
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val binding = ActivityDetailScheduleAssistBinding.inflate(LayoutInflater.from(context))
        dialog.setContentView(binding.root)
        dialog.setCancelable(true)

        // Isi data
        binding.tvTanggalDetail.text = assist.tanggal ?: "-"
        binding.tvJamDetail.text = "${assist.jamMulai ?: "-"} - ${assist.jamSelesai ?: "-"}"

        // Sembunyikan Durasi dan tombol Selesai karena tidak dipakai

        // Tombol close
        binding.btnClose.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}
