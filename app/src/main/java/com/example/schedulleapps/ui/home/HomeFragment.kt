package com.example.schedulleapps.ui.home

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.schedulleapps.AssistAdapter
import com.example.schedulleapps.R
import com.example.schedulleapps.ScheduleAdapter
import com.example.schedulleapps.api.ApiClient
import com.example.schedulleapps.databinding.FragmentHomeBinding
import com.example.schedulleapps.model.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.MonthScrollListener
import com.kizitonwose.calendar.view.ViewContainer
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private var scheduleAdapter: ScheduleAdapter? = null
    private var assistAdapter: AssistAdapter? = null

    private var allSchedules: List<Schedule> = emptyList()
    private var allAssist: List<Assist> = emptyList()

    private var scheduleDates: Set<LocalDate> = emptySet()
    private var assistDates: Set<LocalDate> = emptySet()

    private var isScheduleTabActive = true
    private val gson = Gson()

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val monthYearFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale("id", "ID"))

    private var currentMonth = YearMonth.now()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        setupRecyclerViews()
        setupTabs()
        setupSwipeRefresh()
        setupCalendar()
        setupMonthNavigation()

        // Load data cache
        loadSchedulesFromPrefs()
        loadAssistFromPrefs()

        // Load API data
        loadSchedulesFromApi()
        loadAssistFromApi()

        // ✅ Load total jam fotografer dari API baru
        loadTotalJamFotograferFromApi()

        return binding.root
    }

    private fun setupRecyclerViews() {
        binding.recyclerSchedules.layoutManager = LinearLayoutManager(requireContext())
        scheduleAdapter = ScheduleAdapter(requireContext(), emptyList())
        binding.recyclerSchedules.adapter = scheduleAdapter

        binding.recyclerAssist.layoutManager = LinearLayoutManager(requireContext())
        assistAdapter = AssistAdapter(emptyList())
        binding.recyclerAssist.adapter = assistAdapter
    }

    private fun setupTabs() {
        binding.tabDaftarSchedule.setOnClickListener { showScheduleTab() }
        binding.tabScheduleAssist.setOnClickListener { showAssistTab() }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            loadSchedulesFromApi()
            loadTotalJamFotograferFromApi()
        }
        binding.swipeRefreshAssist.setOnRefreshListener {
            loadAssistFromApi()
        }
    }

    private fun setupCalendar() {
        val startMonth = currentMonth.minusMonths(24)
        val endMonth = currentMonth.plusMonths(24)
        val daysOfWeek = daysOfWeek()

        binding.kalenderKeg.setup(startMonth, endMonth, daysOfWeek.first())
        binding.kalenderKeg.scrollToMonth(currentMonth)
        updateMonthYearText(currentMonth)

        binding.kalenderKeg.monthScrollListener = object : MonthScrollListener {
            override fun invoke(calendarMonth: com.kizitonwose.calendar.core.CalendarMonth) {
                currentMonth = YearMonth.from(calendarMonth.yearMonth)
                updateMonthYearText(currentMonth)
            }
        }

        binding.kalenderKeg.dayBinder = object : MonthDayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)

            override fun bind(container: DayViewContainer, data: CalendarDay) {
                container.day = data
                val textView = container.textView
                val dotView = container.dotView
                textView.text = data.date.dayOfMonth.toString()

                if (data.position == DayPosition.MonthDate) {
                    textView.setTextColor(Color.BLACK)

                    val hasEvent = if (isScheduleTabActive) {
                        scheduleDates.contains(data.date)
                    } else {
                        assistDates.contains(data.date)
                    }

                    dotView.visibility = if (hasEvent) View.VISIBLE else View.GONE

                    if (hasEvent) {
                        val dotColor = if (isScheduleTabActive) {
                            Color.parseColor("#4CAF50")
                        } else {
                            Color.parseColor("#2196F3")
                        }
                        dotView.setBackgroundColor(dotColor)
                    }

                    if (data.date == LocalDate.now()) {
                        textView.setTextColor(Color.parseColor("#4CAF50"))
                        textView.setTypeface(null, android.graphics.Typeface.BOLD)
                    } else {
                        textView.setTypeface(null, android.graphics.Typeface.NORMAL)
                    }
                } else {
                    textView.setTextColor(Color.GRAY)
                    dotView.visibility = View.GONE
                }

                container.view.setOnClickListener {
                    if (data.position == DayPosition.MonthDate) {
                        val selectedDate = data.date.format(dateFormatter)
                        if (isScheduleTabActive) {
                            filterSchedulesByDate(selectedDate)
                        } else {
                            filterAssistByDate(selectedDate)
                        }
                    }
                }
            }
        }
    }

    private fun setupMonthNavigation() {
        binding.btnPreviousMonth.setOnClickListener {
            currentMonth = currentMonth.minusMonths(1)
            binding.kalenderKeg.smoothScrollToMonth(currentMonth)
            updateMonthYearText(currentMonth)
        }

        binding.btnNextMonth.setOnClickListener {
            currentMonth = currentMonth.plusMonths(1)
            binding.kalenderKeg.smoothScrollToMonth(currentMonth)
            updateMonthYearText(currentMonth)
        }

        binding.tvMonthYear.setOnClickListener {
            currentMonth = YearMonth.now()
            binding.kalenderKeg.smoothScrollToMonth(currentMonth)
            updateMonthYearText(currentMonth)
            Toast.makeText(requireContext(), "Kembali ke bulan ini", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateMonthYearText(yearMonth: YearMonth) {
        val monthYear = yearMonth.format(monthYearFormatter)
        binding.tvMonthYear.text = monthYear.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase() else it.toString()
        }
    }

    private fun updateCalendarMarkers() {
        binding.kalenderKeg.notifyCalendarChanged()
    }

    private fun parseToLocalDate(dateString: String): LocalDate? {
        return try {
            LocalDate.parse(dateString, dateFormatter)
        } catch (e: Exception) {
            null
        }
    }

    // ================== API: TOTAL JAM FOTOGRAFER ==================
    private fun loadTotalJamFotograferFromApi() {
        val token = requireActivity()
            .getSharedPreferences("APP", 0)
            .getString("TOKEN", null)

        if (token.isNullOrEmpty()) {
            android.util.Log.e("HomeFragment", "Token kosong")
            return
        }

        ApiClient.instance.getTotalJamFotografer("Bearer $token")
            .enqueue(object : Callback<TotalJamFotograferResponse> {
                override fun onResponse(
                    call: Call<TotalJamFotograferResponse>,
                    response: Response<TotalJamFotograferResponse>
                ) {
                    if (response.isSuccessful) {
                        val result = response.body()
                        if (result != null) {
                            android.util.Log.d(
                                "HomeFragment",
                                "Total jam fotografer (${result.fotografer}): ${result.total_jam_fotografer}"
                            )
                            binding.tvTotalJamFotografer.text =
                                result.total_jam_fotografer.toString()
                            binding.tvTotalMatchAssist.text = allAssist.size.toString()
                        } else {
                            binding.tvTotalJamFotografer.text = "0"
                            Toast.makeText(requireContext(), "Data kosong", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(requireContext(), "Gagal memuat total jam fotografer", Toast.LENGTH_SHORT).show()
                        binding.tvTotalJamFotografer.text = "0"
                    }
                }

                override fun onFailure(call: Call<TotalJamFotograferResponse>, t: Throwable) {
                    Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                    binding.tvTotalJamFotografer.text = "0"
                }
            })
    }

    // ================== CACHE ==================
    private fun loadSchedulesFromPrefs() {
        try {
            val prefs = requireActivity().getSharedPreferences("APP", 0)
            val json = prefs.getString("CACHE_SCHEDULES", null)
            if (!json.isNullOrEmpty()) {
                val type = object : TypeToken<List<Schedule>>() {}.type
                allSchedules = gson.fromJson(json, type) ?: emptyList()
                scheduleAdapter?.updateData(allSchedules)

                scheduleDates = allSchedules.mapNotNull { s ->
                    s.tanggal?.let { parseToLocalDate(it) }
                }.toSet()

                updateCalendarMarkers()
            }
        } catch (e: Exception) {
            allSchedules = emptyList()
        }
    }

    private fun loadAssistFromPrefs() {
        try {
            val prefs = requireActivity().getSharedPreferences("APP", 0)
            val json = prefs.getString("CACHE_ASSISTS", null)
            if (!json.isNullOrEmpty()) {
                val type = object : TypeToken<List<Assist>>() {}.type
                allAssist = gson.fromJson(json, type) ?: emptyList()
                assistAdapter?.updateData(allAssist)

                assistDates = allAssist.mapNotNull { a ->
                    a.tanggal?.let { parseToLocalDate(it) }
                }.toSet()

                if (!isScheduleTabActive) {
                    updateCalendarMarkers()
                }
            }
        } catch (e: Exception) {
            allAssist = emptyList()
        }
    }

    // ================== API DATA ==================
    private fun loadSchedulesFromApi() {
        val token = requireActivity().getSharedPreferences("APP", 0).getString("TOKEN", null)
        if (token.isNullOrEmpty()) return

        binding.swipeRefresh.isRefreshing = true
        ApiClient.instance.getSchedules("Bearer $token")
            .enqueue(object : Callback<ScheduleResponse> {
                override fun onResponse(
                    call: Call<ScheduleResponse>,
                    response: Response<ScheduleResponse>
                ) {
                    binding.swipeRefresh.isRefreshing = false
                    if (response.isSuccessful) {
                        allSchedules = response.body()?.data ?: emptyList()
                        scheduleAdapter?.updateData(allSchedules)

                        scheduleDates = allSchedules.mapNotNull { it.tanggal?.let { d -> parseToLocalDate(d) } }.toSet()
                        updateCalendarMarkers()

                        val prefs = requireActivity().getSharedPreferences("APP", 0)
                        prefs.edit().putString("CACHE_SCHEDULES", gson.toJson(allSchedules)).apply()
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

    private fun loadAssistFromApi() {
        val token = requireActivity().getSharedPreferences("APP", 0).getString("TOKEN", null)
        if (token.isNullOrEmpty()) return

        binding.swipeRefreshAssist.isRefreshing = true
        ApiClient.instance.getAssists("Bearer $token")
            .enqueue(object : Callback<AssistResponse> {
                override fun onResponse(
                    call: Call<AssistResponse>,
                    response: Response<AssistResponse>
                ) {
                    binding.swipeRefreshAssist.isRefreshing = false
                    if (response.isSuccessful) {
                        allAssist = response.body()?.data ?: emptyList()
                        assistAdapter?.updateData(allAssist)

                        assistDates = allAssist.mapNotNull { it.tanggal?.let { d -> parseToLocalDate(d) } }.toSet()
                        if (!isScheduleTabActive) updateCalendarMarkers()

                        val prefs = requireActivity().getSharedPreferences("APP", 0)
                        prefs.edit().putString("CACHE_ASSISTS", gson.toJson(allAssist)).apply()
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

    // ================== FILTER ==================
    private fun filterSchedulesByDate(date: String) {
        val filtered = allSchedules.filter { it.tanggal == date }
        Toast.makeText(requireContext(), "${filtered.size} schedule ditemukan", Toast.LENGTH_SHORT).show()
        scheduleAdapter?.updateData(filtered)
    }

    private fun filterAssistByDate(date: String) {
        val filtered = allAssist.filter { it.tanggal == date }
        Toast.makeText(requireContext(), "${filtered.size} assist ditemukan", Toast.LENGTH_SHORT).show()
        assistAdapter?.updateData(filtered)
    }

    // ================== TAB ==================
    private fun showScheduleTab() {
        isScheduleTabActive = true
        binding.swipeRefresh.visibility = View.VISIBLE
        binding.swipeRefreshAssist.visibility = View.GONE

        binding.tabDaftarSchedule.setBackgroundResource(R.drawable.tab_selected_pill)
        binding.tabDaftarSchedule.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
        binding.tabScheduleAssist.setBackgroundResource(R.drawable.tab_unselected_pill)
        binding.tabScheduleAssist.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.darker_gray))

        scheduleAdapter?.updateData(allSchedules)
        updateCalendarMarkers()
    }

    private fun showAssistTab() {
        isScheduleTabActive = false
        binding.swipeRefresh.visibility = View.GONE
        binding.swipeRefreshAssist.visibility = View.VISIBLE

        binding.tabScheduleAssist.setBackgroundResource(R.drawable.tab_selected_pill)
        binding.tabScheduleAssist.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
        binding.tabDaftarSchedule.setBackgroundResource(R.drawable.tab_unselected_pill)
        binding.tabDaftarSchedule.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.darker_gray))

        assistAdapter?.updateData(allAssist)
        updateCalendarMarkers()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    inner class DayViewContainer(view: View) : ViewContainer(view) {
        val textView: TextView = view.findViewById(R.id.calendarDayText)
        val dotView: View = view.findViewById(R.id.calendarDayDot)
        lateinit var day: CalendarDay
    }
}
