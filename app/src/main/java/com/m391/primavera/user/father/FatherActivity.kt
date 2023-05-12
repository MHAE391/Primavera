package com.m391.primavera.user.father

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.google.firebase.messaging.FirebaseMessaging
import com.m391.primavera.R
import com.m391.primavera.databinding.ActivityFatherBinding
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class FatherActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFatherBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFatherBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}