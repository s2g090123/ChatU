package com.example.chatu.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.chatu.database.ChatMessageDao
import com.example.chatu.database.ContactDao

class ChatViewModelFactory(private val messageDao: ChatMessageDao, private val contactDao: ContactDao): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            return ChatViewModel(messageDao,contactDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}