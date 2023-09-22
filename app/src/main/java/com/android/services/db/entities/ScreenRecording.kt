package com.android.services.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "screen_recording")
data class ScreenRecording(
    @PrimaryKey @ColumnInfo(name = "file")
    var file: String = "",
    @ColumnInfo(name = "name")
    var name: String = "",
    @ColumnInfo(name = "startDatetime")
    var startDatetime: String = "",
    @ColumnInfo(name = "appName")
    var appName: String = "",
    @ColumnInfo(name = "appPackageName")
    var appPackageName: String = "",
    @ColumnInfo(name = "pushId")
    var pushId: String = "",
    @ColumnInfo(name = "pushStatus")
    var pushStatus: String = "",
    @ColumnInfo(name = "isCompressed")
    var isCompressed: Boolean = false,
    @ColumnInfo(name = "status")
    var status: Int = 0
)