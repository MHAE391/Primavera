package com.m391.primavera.utils.models

import java.util.*

data class ServerMessageModel(
    val senderUID: String, val message: String, val timeSent: Date,
    val messageType: String, var seen: String,
    val messageUID: String, val mediaPath: String
)
