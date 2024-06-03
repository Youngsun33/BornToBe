package com.example.borntobe

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.catch

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auto_login")
class DataStoreModule(private val context: Context) {
    // Key 값 변수들
    private object PreferenceKeys {
        val USER_ID = stringPreferencesKey("user_id")
        val USER_PW = stringPreferencesKey("user_pw")
        val USER_NAME = stringPreferencesKey("user_name")
        val AUTO_LOGIN_STATE = booleanPreferencesKey("auto_login_state")
    }

    // userIDFlow 변수 : 저장된 사용자 ID 값 읽어옴
    val userIDFlow: Flow<String> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            // No type safety.
            preferences[PreferenceKeys.USER_ID] ?: "None"
        }
    // saveUserID() : 전달 받은 사용자 ID 저장
    suspend fun saveUserID(userID: String) {
        context.dataStore.edit { settings ->
            settings[PreferenceKeys.USER_ID] = userID
        }
    }
    
    // userPWFlow 변수 : 저장된 사용자 PW 값 읽어옴
    val userPWFlow: Flow<String> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            // No type safety.
            preferences[PreferenceKeys.USER_PW] ?: "None"
        }
    // saveUserPW() : 전달 받은 사용자 PW 저장
    suspend fun saveUserPW(userPW: String) {
        context.dataStore.edit { settings ->
            settings[PreferenceKeys.USER_PW] = userPW
        }
    }

    // userNameFlow 변수 : 저장된 사용자 name 값 읽어옴
    val userNameFlow: Flow<String> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            // No type safety.
            preferences[PreferenceKeys.USER_NAME] ?: "None"
        }
    // saveUserName() : 전달 받은 사용자 name 저장
    suspend fun saveUserName(userName: String) {
        context.dataStore.edit { settings ->
            settings[PreferenceKeys.USER_NAME] = userName
        }
    }

    // userNameFlow 변수 : 저장된 사용자 name 값 읽어옴
    val autoLoginState: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            // No type safety.
            preferences[PreferenceKeys.AUTO_LOGIN_STATE] ?: false
        }
    // saveAutoLoginState() : 전달 받은 자동 로그인 설정 여부 저장
    suspend fun saveAutoLoginState(state: Boolean) {
        context.dataStore.edit { settings ->
            settings[PreferenceKeys.AUTO_LOGIN_STATE] = state
        }
    }
}