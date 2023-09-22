package com.android.services.util

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import java.io.Serializable

inline fun <reified T> AppCompatActivity?.startActivityWithData(
    flags: List<Int>,
    vararg pairs: Pair<String, Any?>
) {
    this ?: return
    this.baseContext.startActivityWithData<T>(flags, *pairs)
}

inline fun <reified T> Fragment?.startActivityWithData(
    flags: List<Int>,
    vararg pairs: Pair<String, Any?>
) {
    this ?: return
    this.activity.startActivityWithData<T>(flags, *pairs)
}

inline fun <reified T> Context?.startActivityWithData(
    flags: List<Int>,
    vararg pairs: Pair<String, Any?>
) {
    this ?: return
    val intent = Intent(this, T::class.java)
    fillTheIntent(intent, pairs)
    flags.forEach { intent.addFlags(it) }
    this.startActivity(intent)
}

fun fillTheIntent(intent: Intent, pairs: Array<out Pair<String, Any?>>) {
    pairs.forEach {
        when (val value = it.second) {
            null -> intent.putExtra(it.first, null as Serializable?)
            is Int -> intent.putExtra(it.first, value)
            is Long -> intent.putExtra(it.first, value)
            is CharSequence -> intent.putExtra(it.first, value)
            is String -> intent.putExtra(it.first, value)
            is Float -> intent.putExtra(it.first, value)
            is Double -> intent.putExtra(it.first, value)
            is Char -> intent.putExtra(it.first, value)
            is Short -> intent.putExtra(it.first, value)
            is Boolean -> intent.putExtra(it.first, value)
            is Serializable -> intent.putExtra(it.first, value)
            is Bundle -> intent.putExtra(it.first, value)
            is Parcelable -> intent.putExtra(it.first, value)
            is Array<*> -> when {
                value.isArrayOf<CharSequence>() -> intent.putExtra(it.first, value)
                value.isArrayOf<String>() -> intent.putExtra(it.first, value)
                value.isArrayOf<Parcelable>() -> intent.putExtra(it.first, value)
            }
            is IntArray -> intent.putExtra(it.first, value)
            is LongArray -> intent.putExtra(it.first, value)
            is FloatArray -> intent.putExtra(it.first, value)
            is DoubleArray -> intent.putExtra(it.first, value)
            is CharArray -> intent.putExtra(it.first, value)
            is ShortArray -> intent.putExtra(it.first, value)
            is BooleanArray -> intent.putExtra(it.first, value)
            else -> {   // ignore

            }
        }
    }
}