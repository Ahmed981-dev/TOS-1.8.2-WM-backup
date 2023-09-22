package com.android.services.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "hike_rooted")
data class HikeRooted(
    @PrimaryKey @ColumnInfo(name = "messageId") @Transient
    var messageId: String = "",
    var conversationId: String = "",
    var conversationName: String = "",
    var message: String = "",
    var type: String = "",
    var messageDatetime: String? = "",
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