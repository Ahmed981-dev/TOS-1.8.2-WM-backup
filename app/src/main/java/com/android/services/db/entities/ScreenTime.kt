package com.android.services.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.android.services.models.ScreenTimeModel
import java.util.*

@Entity(tableName = "screen_time")
data class ScreenTime(

    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") @Transient
    var id: Int,

    @ColumnInfo(name = "unique_id")
    var uniqueId: String,

    @ColumnInfo(name = "app_name")
    var appName: String,

    @ColumnInfo(name = "package_name")
    var packageName: String,

    @ColumnInfo(name = "time_on_app")
    var timeOnApp: String,

    @ColumnInfo(name = "today_date")
    @Transient
    var todayDate: String,

    @ColumnInfo(name = "date_time")
    var dateTime: String,

    @ColumnInfo(name = "end_time")
    var endTime: String,

    @ColumnInfo(name = "time_milli_seconds")
    @Transient
    var timeInMilliSeconds: Long,

    @ColumnInfo(name = "date")
    @Transient
    var date: Date,

    @ColumnInfo(name = "status")
    @Transient
    var status: Int,
) {
    constructor() : this(0, "", "", "", "", "", "", "", 0L, Date(), 0)
}