package com.m391.primavera.user.teacher.home.switjha

import android.app.Application
import com.m391.primavera.database.datastore.DataStoreManager
import com.m391.primavera.database.server.ServerDatabase
import com.m391.primavera.utils.BaseViewModel
import com.m391.primavera.utils.Constants.FATHER
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TeacherSwitchViewModel(app: Application) : BaseViewModel(app) {

    private val dataStore = DataStoreManager.getInstance(app.applicationContext)
    private val fathers = ServerDatabase(app.applicationContext, dataStore).fatherInformation
    private val auth = ServerDatabase(app.applicationContext, dataStore).authentication
    private val messageInformation =
        ServerDatabase(app.applicationContext, dataStore).messageInformation

    suspend fun switchTeacherFather(): Boolean = withContext(Dispatchers.IO) {
        return@withContext fathers.checkAlreadyFatherOrNot()
    }

    suspend fun setCurrentUserType() = withContext(Dispatchers.IO) {
        dataStore.setUserType(FATHER)
        if (dataStore.getCurrentChildUid() == null)
            dataStore.setCurrentChildUid(fathers.getRandomChildUID())
    }

    suspend fun logout() = withContext(Dispatchers.IO) {
        dataStore.setUserType(null)
        dataStore.setUserUid(null)
        messageInformation.unSubscribeToTopic()
        auth.logOut()
    }
}