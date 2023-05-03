package com.m391.primavera

import android.annotation.SuppressLint
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
import androidx.lifecycle.asLiveData
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.m391.primavera.database.datastore.DataStoreManager
import com.m391.primavera.database.server.Authentication
import com.m391.primavera.database.server.FatherInformation
import com.m391.primavera.database.server.ServerDatabase
import com.m391.primavera.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: Authentication
    private lateinit var fathers: FatherInformation
    private var dataStoreManager: DataStoreManager? = null
    private lateinit var userType: LiveData<String?>

    @SuppressLint("HardwareIds")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = ServerDatabase(applicationContext).authentication
        fathers = ServerDatabase(applicationContext).fatherInformation
        dataStoreManager = DataStoreManager(applicationContext)
        userType = dataStoreManager!!.getUserType().asLiveData()
/*
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
        }*/

        val id = Secure.getString(applicationContext.contentResolver, ANDROID_ID)
        val brand = ""

        binding.id.text = "$id\n${getSystemDetail()}"
        binding.qrCode.setImageBitmap(getQrCodeBitmap(id))
    }


    override fun onPause() {
        super.onPause()
        dataStoreManager = null
    }

    private fun getQrCodeBitmap(id: String): Bitmap {
        val size = 512 //pixels
        val bits = QRCodeWriter().encode(id, BarcodeFormat.QR_CODE, size, size)
        return Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565).also {
            for (x in 0 until size) {
                for (y in 0 until size) {
                    it.setPixel(x, y, if (bits[x, y]) Color.BLACK else Color.WHITE)
                }
            }
        }
    }

    fun getDeviceName(): String {
        val manufacturer = Build.MANUFACTURER
        val model = Build.MODEL
        return if (model.startsWith(manufacturer)) {
            capitalize(model)
        } else capitalize(manufacturer) + " " + model
    }

    private fun capitalize(str: String): String {
        if (TextUtils.isEmpty(str)) {
            return str
        }
        val arr = str.toCharArray()
        var capitalizeNext = true
        val phrase = StringBuilder()
        for (c in arr) {
            if (capitalizeNext && Character.isLetter(c)) {
                phrase.append(c.uppercaseChar())
                capitalizeNext = false
                continue
            } else if (Character.isWhitespace(c)) {
                capitalizeNext = true
            }
            phrase.append(c)
        }
        return phrase.toString()
    }

    @SuppressLint("HardwareIds")
    private fun getSystemDetail(): String {
        return "Brand: ${Build.BRAND} \n" +
                "DeviceID: ${
                    Settings.Secure.getString(
                        contentResolver,
                        Settings.Secure.ANDROID_ID
                    )
                } \n" +
                "Model: ${Build.MODEL} \n" +
                "ID: ${Build.ID} \n" +
                "SDK: ${Build.VERSION.SDK_INT} \n" +
                "Manufacture: ${Build.MANUFACTURER} \n" +
                "Brand: ${Build.BRAND} \n" +
                "User: ${Build.USER} \n" +
                "Type: ${Build.TYPE} \n" +
                "Base: ${Build.VERSION_CODES.BASE} \n" +
                "Incremental: ${Build.VERSION.INCREMENTAL} \n" +
                "Board: ${Build.BOARD} \n" +
                "Host: ${Build.HOST} \n" +
                "FingerPrint: ${Build.FINGERPRINT} \n" +
                "Version Code: ${Build.VERSION.RELEASE}" +
                "\nDevice: ${android.os.Build.DEVICE}"
    }

}