package com.example.schedulleapps

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.schedulleapps.databinding.ItemScheduleAssistBinding
import com.example.schedulleapps.model.Assist

class AssistAdapter(
    private var assistList: List<Assist>,
    private val onDetailClick: (Assist) -> Unit
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
                onDetailClick(assist)
            }
        }
    }

    override fun getItemCount(): Int = assistList.size

    fun updateData(newList: List<Assist>) {
        assistList = newList
        notifyDataSetChanged()
    }
}
