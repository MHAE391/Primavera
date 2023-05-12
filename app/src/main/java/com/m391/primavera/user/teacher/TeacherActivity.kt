package com.m391.primavera.user.teacher

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.m391.primavera.R

class TeacherActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_teacher)
        Toast.makeText(applicationContext, "teacher", Toast.LENGTH_SHORT).show()
    }
}