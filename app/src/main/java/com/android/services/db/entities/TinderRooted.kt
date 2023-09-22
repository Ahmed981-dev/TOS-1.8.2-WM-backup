package com.android.services.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "tinder_rooted")
data class TinderRooted(
    @PrimaryKey @ColumnInfo(name = "message_id") @Transient
    var messageId: String = "",
    @ColumnInfo(name = "conversation_id")
    var conversationId: String = "",
    @ColumnInfo(name = "user_name")
    var userName: String = "",
    @ColumnInfo(name = "message_text")
    var messageText: String = "",
    @ColumnInfo(name = "type")
    var type: String = "",
    @ColumnInfo(name = "date")
    var date: String = "",
    @ColumnInfo(name = "timeStamp")
    @Transient
    var timeStamp: Long = 0L,
    @ColumnInfo(name = "message_date")
    @Transient
    var messageDate: Date = Date(),
    @ColumnInfo(name = "status")
    @Transient
    var status: Int = 0,
)