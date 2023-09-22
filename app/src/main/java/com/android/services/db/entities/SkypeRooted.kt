package com.android.services.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "skype_rooted")
data class SkypeRooted(
    @field:ColumnInfo(name = "message_id") @field:PrimaryKey var messageId: String,
    @field:ColumnInfo(name = "conversation_id") var conversationId: String,
    @field:ColumnInfo(name = "conversation_name") var conversationName: String,
    @field:ColumnInfo(name = "message") var message: String,
    @field:ColumnInfo(name = "type") var type: String,
    @field:ColumnInfo(name = "message_datetime") var messageDatetime: String,
    @field:ColumnInfo(name = "sender_number") var senderId: String,
    @field:ColumnInfo(name = "sender_id") var senderName: String,
    @field:ColumnInfo(name = "is_call") var call: Boolean,
    @field:ColumnInfo(name = "call_duration") var duration: String,
    @field:Transient @field:ColumnInfo(name = "date") var date: Date,
    @field:Transient @field:ColumnInfo(name = "timeStamp") var timeStamp: Long,
    @field:Transient @field:ColumnInfo(name = "status") var status: Int
) {
    constructor() : this("", "", "", "", "", "", "", "", false, "", Date(), 0L, 0)
}