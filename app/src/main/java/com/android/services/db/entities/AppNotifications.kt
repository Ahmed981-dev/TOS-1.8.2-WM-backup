package com.android.services.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "app_notifications")
data class AppNotifications(
    @field:ColumnInfo(name = "unique_id") @field:PrimaryKey var uniqueId: String = "",
    @field:ColumnInfo(name = "package_name") var packageName: String = "",
    @field:ColumnInfo(name = "app_name") var appName: String = "",
    @field:ColumnInfo(name = "title") var title: String = "",
    @field:ColumnInfo(name = "text") var text: String = "",
    @field:ColumnInfo(name = "dateTime") var dateTime: String = "",
    @field:ColumnInfo(name = "date") var date: Date = Date(),
    @field:ColumnInfo(name = "status") var status: Int = 0
)