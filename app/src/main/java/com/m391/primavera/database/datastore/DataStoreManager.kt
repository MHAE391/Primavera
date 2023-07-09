package com.m391.primavera.database.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.m391.primavera.utils.Constants
import com.m391.primavera.utils.Constants.NO_LOGGED_IN_USER
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class DataStoreManager(context: Context) {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "Primavera")
    private val dataStore = context.dataStore

    companion object {
        val currentUserUid = stringPreferencesKey("CURRENT_USER_DATA_KEY")
        val currentUserType = stringPreferencesKey("CURRENT_USER_TYPE_KEY")
        val currentChildUid = stringPreferencesKey("CURRENT_CHILD_UID")
        val currentChatReceiver = stringPreferencesKey("CURRENT_CHAT_RECEIVER")

        @Volatile
        private var instance: DataStoreManager? = null

        fun getInstance(context: Context): DataStoreManager {
            return instance ?: synchronized(this) {
                instance ?: DataStoreManager(context).also { instance = it }
            }
        }
    }


    suspend fun setUserUid(uid: String?) {
        dataStore.edit { preferences ->
            preferences[currentUserUid] = uid ?: NO_LOGGED_IN_USER
        }
    }

    suspend fun setUserType(type: String?) {
        dataStore.edit { preferences ->
            preferences[currentUserType] = type ?: NO_LOGGED_IN_USER
        }
    }

    suspend fun getUserUid(): String? {
        val preferences = dataStore.data.first()
        return preferences[currentUserUid]
    }

    suspend fun getUserType(): String? {
        val preferences = dataStore.data.first()
        return preferences[currentUserType]
    }

    fun userType(): Flow<String?> {
        return dataStore.data.map { pre ->
            pre[currentUserType]
        }
    }


    suspend fun setCurrentChildUid(uid: String?) {
        dataStore.edit { preferences ->
            preferences[currentChildUid] = uid ?: NO_LOGGED_IN_USER
        }
    }

    suspend fun getCurrentChildUid(): String? {
        val preferences = dataStore.data.first()
        return preferences[currentChildUid]
    }

    suspend fun setCurrentChatReceiver(uid: String?) {
        dataStore.edit { preferences ->
            preferences[currentChatReceiver] = uid ?: NO_LOGGED_IN_USER
        }
    }

    suspend fun getCurrentChatReceiver(): String? {
        val preferences = dataStore.data.first()
        return preferences[currentChatReceiver]
    }

}