package com.m391.primavera.chat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.m391.primavera.database.datastore.DataStoreManager
import com.m391.primavera.databinding.ActivityChatBinding
import com.m391.primavera.utils.Constants
import kotlinx.coroutines.launch

class ChatActivity : AppCompatActivity() {
    private lateinit var chatBinding: ActivityChatBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        chatBinding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(chatBinding.root)

    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()

    }

}