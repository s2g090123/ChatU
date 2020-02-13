package com.example.chatu.register

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class RegisterViewModel(private val application: Application): ViewModel() {

    private val _navigateToContact = MutableLiveData<Boolean>()
    val navigateToContact: LiveData<Boolean>
        get() = _navigateToContact

    fun doneNavigating() {
        _navigateToContact.value = null
    }
    fun doNavigating() {
        _navigateToContact.value = true
    }
    fun setPreferenceName(name: String): String {
        val sharePre = application.getSharedPreferences("ChatU",0)
        sharePre.edit().putString("name",name).putString("uid","12345678").apply()
        return "12345678"
    }
}