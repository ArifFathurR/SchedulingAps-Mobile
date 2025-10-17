package com.example.schedulleapps.auth

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.schedulleapps.R
import com.example.schedulleapps.api.ApiClient
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

        // Setup spinner role
        setupRoleSpinner()

        // Tombol register
        binding.btnRegister.setOnClickListener {
            handleRegister()
        }

        // Link login
        binding.tvLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun setupRoleSpinner() {
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.role_options,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerRole.adapter = adapter
    }

    private fun handleRegister() {
        val name = binding.etName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val noHp = binding.etNoHp.text.toString().trim()
        val alamat = binding.etAlamat.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val confirmPassword = binding.etConfirmPassword.text.toString().trim()
        val selectedRole = binding.spinnerRole.selectedItem.toString()

        if (!validateInput(name, email, noHp, alamat, password, confirmPassword, selectedRole)) return

        val request = RegisterRequest(
            name = name,
            email = email,
            no_hp = noHp,
            alamat = alamat,
            password = password,
            password_confirmation = confirmPassword,
            role = selectedRole
        )

        performRegister(request)
    }

    private fun validateInput(
        name: String,
        email: String,
        noHp: String,
        alamat: String,
        password: String,
        confirmPassword: String,
        role: String
    ): Boolean {
        if (name.isEmpty()) {
            binding.etName.error = "Nama tidak boleh kosong"
            binding.etName.requestFocus()
            return false
        }
        if (email.isEmpty()) {
            binding.etEmail.error = "Email tidak boleh kosong"
            binding.etEmail.requestFocus()
            return false
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.error = "Email tidak valid"
            binding.etEmail.requestFocus()
            return false
        }
        if (noHp.isEmpty()) {
            binding.etNoHp.error = "Nomor HP tidak boleh kosong"
            binding.etNoHp.requestFocus()
            return false
        }
        if (alamat.isEmpty()) {
            binding.etAlamat.error = "Alamat tidak boleh kosong"
            binding.etAlamat.requestFocus()
            return false
        }
        if (password.isEmpty()) {
            binding.etPassword.error = "Password tidak boleh kosong"
            binding.etPassword.requestFocus()
            return false
        }
        if (password.length < 6) {
            binding.etPassword.error = "Password minimal 6 karakter"
            binding.etPassword.requestFocus()
            return false
        }
        if (confirmPassword.isEmpty()) {
            binding.etConfirmPassword.error = "Konfirmasi password tidak boleh kosong"
            binding.etConfirmPassword.requestFocus()
            return false
        }
        if (password != confirmPassword) {
            binding.etConfirmPassword.error = "Password tidak sama"
            binding.etConfirmPassword.requestFocus()
            Toast.makeText(this, "Password tidak sama!", Toast.LENGTH_SHORT).show()
            return false
        }
        if (role == "Pilih Role") {
            Toast.makeText(this, "Silakan pilih role!", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun performRegister(request: RegisterRequest) {
        binding.btnRegister.isEnabled = false
        binding.btnRegister.text = "Mendaftar..."

        ApiClient.instance.register(request).enqueue(object : Callback<RegisterResponse> {
            override fun onResponse(call: Call<RegisterResponse>, response: Response<RegisterResponse>) {
                binding.btnRegister.isEnabled = true
                binding.btnRegister.text = "Daftar Sekarang"

                if (response.isSuccessful) {
                    val body = response.body()
                    Toast.makeText(
                        this@RegisterActivity,
                        "Registrasi berhasil! Selamat datang ${body?.user?.name}",
                        Toast.LENGTH_SHORT
                    ).show()

                    // Simpan token
                    val sharedPref = getSharedPreferences("APP", MODE_PRIVATE)
                    sharedPref.edit().putString("TOKEN", body?.token).apply()

                    startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
                    finish()
                } else {
                    val errorMessage = when (response.code()) {
                        400 -> "Data tidak valid"
                        409 -> "Email sudah terdaftar"
                        422 -> "Data tidak lengkap"
                        500 -> "Terjadi kesalahan server"
                        else -> "Registrasi gagal (${response.code()})"
                    }
                    Toast.makeText(this@RegisterActivity, errorMessage, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                binding.btnRegister.isEnabled = true
                binding.btnRegister.text = "Daftar Sekarang"
                Toast.makeText(this@RegisterActivity, "Koneksi bermasalah: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
