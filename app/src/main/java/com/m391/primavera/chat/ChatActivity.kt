package com.m391.primavera.chat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.KeyEvent
import androidx.lifecycle.lifecycleScope
import com.m391.primavera.database.datastore.DataStoreManager
import com.m391.primavera.databinding.ActivityChatBinding
import com.m391.primavera.utils.Constants
import com.m391.primavera.utils.Constants.CHILD
import com.m391.primavera.utils.Constants.FATHER
import com.m391.primavera.utils.Constants.TEACHER
import kotlinx.coroutines.launch

class ChatActivity : AppCompatActivity() {
    private lateinit var chatBinding: ActivityChatBinding
    private lateinit var dataStoreManager: DataStoreManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        chatBinding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(chatBinding.root)
        dataStoreManager = DataStoreManager.getInstance(applicationContext)
    }

    override fun onResume() {
        super.onResume()
        when (intent!!.extras!!.getString(Constants.TYPE)) {
            TEACHER -> {
                lifecycleScope.launch {
                    dataStoreManager.setCurrentChatReceiver(intent!!.extras!!.getString(Constants.TEACHER_UID))
                }
            }

            FATHER -> {
                lifecycleScope.launch {
                    dataStoreManager.setCurrentChatReceiver(intent!!.extras!!.getString(Constants.FATHER_UID))
                }
            }

            CHILD -> {
                lifecycleScope.launch {
                    dataStoreManager.setCurrentChatReceiver(intent!!.extras!!.getString(Constants.CHILD_UID))
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        lifecycleScope.launch {
            dataStoreManager.setCurrentChatReceiver(null)
        }
    }


}