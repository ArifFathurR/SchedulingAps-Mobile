package com.example.schedulleapps.ui.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.schedulleapps.AssistAdapter
import com.example.schedulleapps.ScheduleAdapter
import com.example.schedulleapps.api.ApiClient
import com.example.schedulleapps.databinding.FragmentNotificationsBinding
import com.example.schedulleapps.model.Assist
import com.example.schedulleapps.model.AssistResponse
import com.example.schedulleapps.model.Schedule
import com.example.schedulleapps.model.ScheduleResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!

    private lateinit var scheduleAdapter: ScheduleAdapter
    private lateinit var assistAdapter: AssistAdapter

    private var allSchedules: List<Schedule> = emptyList()
    private var allAssist: List<Assist> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)

        // Setup RecyclerView Schedule
        binding.recyclerSchedules.layoutManager = LinearLayoutManager(requireContext())
        scheduleAdapter = ScheduleAdapter(requireContext(), emptyList())
        binding.recyclerSchedules.adapter = scheduleAdapter

        // Setup RecyclerView Assist
        binding.recyclerAssist.layoutManager = LinearLayoutManager(requireContext())
        assistAdapter = AssistAdapter(emptyList())
        binding.recyclerAssist.adapter = assistAdapter

        // SwipeRefresh
        binding.swipeRefresh.setOnRefreshListener { loadSchedules() }
        binding.swipeRefresh.setColorSchemeResources(
            android.R.color.holo_blue_bright,
            android.R.color.holo_green_light,
            android.R.color.holo_orange_light,
            android.R.color.holo_red_light
        )

        binding.swipeRefreshAssist.setOnRefreshListener { loadAssist() }
        binding.swipeRefreshAssist.setColorSchemeResources(
            android.R.color.holo_blue_bright,
            android.R.color.holo_green_light,
            android.R.color.holo_orange_light,
            android.R.color.holo_red_light
        )

        // Tab Switching
        binding.tabDaftarSchedule.setOnClickListener { showScheduleTab() }
        binding.tabScheduleAssist.setOnClickListener { showAssistTab() }

        // Load data awal
        loadSchedules()
        loadAssist()

        return binding.root
    }

    private fun loadSchedules() {
        val token = requireActivity().getSharedPreferences("APP", 0)
            .getString("TOKEN", null)

        if (token == null) {
            binding.swipeRefresh.isRefreshing = false
            Toast.makeText(requireContext(), "Token tidak ditemukan", Toast.LENGTH_SHORT).show()
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
                    } else {
                        Toast.makeText(requireContext(), "Gagal memuat data schedule", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ScheduleResponse>, t: Throwable) {
                    binding.swipeRefresh.isRefreshing = false
                    Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun loadAssist() {
        val token = requireActivity().getSharedPreferences("APP", 0)
            .getString("TOKEN", null)

        if (token == null) {
            binding.swipeRefreshAssist.isRefreshing = false
            Toast.makeText(requireContext(), "Token tidak ditemukan", Toast.LENGTH_SHORT).show()
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

    private fun showScheduleTab() {
        binding.swipeRefresh.visibility = View.VISIBLE
        binding.swipeRefreshAssist.visibility = View.GONE
        binding.tabDaftarSchedule.setBackgroundResource(com.example.schedulleapps.R.drawable.tab_selected_pill)
        binding.tabDaftarSchedule.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
        binding.tabScheduleAssist.setBackgroundResource(com.example.schedulleapps.R.drawable.tab_unselected_pill)
        binding.tabScheduleAssist.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.darker_gray))
    }

    private fun showAssistTab() {
        binding.swipeRefresh.visibility = View.GONE
        binding.swipeRefreshAssist.visibility = View.VISIBLE
        binding.tabScheduleAssist.setBackgroundResource(com.example.schedulleapps.R.drawable.tab_selected_pill)
        binding.tabScheduleAssist.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
        binding.tabDaftarSchedule.setBackgroundResource(com.example.schedulleapps.R.drawable.tab_unselected_pill)
        binding.tabDaftarSchedule.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.darker_gray))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
