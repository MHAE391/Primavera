package com.m391.primavera

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: Authentication
    private lateinit var fathers: FatherInformation
    private var dataStoreManager: DataStoreManager? = null
    private lateinit var userUid: LiveData<String?>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = ServerDatabase(applicationContext).authentication
        fathers = ServerDatabase(applicationContext).fatherInformation
        dataStoreManager = DataStoreManager(applicationContext)
        userUid = dataStoreManager!!.getUserUid().asLiveData()

        if (auth.getCurrentUser() == null) {
            startActivity(Intent(this@MainActivity, AuthenticationActivity::class.java))
            finish()
        } else {
            userUid.observe(this, Observer {
                if (it != null) {
                    startActivity(Intent(this@MainActivity, FatherActivity::class.java))
                    finish()
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