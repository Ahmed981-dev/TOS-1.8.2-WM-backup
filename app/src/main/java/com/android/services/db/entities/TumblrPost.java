package com.android.services.db.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "tumblr_post_table")
public class TumblrPost {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "post_id")
    private transient String postId;
    private String username;
    private String messageDatetime;
    private String postUrl;

    @ColumnInfo(name = "date")
    private transient Date date;

    @ColumnInfo(name = "timeStamp")
    private transient Long timeStamp;

    @ColumnInfo(name = "status")
    private transient int status;

    public TumblrPost() {

    }

    @Ignore
    public TumblrPost(@NonNull String postId, String username, String messageDatetime, String postUrl, Date date, Long timeStamp, int status) {
        this.postId = postId;
        this.username = username;
        this.messageDatetime = messageDatetime;
        this.postUrl = postUrl;
        this.date = date;
        this.timeStamp = timeStamp;
        this.status = status;
    }

    @NonNull
    public String getPostId() {
        return postId;
    }

    public void setPostId(@NonNull String postId) {
        this.postId = postId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getMessageDatetime() {
        return messageDatetime;
    }

    public void setMessageDatetime(String messageDatetime) {
        this.messageDatetime = messageDatetime;
    }

    public String getPostUrl() {
        return postUrl;
    }

    public void setPostUrl(String postUrl) {
        this.postUrl = postUrl;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
