package com.example.schedulleapps.model

data class Assist(
    val id: Int,
    val tanggal: String?,
    val jamMulai: String?,
    val jamSelesai: String?,
    val assistable_type: String,
    val assistable: Assistable?   // relasi ke Fotografer/Editor
)
