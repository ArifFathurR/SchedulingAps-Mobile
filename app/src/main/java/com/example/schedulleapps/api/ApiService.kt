package com.example.schedulleapps.api

import com.example.schedulleapps.auth.LoginRequest
import com.example.schedulleapps.auth.LoginResponse
import com.example.schedulleapps.auth.RegisterRequest
import com.example.schedulleapps.auth.RegisterResponse
import com.example.schedulleapps.model.ScheduleResponse
import com.example.schedulleapps.model.UpdateScheduleRequest
import com.example.schedulleapps.model.UpdateScheduleResponse
import retrofit2.Call
import retrofit2.http.*
import okhttp3.MultipartBody
import okhttp3.RequestBody

interface ApiService {
    @POST("login")
    fun login(@Body request: LoginRequest): Call<LoginResponse>

    @POST("register")
    fun register(@Body request: RegisterRequest): Call<RegisterResponse>

//    @GET("profile")
//    fun getProfile(@Header("Authorization") token: String): Call<User>

    @POST("logout")
    fun logout(@Header("Authorization") token: String): Call<Void>

    @GET("schedule")
    fun getSchedules(
        @Header("Authorization") token: String
    ): Call<ScheduleResponse>

    @PUT("schedule/{id}")
    fun updateSchedule(
        @Path("id") id: Int,
        @Header("Authorization") token: String,
        @Body request: UpdateScheduleRequest
    ): Call<UpdateScheduleResponse>
}