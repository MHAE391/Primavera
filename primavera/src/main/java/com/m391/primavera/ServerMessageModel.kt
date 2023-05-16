package com.m391.primavera

import java.util.*

data class ServerMessageModel(
    val senderUID: String, val message: String, val timeSent: Date,
    val messageType: String, var seen: String,
    val messageUID: String, val mediaPath: String
)
