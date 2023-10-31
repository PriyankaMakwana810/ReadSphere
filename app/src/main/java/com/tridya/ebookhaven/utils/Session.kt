package com.tridya.ebookhaven.utils

import android.content.Context
import com.google.gson.Gson
import com.tridya.ebookhaven.R
import com.tridya.ebookhaven.models.UserPref

class Session(val context: Context) {
    val pref =
        context.getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE)

    var isLoggedIn: Boolean
        get() = pref.contains(KEY_IS_LOGIN) && pref.getBoolean(KEY_IS_LOGIN, false)
        set(isLoggedIn) = storeDataByKey(KEY_IS_LOGIN, isLoggedIn)

    var userPref: UserPref?
        get() {
            val gson = Gson()
            val json = getDataByKey(KEY_USER_INFO, "")
            return gson.fromJson(json, UserPref::class.java)
        }
        set(userPref) {
            val gson = Gson()
            val json = gson.toJson(userPref)
            pref.edit().putString(KEY_USER_INFO, json).apply()
            isLoggedIn = true
        }

    fun getDataByKey(Key: String, DefaultValue: String = ""): String {
        return if (pref.contains(Key)) {
            pref.getString(Key, DefaultValue).toString()
        } else {
            DefaultValue
        }
    }

    fun getDataByKey(Key: String, DefaultValue: Boolean = false): Boolean {
        return if (pref.contains(Key)) {
            pref.getBoolean(Key, DefaultValue)
        } else {
            DefaultValue
        }
    }

    fun storeDataByKey(key: String, Value: String) {
        pref.edit().putString(key, Value).apply()
    }

    fun storeDataByKey(key: String, Value: Boolean) {
        pref.edit().putBoolean(key, Value).apply()
    }

    operator fun contains(key: String): Boolean {
        return pref.contains(key)
    }

    fun remove(key: String) {
        pref.edit().remove(key).apply()
    }

    fun logout() {
        pref.edit().clear().apply()
        this.userPref = null
        this.isLoggedIn = false
    }

    private companion object {
        private const val KEY_IS_LOGIN = "isLogin"
        private const val KEY_USER_INFO = "userPref"
    }
}