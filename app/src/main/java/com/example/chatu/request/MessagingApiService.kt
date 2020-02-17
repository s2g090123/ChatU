package com.example.chatu.request

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import kotlinx.coroutines.Deferred
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

private const val baseUrl = "https://fcm.googleapis.com/"
private const val serverKey = "AIzaSyBlX9-q9IYdisJ4JPr17flw-hIZgVYnwZE"

private val retrofit = Retrofit.Builder().baseUrl(baseUrl).addConverterFactory(GsonConverterFactory.create())
    .addCallAdapterFactory(CoroutineCallAdapterFactory()).build()

interface MessagingApiService {
    @Headers(
        "Content-Type:application/json",
        "Authorization:key=$serverKey"
    )
    @POST("fcm/send")
    fun sendInvitationRequest(@Body body: InvitationRequest): Deferred<Response>

    @Headers(
        "Content-Type:application/json",
        "Authorization:key=$serverKey"
    )
    @POST("fcm/send")
    fun sendMessageRequest(@Body body: MessageRequest): Deferred<Response>

}

object MessagingApi {
    val retrofitService: MessagingApiService by lazy {
        retrofit.create(MessagingApiService::class.java)
    }
}