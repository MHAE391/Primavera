package com.m391.primavera.authentication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.m391.primavera.R
import com.m391.primavera.authentication.information.InformationActivity
import com.m391.primavera.database.server.ServerDatabase
import com.m391.primavera.databinding.ActivityAuthenticationBinding

class AuthenticationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAuthenticationBinding
    private val auth = ServerDatabase().authentication
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthenticationBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onStart() {
        super.onStart()
        if (auth.getCurrentUser() != null) {
            startActivity(Intent(this@AuthenticationActivity, InformationActivity::class.java))
            finish()
        }
    }
}