package com.example.schedulleapps
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.schedulleapps.auth.LoginActivity
import com.example.schedulleapps.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup Navigation dengan BottomNavigationView
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        binding.navView.setupWithNavController(navController)

        // Logout button
        binding.btnLogout.setOnClickListener {
            // Hapus token auth dari SharedPreferences
            val prefs = getSharedPreferences("APP", MODE_PRIVATE)
            prefs.edit().clear().apply()

            // Arahkan ke LoginActivity
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}
