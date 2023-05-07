package com.m391.primavera

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.provider.Settings.Secure
import android.provider.Settings.Secure.*
import android.text.TextUtils
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.asLiveData
import com.m391.primavera.authentication.AuthenticationActivity
import com.m391.primavera.authentication.information.InformationActivity
import com.m391.primavera.database.datastore.DataStoreManager
import com.m391.primavera.database.server.Authentication
import com.m391.primavera.database.server.FatherInformation
import com.m391.primavera.database.server.ServerDatabase
import com.m391.primavera.databinding.ActivityMainBinding
import com.m391.primavera.user.father.FatherActivity
import com.m391.primavera.user.teacher.TeacherActivity
import com.m391.primavera.utils.Constants.FATHER
import com.m391.primavera.utils.Constants.TEACHER


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: Authentication
    private lateinit var fathers: FatherInformation
    private var dataStoreManager: DataStoreManager? = null
    private lateinit var userType: LiveData<String?>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = ServerDatabase(applicationContext).authentication
        fathers = ServerDatabase(applicationContext).fatherInformation
        dataStoreManager = DataStoreManager(applicationContext)
        userType = dataStoreManager!!.getUserType().asLiveData()

        if (auth.getCurrentUser() == null) {
            startActivity(Intent(this@MainActivity, AuthenticationActivity::class.java))
            finish()
        } else {
            userType.observe(this, Observer {
                if (it != null) {
                    when (it) {
                        TEACHER -> {
                            startActivity(Intent(this@MainActivity, TeacherActivity::class.java))
                            finish()
                        }
                        FATHER -> {
                            startActivity(Intent(this@MainActivity, FatherActivity::class.java))
                            finish()
                        }
                    }
                } else {
                    startActivity(Intent(this@MainActivity, InformationActivity::class.java))
                    finish()
                }
            })
        }
    }


    override fun onPause() {
        super.onPause()
        dataStoreManager = null
    }

}