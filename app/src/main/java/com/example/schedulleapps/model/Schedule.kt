package com.example.schedulleapps.model

data class Schedule(
    val id: Int,
    val tanggal: String,
    val jamMulai: String,
    val jamSelesai: String,
    val namaEvent: String,
    val fotografer_id: Int?,
    val editor_id: Int?,
    val catatan: String? = null,
    val linkGdriveFotografer: String? = null,
    val linkGdriveEditor: String? = null
)