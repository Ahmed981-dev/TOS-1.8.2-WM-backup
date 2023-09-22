package com.android.services.interfaces;

import android.content.Context;

import com.android.services.models.AccessibilityEventModel;

public interface OnAccessibilityEvent {
    void onAccessibilityEvent(Context context, AccessibilityEventModel accessibilityEventModel);
}
