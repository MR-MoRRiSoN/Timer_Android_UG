package com.example.timer_android_ug.service

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface CountdownApiService {

    @GET("/api/countdown/{userId}")
    fun getCountdown(@Path("userId") userId: String): Call<Long>
}
