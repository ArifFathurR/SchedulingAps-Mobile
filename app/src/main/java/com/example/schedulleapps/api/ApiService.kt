package com.example.schedulleapps.api

import com.example.schedulleapps.auth.LoginRequest
import com.example.schedulleapps.auth.LoginResponse
import com.example.schedulleapps.model.ScheduleResponse
import retrofit2.Call
import retrofit2.http.*
import okhttp3.MultipartBody
import okhttp3.RequestBody

interface ApiService {
    @POST("login")
    fun login(@Body request: LoginRequest): Call<LoginResponse>

//    @GET("profile")
//    fun getProfile(@Header("Authorization") token: String): Call<User>

    @POST("logout")
    fun logout(@Header("Authorization") token: String): Call<Void>

    @GET("schedule")
    fun getSchedules(
        @Header("Authorization") token: String
    ): Call<ScheduleResponse>
}