package com.example.timer_android_ug.service

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class CountdownViewModelFactory(private val userId: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CountdownViewModel::class.java)) {
            return CountdownViewModel(userId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
