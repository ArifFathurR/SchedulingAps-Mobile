package com.example.schedulleapps.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.schedulleapps.ScheduleAdapter
import com.example.schedulleapps.api.ApiClient
import com.example.schedulleapps.databinding.FragmentHomeBinding
import com.example.schedulleapps.model.Schedule
import com.example.schedulleapps.model.ScheduleResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private var allSchedules: List<Schedule> = emptyList()
    private lateinit var adapter: ScheduleAdapter
    private var scheduleDates: Set<String> = emptySet()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        binding.recyclerSchedules.layoutManager = LinearLayoutManager(requireContext())
        adapter = ScheduleAdapter(requireContext(), emptyList())
        binding.recyclerSchedules.adapter = adapter

        loadSchedules()

        binding.kalenderKeg.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val selectedDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
            filterSchedulesByDate(selectedDate)
            if (scheduleDates.contains(selectedDate)) {
                Toast.makeText(requireContext(), "Ada schedule di tanggal ini", Toast.LENGTH_SHORT).show()
            }
        }

        binding.tx1.setOnClickListener { showAllSchedules() }

        return binding.root
    }

    private fun loadSchedules() {
        val sharedPref = requireActivity().getSharedPreferences("APP", 0)
        val token = sharedPref.getString("TOKEN", null)

        if (token == null) {
            Toast.makeText(requireContext(), "Token tidak ditemukan", Toast.LENGTH_SHORT).show()
            return
        }

        ApiClient.instance.getSchedules("Bearer $token")
            .enqueue(object : Callback<ScheduleResponse> {
                override fun onResponse(
                    call: Call<ScheduleResponse>,
                    response: Response<ScheduleResponse>
                ) {
                    if (response.isSuccessful) {
                        allSchedules = response.body()?.data ?: emptyList()
                        adapter.updateData(allSchedules)
                        scheduleDates = allSchedules.map { it.tanggal }.toSet()
                    } else {
                        Toast.makeText(requireContext(), "Gagal memuat data", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ScheduleResponse>, t: Throwable) {
                    Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun filterSchedulesByDate(date: String) {
        val filtered = allSchedules.filter { it.tanggal == date }
        if (filtered.isEmpty()) {
            Toast.makeText(requireContext(), "Tidak ada schedule di tanggal ini", Toast.LENGTH_SHORT).show()
        }
        adapter.updateData(filtered)
    }

    private fun showAllSchedules() {
        adapter.updateData(allSchedules)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
