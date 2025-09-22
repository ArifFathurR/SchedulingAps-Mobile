package com.example.schedulleapps.auth
import com.example.schedulleapps.model.User


data class RegisterResponse(
    val message: String,
    val user: User,
    val token: String
)