package com.android.services.sharedprefs

import android.annotation.SuppressLint
import android.content.SharedPreferences
import com.android.services.MyApplication
import java.util.*
import kotlin.reflect.KProperty

class DelegateSharedPref<T>(val key: String? = null, val defaultValue: T) {

    // if key given null property Name act like key. example : myFirstName will make key as SHP_My_First_Name
    // Use this for Boolean , Int , Long, Float & String only Otherwise it throws IllegalArgumentException
    val pref: SharedPreferences by lazy {
        MyApplication.appContext.getSharedPreferences("MyPrefFile", 0)
    }

    operator fun getValue(instance: Any, property: KProperty<*>): T {
        val key = key ?: "SHP_" + property.name.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
        }
            .toSnakeCase()
        return findPreferences(key, defaultValue)
    }

    operator fun setValue(instance: Any, property: KProperty<*>, newValue: T) {
        val key = key ?: "SHP_" + property.name.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
        }
            .toSnakeCase()
        return savePreference(key, newValue)
    }

    @Suppress("UNCHECKED_CAST")
    private fun findPreferences(key: String, value: T): T {
        with(pref) {
            val result: Any = when (value) {
                is Boolean -> getBoolean(key, value)
                is Int -> getInt(key, value)
                is Long -> getLong(key, value)
                is Float -> getFloat(key, value)
                is String -> getString(key, value)!!
                else -> throw IllegalArgumentException()
            }
            return result as T
        }
    }

    @SuppressLint("CommitPrefEdits")
    private fun savePreference(key: String, value: T) {
        with(pref.edit()) {
            when (value) {
                is Boolean -> putBoolean(key, value)
                is Int -> putInt(key, value)
                is Long -> putLong(key, value)
                is Float -> putFloat(key, value)
                is String -> putString(key, value)
                else -> throw IllegalArgumentException()
            }
        }
    }

    fun String.toSnakeCase(): String {  // extension which converts input like "MyProfileName" to "My_Profile_Name"
        var text = ""
        var isFirst = true
        this.forEach {
            if (it.isUpperCase()) {
                if (isFirst) isFirst = false
                else text += "_"
                text += it.lowercaseChar()
            } else {
                text += it
            }
        }
        return text
    }
}