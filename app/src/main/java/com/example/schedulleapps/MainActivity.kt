package com.example.schedulleapps

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.bumptech.glide.Glide
import com.example.schedulleapps.api.ApiClient
import com.example.schedulleapps.auth.LoginActivity
import com.example.schedulleapps.databinding.ActivityMainBinding
import com.example.schedulleapps.model.ProfileResponse
import com.example.schedulleapps.worker.ScheduleWorker
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val REQUEST_CODE_NOTIFICATION = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        binding.navView.setupWithNavController(navController)

        // Logout
        binding.btnLogout.setOnClickListener {
            val prefs = getSharedPreferences("APP", MODE_PRIVATE)
            prefs.edit().clear().apply()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        // Izin notifikasi
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    REQUEST_CODE_NOTIFICATION
                )
            } else startWorker()
        } else startWorker()

        loadProfile()
    }

    private fun startWorker() {
        val workRequest = PeriodicWorkRequestBuilder<ScheduleWorker>(1, TimeUnit.HOURS).build()
        WorkManager.getInstance(this).enqueue(workRequest)
    }

    private fun loadProfile() {
        val prefs = getSharedPreferences("APP", MODE_PRIVATE)
        val token = prefs.getString("TOKEN", null)
        if (token.isNullOrEmpty()) {
            Toast.makeText(this, "User belum login!", Toast.LENGTH_SHORT).show()
            return
        }

        ApiClient.instance.getProfile("Bearer $token")
            .enqueue(object : Callback<ProfileResponse> {
                override fun onResponse(
                    call: Call<ProfileResponse>,
                    response: Response<ProfileResponse>
                ) {
                    if (response.isSuccessful) {
                        val profile = response.body()?.data
                        if (profile != null) {
                            binding.etNama.text = profile.nama
                            binding.role.text = profile.role
                            if (!profile.photo.isNullOrEmpty()) {
                                Glide.with(this@MainActivity)
                                    .load(profile.photo)
                                    .into(binding.imgProfile)
                            }
                        }
                    } else {
                        Toast.makeText(this@MainActivity, "Gagal memuat profil", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ProfileResponse>, t: Throwable) {
                    Toast.makeText(this@MainActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }
}
