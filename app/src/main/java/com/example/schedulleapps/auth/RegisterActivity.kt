package com.example.schedulleapps.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.schedulleapps.api.ApiClient
import com.example.schedulleapps.auth.RegisterRequest
import com.example.schedulleapps.auth.RegisterResponse
import com.example.schedulleapps.databinding.ActivityRegisterBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnRegister.setOnClickListener {
            val name = binding.etName.text.toString()
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()
            val confirmPassword = binding.etConfirmPassword.text.toString()
            val role = binding.etRole.text.toString()
            val noHp = binding.etNoHp.text.toString()
            val alamat = binding.etAlamat.text.toString()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || role.isEmpty() || noHp.isEmpty()) {
                Toast.makeText(this, "Semua field wajib diisi!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Password tidak sama!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val request = RegisterRequest(
                name = name,
                email = email,
                password = password,
                password_confirmation = confirmPassword,
                role = role,
                no_hp = noHp,
                alamat = alamat
            )

            ApiClient.instance.register(request).enqueue(object : Callback<RegisterResponse> {
                override fun onResponse(call: Call<RegisterResponse>, response: Response<RegisterResponse>) {
                    if (response.isSuccessful) {
                        val body = response.body()
                        Toast.makeText(this@RegisterActivity, "Registrasi sukses: ${body?.user?.name}", Toast.LENGTH_SHORT).show()

                        // Simpan token ke SharedPreferences
                        val sharedPref = getSharedPreferences("APP", MODE_PRIVATE)
                        sharedPref.edit().putString("TOKEN", body?.token).apply()

                        // Arahkan ke login / home
                        startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this@RegisterActivity, "Registrasi gagal: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                    Toast.makeText(this@RegisterActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}