package com.example.schedulleapps.model

data class Schedule(
    val id: Int,
    val tanggal: String,
    val jamMulai: String,
    val jamSelesai: String,
    val namaEvent: String,
    val fotografer_id: Int?,
    val editor_id: Int?,
    val lapangan: Lapangan?,     // ✅ object, bukan String
    val catatan: String?,        // ✅ string, bukan Lapangan
    val linkGdrive: String?      // ✅ satu field saja
)
