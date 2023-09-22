package com.android.services.util

import androidx.room.TypeConverter
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

object Converters {

    private val dateFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

    @JvmStatic
    @TypeConverter
    fun fromTimestamp(value: String?): Date? {
        return if (value != null) {
            try {
                return dateFormat.parse(value)
            } catch (e: ParseException) {
                e.printStackTrace()
            }
            null
        } else {
            null
        }
    }

    @JvmStatic
    @TypeConverter
    fun dateToTimestamp(value: Date?): String? {
        return if (value == null) null else dateFormat.format(value)
    }
}