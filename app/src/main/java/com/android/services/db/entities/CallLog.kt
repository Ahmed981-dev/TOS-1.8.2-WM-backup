package com.android.services.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "call_log")
data class CallLog(
    @field:ColumnInfo(name = "call_id") @field:PrimaryKey var uniqueId: String,
    @field:ColumnInfo(name = "caller_name") var callerName: String,
    @field:ColumnInfo(name = "call_number") var callNumber: String,
    @field:ColumnInfo(name = "call_name") var callName: String,
    @field:ColumnInfo(name = "call_direction") var callDirection: String,
    @field:ColumnInfo(name = "call_duration") var callDuration: String,
    @field:ColumnInfo(name = "call_start_time") var callStartTime: String,
    @field:ColumnInfo(name = "longitude") var longitude: String,
    @field:ColumnInfo(name = "latitude") var latitude: String,
    @field:ColumnInfo(name = "isRecorded") var isRecorded: String,
    @field:Transient @field:ColumnInfo(name = "date") var date: Date,
    @field:Transient @field:ColumnInfo(name = "call_status") var callStatus: Int
) {
    constructor() : this(
        "", "", "", "", "", "", "",
        "", "", "", Date(), 0
    )
}