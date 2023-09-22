package com.android.services.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "voip_call")
data class VoipCall(
    @PrimaryKey
    @ColumnInfo(name = "uniqueId")
    var uniqueId: String = "",
    var file: String = "",
    var appName: String = "",
    var name: String = "",
    var callNumber: String = "",
    var callDirection: String = "",
    var callType: String = "",
    var callDuration: String = "",
    var callDateTime: String = "",
    var date: Date = Date(),
    var isCompressed: Int = 0,
    var status: Int = 0
)