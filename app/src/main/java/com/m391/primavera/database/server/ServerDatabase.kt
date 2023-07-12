package com.m391.primavera.database.server

import android.content.Context
import com.m391.primavera.database.datastore.DataStoreManager

class ServerDatabase(context: Context, dataStoreManager: DataStoreManager) {

    val authentication = Authentication()
    val fatherInformation = FatherInformation(context, dataStoreManager)
    val childInformation = ChildInformation(context, dataStoreManager)
    val teacherInformation = TeacherInformation(context, dataStoreManager)
    val messageInformation = MessageInformation(context, dataStoreManager)
    val conversations = ConversationInformation(context, dataStoreManager)
    val watches = WatchInformation(context)
    val healthHistoryInformation = HealthHistoryInformation(context, dataStoreManager)
}