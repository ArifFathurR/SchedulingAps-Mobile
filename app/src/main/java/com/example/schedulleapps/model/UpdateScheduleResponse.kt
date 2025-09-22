package com.example.schedulleapps.model

data class UpdateScheduleResponse(
    val status: String,
    val message: String,
    val data: Schedule?
)
