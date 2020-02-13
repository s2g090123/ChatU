package com.example.chatu.register

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class RegisterViewModelFactory(private val application: Application): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(RegisterViewModel::class.java)) {
            return RegisterViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

}