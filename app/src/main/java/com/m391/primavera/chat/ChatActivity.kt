package com.m391.primavera.chat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.m391.primavera.databinding.ActivityChatBinding

class ChatActivity : AppCompatActivity() {
    private lateinit var chatBinding: ActivityChatBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        chatBinding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(chatBinding.root)
    }
}