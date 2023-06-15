package com.m391.primavera

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.m391.primavera.authentication.AuthenticationActivity

import com.m391.primavera.database.datastore.DataStoreManager
import com.m391.primavera.database.server.Authentication
import com.m391.primavera.databinding.PrimaveraBinding
import com.m391.primavera.user.father.FatherActivity
import com.m391.primavera.utils.Constants.TYPE
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
        //  dataStoreManager = DataStoreManager.getInstance(applicationContext)
//        auth = ServerDatabase(applicationContext, dataStoreManager!!).authentication
        /* lifecycleScope.launch {
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
                             Toast.makeText(this@Primavera, "poth", Toast.LENGTH_SHORT).show()
                             /* Show Fragment to Choose Which Ui Want to Use*/
                             startActivity(Intent(this@Primavera, FatherActivity::class.java))
                             finish()
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
         }*/
        startActivity(Intent(this, FatherActivity::class.java))
        finish()
        setContentView(binding.root)
    }

    override fun onPause() {
        super.onPause()
        dataStoreManager = null
    }


}
