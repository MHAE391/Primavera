package com.m391.primavera

import android.Manifest
import android.app.AlertDialog
import android.app.AuthenticationRequiredException
import android.content.Intent
import android.os.Build
import android.os.Build.VERSION
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.lifecycleScope
import com.m391.primavera.authentication.AuthenticationActivity
import com.m391.primavera.authentication.information.InformationActivity

import com.m391.primavera.database.datastore.DataStoreManager
import com.m391.primavera.database.server.Authentication
import com.m391.primavera.database.server.ServerDatabase
import com.m391.primavera.databinding.PrimaveraBinding
import com.m391.primavera.user.father.FatherActivity
import com.m391.primavera.user.teacher.TeacherActivity
import com.m391.primavera.utils.Constants.FATHER
import com.m391.primavera.utils.Constants.NO_LOGGED_IN_USER
import com.m391.primavera.utils.Constants.TEACHER
import com.m391.primavera.utils.Constants.TYPE
import kotlinx.coroutines.launch
import kotlin.properties.Delegates


class Primavera : AppCompatActivity() {
    private lateinit var binding: PrimaveraBinding
    private var dataStoreManager: DataStoreManager? = null
    var type: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = PrimaveraBinding.inflate(layoutInflater)
        setContentView(binding.root)
        dataStoreManager = DataStoreManager.getInstance(applicationContext)/*
        when (intent.extras?.getString(TYPE)) {
            TEACHER -> {
                type = TEACHER
                startActivity(Intent(this@Primavera, TeacherActivity::class.java))
                finish()
            }

            FATHER -> {
                type = FATHER
                startActivity(Intent(this@Primavera, FatherActivity::class.java))
                finish()
            }

            NO_LOGGED_IN_USER -> {
                type = NO_LOGGED_IN_USER
                startActivity(Intent(this@Primavera, AuthenticationActivity::class.java))
                finish()
            }
        }*/
    }

    override fun onPause() {
        super.onPause()

    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            when (dataStoreManager!!.getUserType()) {
                FATHER -> {
                    startActivity(Intent(this@Primavera, FatherActivity::class.java))
                    finish()
                }

                TEACHER -> {
                    startActivity(Intent(this@Primavera, TeacherActivity::class.java))
                    finish()
                }

                else -> {
                    startActivity(Intent(this@Primavera, AuthenticationActivity::class.java))
                    finish()
                }
            }
        }

    }
}

