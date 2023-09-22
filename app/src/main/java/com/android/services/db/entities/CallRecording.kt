package com.android.services.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "call_recording")
data class CallRecording(

    @PrimaryKey @ColumnInfo(name = "file")
    var file: String = "",
    var callerName: String = "",
    var callName: String = "",
    var callDuration: String = "",
    var callStartTime: String = "",
    var callDirection: String = "",
    var callNumber: String = "",
    var isCompressed: Int = 0,
    var status: Int = 0
)