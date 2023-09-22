package com.android.services.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "appointment_log")
data class AppointmentLog(

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "unique_id")
    @Transient
    var uniqueId: Int,

    @ColumnInfo(name = "appointmentId")
    @Transient
    var appointmentId: String,

    @ColumnInfo(name = "appointmentName")
    var appointmentName: String,

    @ColumnInfo(name = "appointmentTitle")
    var appointmentTitle: String,

    @ColumnInfo(name = "appointmentDescription")
    var appointmentDescription: String,

    @ColumnInfo(name = "appointmentStartTime")
    var appointmentStartTime: String,

    @ColumnInfo(name = "appointmentEndTime")
    var appointmentEndTime: String,

    @ColumnInfo(name = "appointmentLocation")
    var appointmentLocation: String,

    @ColumnInfo(name = "appointmentTimeZone")
    var appointmentTimeZone: String,

    @ColumnInfo(name = "allDayEvent")
    var allDayEvent: String,

    @ColumnInfo(name = "appointmentStatus")
    var appointmentStatus: String,

    @ColumnInfo(name = "appointmentReminder")
    var appointmentReminder: String,

    @ColumnInfo(name = "sentStatus")
    @Transient
    var sentStatus: Int
) {
    constructor() : this(
        0, "", "", "",
        "", "", "", "", "",
        "", "", "", 0
    )
}