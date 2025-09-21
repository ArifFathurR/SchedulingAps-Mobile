package com.example.schedulleapps.model

data class ScheduleResponse(
    val status: String,
    val user: User,
    val data: List<Schedule>
)