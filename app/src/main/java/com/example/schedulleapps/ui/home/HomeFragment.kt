package com.example.schedulleapps.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.schedulleapps.AssistAdapter
import com.example.schedulleapps.ScheduleAdapter
import com.example.schedulleapps.api.ApiClient
import com.example.schedulleapps.databinding.FragmentHomeBinding
import com.example.schedulleapps.model.Assist
import com.example.schedulleapps.model.AssistResponse
import com.example.schedulleapps.model.Schedule
import com.example.schedulleapps.model.ScheduleResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var scheduleAdapter: ScheduleAdapter
    private lateinit var assistAdapter: AssistAdapter

    private var allSchedules: List<Schedule> = emptyList()
    private var allAssist: List<Assist> = emptyList()
    private var scheduleDates: Set<String> = emptySet()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        // --- Setup RecyclerView Schedule ---
        binding.recyclerSchedules.layoutManager = LinearLayoutManager(requireContext())
        scheduleAdapter = ScheduleAdapter(requireContext(), emptyList())
        binding.recyclerSchedules.adapter = scheduleAdapter

        // --- Setup RecyclerView Assist ---
        binding.recyclerAssist.layoutManager = LinearLayoutManager(requireContext())
        assistAdapter = AssistAdapter(emptyList()) { assist ->
            Toast.makeText(requireContext(), "Detail: ${assist.tanggal}", Toast.LENGTH_SHORT).show()
        }
        binding.recyclerAssist.adapter = assistAdapter

        // --- Swipe Refresh Jadwal ---
        binding.swipeRefresh.setOnRefreshListener {
            loadSchedules()
        }
        binding.swipeRefresh.setColorSchemeResources(
            android.R.color.holo_blue_bright,
            android.R.color.holo_green_light,
            android.R.color.holo_orange_light,
            android.R.color.holo_red_light
        )

        // --- Swipe Refresh Assist ---
        binding.swipeRefreshAssist.setOnRefreshListener {
            loadAssist()
        }
        binding.swipeRefreshAssist.setColorSchemeResources(
            android.R.color.holo_blue_bright,
            android.R.color.holo_green_light,
            android.R.color.holo_orange_light,
            android.R.color.holo_red_light
        )

        // --- Tab Switching ---
        binding.tabDaftarSchedule.setOnClickListener {
            showScheduleTab()
        }

        binding.tabScheduleAssist.setOnClickListener {
            showAssistTab()
        }

        // --- CalendarView Listener ---
        binding.kalenderKeg.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val selectedDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
            filterSchedulesByDate(selectedDate)
            if (scheduleDates.contains(selectedDate)) {
                Toast.makeText(requireContext(), "Ada schedule di tanggal ini", Toast.LENGTH_SHORT).show()
            }
        }

        // --- Load Data pertama kali ---
        loadSchedules()
        loadAssist()

        return binding.root
    }

    private fun loadSchedules() {
        val sharedPref = requireActivity().getSharedPreferences("APP", 0)
        val token = sharedPref.getString("TOKEN", null)

        if (token == null) {
            Toast.makeText(requireContext(), "Token tidak ditemukan", Toast.LENGTH_SHORT).show()
            binding.swipeRefresh.isRefreshing = false
            return
        }

        ApiClient.instance.getSchedules("Bearer $token")
            .enqueue(object : Callback<ScheduleResponse> {
                override fun onResponse(
                    call: Call<ScheduleResponse>,
                    response: Response<ScheduleResponse>
                ) {
                    binding.swipeRefresh.isRefreshing = false
                    if (response.isSuccessful) {
                        allSchedules = response.body()?.data ?: emptyList()
                        scheduleAdapter.updateData(allSchedules)
                        scheduleDates = allSchedules.map { it.tanggal }.toSet()
                    } else {
                        Toast.makeText(requireContext(), "Gagal memuat data jadwal", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ScheduleResponse>, t: Throwable) {
                    binding.swipeRefresh.isRefreshing = false
                    Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun loadAssist() {
        val sharedPref = requireActivity().getSharedPreferences("APP", 0)
        val token = sharedPref.getString("TOKEN", null)

        if (token == null) {
            Toast.makeText(requireContext(), "Token tidak ditemukan", Toast.LENGTH_SHORT).show()
            binding.swipeRefreshAssist.isRefreshing = false
            return
        }

        ApiClient.instance.getAssists("Bearer $token")
            .enqueue(object : Callback<AssistResponse> {
                override fun onResponse(
                    call: Call<AssistResponse>,
                    response: Response<AssistResponse>
                ) {
                    binding.swipeRefreshAssist.isRefreshing = false
                    if (response.isSuccessful) {
                        allAssist = response.body()?.data ?: emptyList()
                        assistAdapter.updateData(allAssist)
                    } else {
                        Toast.makeText(requireContext(), "Gagal memuat data assist", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<AssistResponse>, t: Throwable) {
                    binding.swipeRefreshAssist.isRefreshing = false
                    Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun filterSchedulesByDate(date: String) {
        val filtered = allSchedules.filter { it.tanggal == date }
        if (filtered.isEmpty()) {
            Toast.makeText(requireContext(), "Tidak ada schedule di tanggal ini", Toast.LENGTH_SHORT).show()
        }
        scheduleAdapter.updateData(filtered)
    }

    private fun showScheduleTab() {
        binding.swipeRefresh.visibility = View.VISIBLE
        binding.swipeRefreshAssist.visibility = View.GONE

        binding.tabDaftarSchedule.setBackgroundResource(com.example.schedulleapps.R.drawable.tab_selected_pill)
        binding.tabDaftarSchedule.setTextColor(resources.getColor(android.R.color.white))

        binding.tabScheduleAssist.setBackgroundResource(com.example.schedulleapps.R.drawable.tab_unselected_pill)
        binding.tabScheduleAssist.setTextColor(resources.getColor(android.R.color.darker_gray))
    }

    private fun showAssistTab() {
        binding.swipeRefresh.visibility = View.GONE
        binding.swipeRefreshAssist.visibility = View.VISIBLE

        binding.tabScheduleAssist.setBackgroundResource(com.example.schedulleapps.R.drawable.tab_selected_pill)
        binding.tabScheduleAssist.setTextColor(resources.getColor(android.R.color.white))

        binding.tabDaftarSchedule.setBackgroundResource(com.example.schedulleapps.R.drawable.tab_unselected_pill)
        binding.tabDaftarSchedule.setTextColor(resources.getColor(android.R.color.darker_gray))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
