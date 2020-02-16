package com.example.chatu

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log

class NetworkChangeReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.i("Chat","receive")
        val connectivityManager = context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if(Build.VERSION.SDK_INT < 23) {
            val networkInfo = connectivityManager.activeNetworkInfo
            networkInfo.runCatching {
                if(this != null && isConnected && type == ConnectivityManager.TYPE_WIFI || type == ConnectivityManager.TYPE_MOBILE)
                    Result.success(this)
                else
                    Result.failure(Throwable())
            }.fold(onSuccess = {
                Log.i("Chat","Connect")
            },onFailure = {
                Log.i("Chat","Disconnect")
            })
        }
        else {
            val network = connectivityManager.activeNetwork
            network?.let {
                val networkCapability = connectivityManager.getNetworkCapabilities(network)
                if(networkCapability.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) || networkCapability.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    Log.i("Chat","Connect")
                    return
                }
            }
            Log.i("Chat","Disconnect")
        }
    }

}