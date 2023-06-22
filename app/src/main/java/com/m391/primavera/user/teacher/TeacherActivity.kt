package com.m391.primavera.user.teacher

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.m391.primavera.R
import com.m391.primavera.database.datastore.DataStoreManager
import com.m391.primavera.database.server.MessageInformation
import com.m391.primavera.database.server.ServerDatabase
import com.m391.primavera.database.server.TeacherInformation
import kotlinx.coroutines.launch

class TeacherActivity : AppCompatActivity() {
    private lateinit var dataStoreManager: DataStoreManager
    private lateinit var messaging: MessageInformation
    private lateinit var teacher: TeacherInformation
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dataStoreManager = DataStoreManager.getInstance(applicationContext)
        messaging = ServerDatabase(applicationContext, dataStoreManager).messageInformation
        teacher = ServerDatabase(applicationContext, dataStoreManager).teacherInformation
        lifecycleScope.launch {
            messaging.subscribeToTopic()
        }
        setContentView(R.layout.activity_teacher)
    }
}