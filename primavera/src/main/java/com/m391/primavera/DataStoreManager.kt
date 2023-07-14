package com.m391.primavera

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class DataStoreManager(context: Context) {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "Primavera")
    private val dataStore = context.dataStore

    companion object {
        val currentChildUid = stringPreferencesKey("CURRENT_CHILD_UID")
        val currentChildName = stringPreferencesKey("CURRENT_CHILD_NAME")
        val currentChildFatherUid = stringPreferencesKey("CURRENT_CHILD_FATHER_UID")
        val currentChildFatherName = stringPreferencesKey("CURRENT_CHILD_FATHER_NAME")
        val currentOxygenLevel = stringPreferencesKey("CURRENT_OXYGEN_LEVEL")
        val storedSteps = floatPreferencesKey("STORED_STEPS")

        @Volatile
        private var instance: DataStoreManager? = null

        fun getInstance(context: Context): DataStoreManager {
            return instance ?: synchronized(this) {
                instance ?: DataStoreManager(context).also { instance = it }
            }
        }
    }

    suspend fun setAllSteps(type: Float) {
        dataStore.edit { preferences ->
            preferences[storedSteps] = type ?: 0f
        }
    }

    val stepsTodayFlow: Flow<Float> = context.dataStore.data.map { preferences ->
        preferences[storedSteps] ?: 0F
    }

    suspend fun setFatherUid(uid: String?) {
        dataStore.edit { preferences ->
            preferences[currentChildFatherUid] = uid ?: ""
        }
    }

    suspend fun setFatherName(type: String?) {
        dataStore.edit { preferences ->
            preferences[currentChildFatherName] = type ?: ""
        }
    }

    suspend fun getFatherUid(): String? {
        val preferences = dataStore.data.first()
        return preferences[currentChildFatherUid]
    }

    suspend fun getFatherName(): String? {
        val preferences = dataStore.data.first()
        return preferences[currentChildFatherName]
    }


    suspend fun setCurrentChildUid(uid: String?) {
        dataStore.edit { preferences ->
            preferences[currentChildUid] = uid ?: ""
        }
    }

    suspend fun getCurrentChildUid(): String? {
        val preferences = dataStore.data.first()
        return preferences[currentChildUid]
    }

    suspend fun setCurrentChildName(uid: String?) {
        dataStore.edit { preferences ->
            preferences[currentChildName] = uid ?: ""
        }
    }

    suspend fun getCurrentChildName(): String? {
        val preferences = dataStore.data.first()
        return preferences[currentChildName]
    }

    suspend fun getCurrentOxygenLevel(): String? {
        val preferences = dataStore.data.first()
        return preferences[currentOxygenLevel]
    }
}