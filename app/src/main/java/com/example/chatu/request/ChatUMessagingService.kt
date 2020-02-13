package com.example.chatu.request

import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.chatu.database.ChatMessage
import com.example.chatu.database.ChatMessageDao
import com.example.chatu.database.ChatUDatabase
import com.example.chatu.database.Invitation
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChatUMessagingService: FirebaseMessagingService() {

    private lateinit var broadcastManager: LocalBroadcastManager
    override fun onCreate() {
        super.onCreate()
        broadcastManager = LocalBroadcastManager.getInstance(applicationContext)
    }

    override fun onMessageReceived(p0: RemoteMessage) {
        super.onMessageReceived(p0)
        val data = p0.data
        if(data != null) {
            when(data["action"]) {
                "register" -> {
                    val sharedPref: SharedPreferences = applicationContext.getSharedPreferences("ChatU",0)
                    sharedPref.edit().putString("uid", data["uid"]).putString("token", data["token"]).apply()
                }
                "find" -> {
                    findResultBroadcast(data["to_uid"],data["name"])
                }
                "invitation" -> {
                    invitationBroadcast(Invitation(data["time"]!!,data["from_uid"]!!,data["to_uid"]!!,data["name"]!!,null))
                }
                "reply" -> {
                    replyBroadcast(Invitation(data["time"]!!,data["from_uid"]!!,data["to_uid"]!!,data["name"]!!,data["answer"]))
                }
            }
        }
    }

    private fun findResultBroadcast(uid: String?, name: String?) {
        val intent = Intent("Find")
        intent.putExtra("uid",uid)
        intent.putExtra("name",name)
        broadcastManager.sendBroadcast(intent)
    }

    private fun invitationBroadcast(invitation: Invitation) {
        val intent = Intent("Invitation")
        intent.putExtra("invitation",invitation)
        broadcastManager.sendBroadcast(intent)
    }

    private fun replyBroadcast(reply: Invitation) {
        val intent = Intent("Reply")
        intent.putExtra("reply",reply)
        broadcastManager.sendBroadcast(intent)
    }
}