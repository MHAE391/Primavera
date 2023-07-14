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
import com.m391.primavera.utils.Constants.TEACHER
import com.m391.primavera.utils.Constants.TYPE
import kotlinx.coroutines.launch
import kotlin.properties.Delegates


class Primavera : AppCompatActivity() {
    private lateinit var binding: PrimaveraBinding
    private lateinit var auth: Authentication
    private var fathers by Delegates.notNull<Boolean>()
    private var teachers by Delegates.notNull<Boolean>()
    private var dataStoreManager: DataStoreManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // appCheck()
        binding = PrimaveraBinding.inflate(layoutInflater)
        dataStoreManager = DataStoreManager.getInstance(applicationContext)
        auth = ServerDatabase(applicationContext, dataStoreManager!!).authentication
        lifecycleScope.launch {
            val userType = dataStoreManager?.getUserType()
            if (userType != null) {
                when (userType) {
                    TEACHER -> {
                        startActivity(
                            Intent(
                                this@Primavera,
                                TeacherActivity::class.java
                            )
                        )
                        finish()
                    }

                    FATHER -> {
                        startActivity(Intent(this@Primavera, FatherActivity::class.java))
                        finish()
                    }
                }
            } else {
                if (auth.getCurrentUser() == null) {
                    startActivity(
                        Intent(
                            this@Primavera,
                            AuthenticationActivity::class.java
                        )
                    )
                    finish()
                } else {
                    fathers = ServerDatabase(
                        applicationContext,
                        dataStoreManager!!
                    ).fatherInformation.checkAlreadyFatherOrNot()
                    teachers = ServerDatabase(
                        applicationContext,
                        dataStoreManager!!
                    ).teacherInformation.checkAlreadyTeacherOrNot()
                    when {
                        !fathers && !teachers -> {
                            startActivity(Intent(this@Primavera, InformationActivity::class.java))
                            finish()
                        }

                        fathers && teachers -> {
                            showChoiceAlert()
                        }

                        fathers -> {
                            startActivity(Intent(this@Primavera, FatherActivity::class.java))
                            dataStoreManager!!.setUserType(FATHER)
                            finish()
                        }

                        teachers -> {
                            startActivity(Intent(this@Primavera, TeacherActivity::class.java))
                            dataStoreManager!!.setUserType(TEACHER)
                            finish()
                        }

                        else -> {
                            startActivity(
                                Intent(
                                    this@Primavera,
                                    AuthenticationActivity::class.java
                                )
                            )
                            finish()
                        }
                    }
                }
            }
        }

        /*
                startActivity(Intent(this, TeacherActivity::class.java))
                finish()*/
        setContentView(binding.root)
    }

    override fun onPause() {
        super.onPause()
        dataStoreManager = null
    }

    private fun showChoiceAlert() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Multiple Account")
            .setMessage("Choose Father to open Father Mode\nTeacher to open Teacher Mode")
            .setPositiveButton("Father") { _, _ ->
                lifecycleScope.launch {
                    dataStoreManager!!.setUserType(FATHER)
                    startActivity(Intent(this@Primavera, FatherActivity::class.java))
                    finish()
                }
            }.setNegativeButton("Teacher") { _, _ ->
                lifecycleScope.launch {
                    dataStoreManager!!.setUserType(TEACHER)
                    startActivity(Intent(this@Primavera, TeacherActivity::class.java))
                    finish()
                }
            }.show()
    }

}
