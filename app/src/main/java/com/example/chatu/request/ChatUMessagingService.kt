package com.example.chatu.request

import android.R
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.chatu.MainActivity
import com.example.chatu.database.ChatUDatabase
import com.example.chatu.database.Contact
import com.example.chatu.database.Invitation
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class ChatUMessagingService: FirebaseMessagingService() {

    private lateinit var broadcastManager: LocalBroadcastManager
    private lateinit var coroutineScope: CoroutineScope
    override fun onCreate() {
        super.onCreate()
        broadcastManager = LocalBroadcastManager.getInstance(applicationContext)
        coroutineScope = CoroutineScope(Dispatchers.Main)
    }

    override fun onMessageReceived(p0: RemoteMessage) {
        super.onMessageReceived(p0)
        val data = p0.data
        if(data != null) {
            when(data["action"]) {
                "register" -> {
                    val sharedPref: SharedPreferences = applicationContext.getSharedPreferences("ChatU",0)
                    sharedPref.edit().putString("uid", data["uid"]).putString("token", data["token"]).apply()
                    sendNotification("${data["name"]}, 你的註冊已被審核","UID: ${data["uid"]}")
                }
                "message" -> {
                    val sharedPref: SharedPreferences = applicationContext.getSharedPreferences("ChatU",0)
                    val otherUser = sharedPref.getString("otherUser",null)
                    if(data["from_uid"] != otherUser) {
                        updateUnRead(data["from_uid"]!!)
                        if(data["type"] == "0")
                            sendNotification("${data["name"]}:",data["content"]!!)
                        else
                            sendNotification("${data["name"]}:","傳了一個貼圖")
                    }
                }
                "find" -> {
                    findResultBroadcast(data["to_uid"],data["name"])
                }
                "invitation" -> {
                    sendNotification("來自${data["name"]}","有一則好友邀請")
                    insertInvitation(Invitation(data["time"]!!,data["from_uid"]!!,data["to_uid"]!!,data["name"]!!,null))
                }
                "reply" -> {
                    sendNotification("來自${data["name"]}的邀請回覆:",data["answer"]!!)
                    if(data["answer"] == "Yes") {
                        insertContact(Contact(data["from_uid"]!!,data["name"]!!))
                    }
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

    private fun sendNotification(title: String, body: String) {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0 /* request code */,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val pattern = longArrayOf(500, 500, 500, 500, 500)

        val defaultSoundUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel("1","channel_name",NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(notificationChannel)
        }

        val notificationBuilder = NotificationCompat.Builder(this)
            .setSmallIcon(R.drawable.stat_notify_chat)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setVibrate(pattern)
            .setLights(Color.BLUE, 1, 1)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)
            .setChannelId("1") as NotificationCompat.Builder

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build())
    }

    private fun insertInvitation(invitation: Invitation) {
        coroutineScope.launch {
            doInsertInvitation(invitation)
        }
    }

    private suspend fun doInsertInvitation(invitation: Invitation) {
        withContext(Dispatchers.IO) {
            val invitationDao = ChatUDatabase.getInstance(applicationContext).invitationDao
            invitationDao.insert(invitation)
        }
    }

    private fun insertContact(contact: Contact) {
        coroutineScope.launch {
                doInsertContact(contact)
        }
    }

    private suspend fun doInsertContact(contact: Contact) {
        withContext(Dispatchers.IO) {
            val contactDao = ChatUDatabase.getInstance(applicationContext).contactDao
            contactDao.insert(contact)
        }
    }

    private fun updateUnRead(uid: String) {
        coroutineScope.launch {
            doUpdateUnRead(uid)
        }
    }

    private suspend fun doUpdateUnRead(uid: String) {
        withContext(Dispatchers.IO) {
            val contactDao = ChatUDatabase.getInstance(applicationContext).contactDao
            contactDao.update(uid)
        }
    }
}