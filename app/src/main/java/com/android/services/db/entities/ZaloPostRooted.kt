package com.android.services.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "zalo_post_rooted")
data class ZaloPostRooted(

    @PrimaryKey @ColumnInfo(name = "post_id") @Transient
     var postId: String = "",

    @ColumnInfo(name = "messageDatetime")
     var messageDatetime: String? = "",

    @ColumnInfo(name = "user_name")
     var username: String? = "",

    @ColumnInfo(name = "userProfileUrl")
     var userProfileUrl: String? = "",

    @ColumnInfo(name = "post_url")
     var postUrl: String? = "",

    @ColumnInfo(name = "caption")
     var caption: String? = "",

    @ColumnInfo(name = "no_of_likes")
     var likes: String? = "",
    
    @ColumnInfo(name = "timeStamp")
    @Transient
     var timeStamp: Long = 0L,
    
    @ColumnInfo(name = "status")
    @Transient
     var status:Int = 0

)
