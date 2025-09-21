package com.example.schedulleapps.auth

import com.example.schedulleapps.model.User

data class LoginResponse(
    val user: User,
    val token: String
)