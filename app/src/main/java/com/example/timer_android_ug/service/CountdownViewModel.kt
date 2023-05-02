package com.example.timer_android_ug.service

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class CountdownViewModel(private val userId: String) : ViewModel() {

    private val countdownApiService: CountdownApiService

    private val _remainingTime = MutableLiveData<String>()
    val remainingTime: LiveData<String> = _remainingTime

    private val updateIntervalMillis = 1000L

    private val handler = Handler(Looper.getMainLooper())
    private val runnable = object : Runnable {
        override fun run() {
            fetchCountdown()
            handler.postDelayed(this, updateIntervalMillis)
        }
    }

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.1.29:8080")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        countdownApiService = retrofit.create(CountdownApiService::class.java)

        handler.post(runnable)
    }

    private fun fetchCountdown() {
        countdownApiService.getCountdown(userId).enqueue(object : Callback<Long> {
            override fun onResponse(call: Call<Long>, response: Response<Long>) {
                if (response.isSuccessful) {
                    val remainingTime = response.body() ?: return
                    val hours = TimeUnit.SECONDS.toHours(remainingTime)
                    val minutes = TimeUnit.SECONDS.toMinutes(remainingTime) % 60
                    val seconds = remainingTime % 60
                    val timeText = String.format("%02d:%02d:%02d", hours, minutes, seconds)
                    _remainingTime.postValue(timeText)
                }
            }

            override fun onFailure(call: Call<Long>, t: Throwable) {
                // Handle the error
            }
        })
    }

    override fun onCleared() {
        super.onCleared()
        handler.removeCallbacks(runnable)
    }
}
