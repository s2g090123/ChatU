package com.example.chatu.request

import com.example.chatu.database.ChatMessage

data class MessageRequest(
    val data: RequestMessage<ChatMessage>,
    val to: String
)