package com.example.chatu.request

data class RequestMessage<T>(val action: String, val data: T, val note: String? = null)