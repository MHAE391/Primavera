package com.m391.primavera.user.father

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.m391.primavera.database.datastore.DataStoreManager
import com.m391.primavera.database.server.FatherInformation
import com.m391.primavera.database.server.MessageInformation
import com.m391.primavera.database.server.ServerDatabase
import com.m391.primavera.databinding.ActivityFatherBinding
import kotlinx.coroutines.launch

class FatherActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFatherBinding
    private lateinit var dataStoreManager: DataStoreManager
    private lateinit var messaging: MessageInformation
    private lateinit var father: FatherInformation


    @SuppressLint("InlinedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        dataStoreManager = DataStoreManager.getInstance(applicationContext)
        messaging = ServerDatabase(applicationContext, dataStoreManager).messageInformation
        father = ServerDatabase(applicationContext, dataStoreManager).fatherInformation
        lifecycleScope.launch {
            messaging.subscribeToTopic()
        }
        binding = ActivityFatherBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

}