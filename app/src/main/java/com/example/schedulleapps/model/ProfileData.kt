package com.example.schedulleapps.model

data class ProfileData(
    val id: Int,
    val role: String,
    val nama: String,
    val alamat: String?,
    val no_hp: String?,
    val email: String,
    val photo: String?
)