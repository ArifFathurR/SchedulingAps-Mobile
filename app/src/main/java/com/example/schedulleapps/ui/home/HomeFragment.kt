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
    private var scheduleDates: Set<String> = emptySet() // tanggal yang ada schedule

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        // Setup RecyclerView
        binding.recyclerSchedules.layoutManager = LinearLayoutManager(requireContext())
        adapter = ScheduleAdapter(emptyList())
        binding.recyclerSchedules.adapter = adapter

        // Load data dari API
        loadSchedules()

        // Listener kalender untuk filter tanggal
        binding.kalenderKeg.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val selectedDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
            filterSchedulesByDate(selectedDate)
            if (scheduleDates.contains(selectedDate)) {
                if (isAdded && context != null) {
                    Toast.makeText(requireContext(), "Ada schedule di tanggal ini", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Listener tombol tx1 untuk menampilkan semua schedule
        binding.tx1.setOnClickListener {
            showAllSchedules()
        }

        return binding.root
    }

    // Fungsi load schedule dari API
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
                        adapter = ScheduleAdapter(allSchedules)
                        binding.recyclerSchedules.adapter = adapter

                        // Simpan tanggal yang ada schedule
                        scheduleDates = allSchedules.map { it.tanggal }.toSet()
                    } else if (isAdded && context != null) {
                        Toast.makeText(requireContext(), "Gagal memuat data", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ScheduleResponse>, t: Throwable) {
                    if (isAdded && context != null) {
                        Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            })
    }

    // Fungsi filter schedule berdasarkan tanggal
    private fun filterSchedulesByDate(date: String) {
        val filtered = allSchedules.filter { it.tanggal == date }
        if (filtered.isEmpty()) {
            if (isAdded && context != null) {
                Toast.makeText(requireContext(), "Tidak ada schedule di tanggal ini", Toast.LENGTH_SHORT).show()
            }
        }
        adapter = ScheduleAdapter(filtered)
        binding.recyclerSchedules.adapter = adapter
    }

    // Fungsi menampilkan semua schedule
    private fun showAllSchedules() {
        adapter = ScheduleAdapter(allSchedules)
        binding.recyclerSchedules.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
