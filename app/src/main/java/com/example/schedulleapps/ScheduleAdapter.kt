package com.example.schedulleapps
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.schedulleapps.databinding.ItemScheduleBinding
import com.example.schedulleapps.model.Schedule

class ScheduleAdapter(private val schedules: List<Schedule>) :
    RecyclerView.Adapter<ScheduleAdapter.ScheduleViewHolder>() {

    inner class ScheduleViewHolder(val binding: ItemScheduleBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleViewHolder {
        val binding = ItemScheduleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ScheduleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ScheduleViewHolder, position: Int) {
        val schedule = schedules[position]
        holder.binding.apply {
            tvNamaEvent.text = schedule.namaEvent
            tvTanggal.text = schedule.tanggal
            tvJam.text = "${schedule.jamMulai} - ${schedule.jamSelesai}"
        }
    }

    override fun getItemCount(): Int = schedules.size
}

