package com.android.services.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "instagram_feed_rooted")
data class InstagramFeedRooted(
    @PrimaryKey @ColumnInfo(name = "message_id")
    var messageId: String = "",
    var username: String = "",
    var userProfilePic: String = "",
    var following: String = "",
    var date: String = "",
    var feedUrl: String = "",
    var feedText: String = "",
    var feedLikes: String = "",
    var feedLiked: String = "",
    var feedLikersList: String = "",
    var feedCommentersList: String = "",
    var isImage: String = "",
    @ColumnInfo(name = "feedDate")
    @Transient
    var feedDate: Date = Date(),
    @ColumnInfo(name = "timeStamp")
    @Transient
    var timeStamp: Long = 0L,
    @ColumnInfo(name = "status")
    @Transient
    var status: Int = 0
)
