package com.example.schedulleapps.model

data class Schedule(
    val id: Int,
    val tanggal: String,
    val jamMulai: String,
    val jamSelesai: String,
    val namaEvent: String,
    val fotografer_id: Int?,
    val editor_id: Int?
)