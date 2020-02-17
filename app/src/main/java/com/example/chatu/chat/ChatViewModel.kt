package com.example.chatu.chat

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.chatu.database.ChatMessage
import com.example.chatu.database.ChatMessageDao
import com.example.chatu.database.ContactDao
import com.example.chatu.request.MessageRequest
import com.example.chatu.request.MessagingApi
import com.example.chatu.request.RequestMessage
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.*

class ChatViewModel(private val messageDao: ChatMessageDao, private val contactDao: ContactDao): ViewModel() {
    lateinit var messages: LiveData<List<ChatMessage>>
    fun getAllMessages(myUid: String, uid:String) {
        messages = messageDao.get(myUid,uid)
    }
    val messageCount = messageDao.getRowCount()

    private val job = Job()
    private val coroutineScope = CoroutineScope(Dispatchers.Main + job)

    private val firebaseDatabase = FirebaseDatabase.getInstance()
    private val databaseRef = firebaseDatabase.reference.child("chats")
    private val userRef = firebaseDatabase.reference.child("user")

    private val _sendClicked = MutableLiveData<Boolean?>()
    val sendClicked: LiveData<Boolean?>
        get() = _sendClicked

    private val _stickerClicked = MutableLiveData<Boolean?>()
    val stickerClicked: LiveData<Boolean?>
        get() = _stickerClicked

    fun onSendClicked() {
        _sendClicked.value = true
    }
    fun doneSendClicked() {
        _sendClicked.value = null
    }

    fun onStickerClicked() {
        _stickerClicked.value = true
    }
    fun doneStickerClicked() {
        _stickerClicked.value = null
    }

    // 從firebase db(chats)更新對方訊息至local db
    fun getMessage(message: ChatMessage) {
        coroutineScope.launch {
            insert(message)
        }
    }

    // 傳送訊息至firebase db(chats)，新增到local db，傳送通知給對方
    fun sendMessage(message: ChatMessage, name: String) {
        coroutineScope.launch {
            databaseRef.push().setValue(message)
            insert(message)
            sendNotification(message,name)
        }
    }

    // 從local db中更新我傳送的未讀訊息，變成已讀訊息
    fun updateMessage(myUid: String, tag: Long) {
        coroutineScope.launch {
            update(myUid,tag)
        }
    }

    fun clearMessage() {
        coroutineScope.launch {
            delete()
        }
    }

    private fun sendNotification(message: ChatMessage, name: String) {
        userRef.child("${message.to_uid}/token").addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {}
            override fun onDataChange(p0: DataSnapshot) {
                val token: String? = p0.getValue(String::class.java)
                coroutineScope.launch {
                    try {
                        val request = RequestMessage("message",message,name)
                        val response = MessagingApi.retrofitService.sendMessageRequest(MessageRequest(request,token!!))
                        response.await()
                    } catch (e: Exception) {
                        Log.i("Chat","error: ${e.message}")
                    }
                }
            }
        })
    }

    private suspend fun insert(message: ChatMessage) {
        withContext(Dispatchers.IO) {
            messageDao.insert(message)
        }
    }

    private suspend fun update(myUid: String, tag: Long) {
        withContext(Dispatchers.IO) {
            messageDao.update(tag,myUid)
        }
    }

    private suspend fun delete() {
        withContext(Dispatchers.IO) {
            messageDao.clear()
        }
    }

    // 重置本地對對方的未讀訊息數量
    fun resetUnread(uid: String) {
        coroutineScope.launch {
            doResetUnread(uid)
        }
    }
    private suspend fun doResetUnread(uid: String) {
        withContext(Dispatchers.IO) {
            contactDao.reset(uid)
        }
    }

    override fun onCleared() {
        super.onCleared()
        job.cancel()
    }
}