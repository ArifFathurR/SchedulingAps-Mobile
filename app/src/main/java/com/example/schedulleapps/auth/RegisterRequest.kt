package com.example.schedulleapps.auth

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    val password_confirmation: String,
    val role: String,
    val no_hp: String,
    val alamat: String? = null
)
