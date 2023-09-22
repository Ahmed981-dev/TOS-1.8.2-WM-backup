package com.android.services.db.entities

import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import java.util.*

@Entity(tableName = "voice_message_table")
data class VoiceMessage(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    @Transient
    var id: Int = 0,
    var file: String? = null,
    var fileName: String? = null,
    var appName: String? = null,
    var dateTaken: String? = null,
    var timeStamp: Long? = null,
    var date: Date? = null,
    @ColumnInfo(name = "status")
    @Transient
    var status: Int = 0
)