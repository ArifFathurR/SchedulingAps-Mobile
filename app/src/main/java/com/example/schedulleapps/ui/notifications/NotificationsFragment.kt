package com.example.schedulleapps.ui.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.schedulleapps.ScheduleAdapter
import com.example.schedulleapps.api.ApiClient
import com.example.schedulleapps.databinding.FragmentNotificationsBinding
import com.example.schedulleapps.model.Schedule
import com.example.schedulleapps.model.ScheduleResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!

    private var allSchedules: List<Schedule> = emptyList()
    private lateinit var adapter: ScheduleAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)

        // Setup RecyclerView
        binding.recyclerSchedules.layoutManager = LinearLayoutManager(requireContext())
        adapter = ScheduleAdapter(requireContext(), emptyList())
        binding.recyclerSchedules.adapter = adapter

        // Load data pertama kali
        loadSchedules()

        // SwipeRefresh untuk tarik refresh
        binding.swipeRefresh.setOnRefreshListener {
            loadSchedules()
        }
        binding.swipeRefresh.setColorSchemeResources(
            android.R.color.holo_blue_bright,
            android.R.color.holo_green_light,
            android.R.color.holo_orange_light,
            android.R.color.holo_red_light
        )

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
                        adapter.updateData(allSchedules)
                    } else {
                        Toast.makeText(requireContext(), "Gagal memuat data", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ScheduleResponse>, t: Throwable) {
                    binding.swipeRefresh.isRefreshing = false
                    Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
