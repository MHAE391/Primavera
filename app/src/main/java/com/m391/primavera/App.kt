package com.m391.primavera

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.m391.primavera.database.datastore.DataStoreManager
import com.m391.primavera.database.server.Authentication
import com.m391.primavera.database.server.ServerDatabase
import com.m391.primavera.utils.Constants.FATHER
import com.m391.primavera.utils.Constants.TEACHER
import com.m391.primavera.utils.Constants.TYPE
import kotlinx.coroutines.runBlocking
import kotlin.properties.Delegates

class App : Application() {
    private var dataStoreManager: DataStoreManager? = null
    private lateinit var auth: Authentication
    private var fathers by Delegates.notNull<Boolean>()
    private var teachers by Delegates.notNull<Boolean>()
    private lateinit var primavera: Primavera
    override fun onCreate() {
        super.onCreate()
    }

    override fun onTerminate() {
        super.onTerminate()
        dataStoreManager = null
    }
}