package com.example.chatu.chat

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.chatu.database.ChatMessage
import com.example.chatu.database.ChatMessageDao
import com.example.chatu.request.RequestMessage
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.*

class ChatViewModel(private val messageDao: ChatMessageDao, myUid: String, uid: String): ViewModel() {
    val messages = messageDao.get(myUid,uid)
    val messageCount = messageDao.getRowCount()

    private val job = Job()
    private val coroutineScope = CoroutineScope(Dispatchers.Main + job)

    private val firebaseDatabase = FirebaseDatabase.getInstance()
    private val databaseRef = firebaseDatabase.reference.child("chats")
    private val requestRef = firebaseDatabase.reference.child("requests")

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

    fun sendMessage(message: ChatMessage) {
        coroutineScope.launch {
            messageToServer(message)
            insert(message)
        }
    }

    fun getMessage(message: ChatMessage) {
        coroutineScope.launch {
            insert(message)
        }
    }

    private suspend fun insert(message: ChatMessage) {
        withContext(Dispatchers.IO) {
            messageDao.insert(message)
        }
    }

    private fun messageToServer(message: ChatMessage) {
        requestRef.push().setValue(RequestMessage("message",message))
        databaseRef.push().setValue(message)
    }

    override fun onCleared() {
        super.onCleared()
        job.cancel()
    }

    fun clearMessage() {
        coroutineScope.launch {
            delete()
        }
    }

    private suspend fun delete() {
        withContext(Dispatchers.IO) {
            messageDao.clear()
        }
    }

    fun updateMessage(tag: Long) {
        coroutineScope.launch {
            update(tag)
        }
    }

    private suspend fun update(tag: Long) {
        withContext(Dispatchers.IO) {
            messageDao.update(tag)
        }
    }
}