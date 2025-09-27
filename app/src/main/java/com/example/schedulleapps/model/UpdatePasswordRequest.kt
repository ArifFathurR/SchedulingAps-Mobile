package com.example.schedulleapps.model

data class UpdatePasswordRequest(
    val current_password: String,
    val password: String,
    val password_confirmation: String
)
