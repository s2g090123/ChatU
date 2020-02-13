package com.example.chatu.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.chatu.database.ChatMessageDao

class ChatViewModelFactory(private val messageDao: ChatMessageDao, private val myUid: String, private val uid: String): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            return ChatViewModel(messageDao,myUid,uid) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}