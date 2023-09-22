package com.android.services.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "snap_chat_event")
data class SnapChatEvent(
    @PrimaryKey @ColumnInfo(name = "file") var file: String = "",
    @ColumnInfo(name = "name") var name: String = "",
    @ColumnInfo(name = "type") var type: String = "",
    @ColumnInfo(name = "dateTaken") var dateTaken: String = "",
    @field:Transient @field:ColumnInfo(name = "date") var date: Date = Date(),
    @field:Transient @ColumnInfo(name = "status") var status: Int = 0
)
