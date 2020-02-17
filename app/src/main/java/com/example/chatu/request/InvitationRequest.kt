package com.example.chatu.request

import com.example.chatu.database.Invitation

data class InvitationRequest(
    val data: RequestMessage<Invitation>,
    val to: String
)