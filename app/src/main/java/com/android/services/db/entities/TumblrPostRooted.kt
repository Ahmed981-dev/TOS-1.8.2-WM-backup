package com.android.services.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "tumblr_post_rooted")
data class TumblrPostRooted(
    @PrimaryKey @ColumnInfo(name = "post_id") @Transient
    var postId: String = "",
    var username: String = "",
    var messageDatetime: String = "",
    var postUrl: String = "",
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
