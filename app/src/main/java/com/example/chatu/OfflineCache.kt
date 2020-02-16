package com.example.chatu

import android.app.Application
import com.google.firebase.database.FirebaseDatabase

class OfflineCache: Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
    }
}