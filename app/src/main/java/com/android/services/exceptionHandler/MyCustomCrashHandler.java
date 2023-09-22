package com.android.services.exceptionHandler;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.services.util.FirebasePushUtils;

public class MyCustomCrashHandler  implements Thread.UncaughtExceptionHandler {
    private Context context;
    @Nullable
    private final Thread.UncaughtExceptionHandler defaultHandler;
    private Activity activity;

    public MyCustomCrashHandler(@Nullable Thread.UncaughtExceptionHandler defaultHandler,Context context)
    {
        this.defaultHandler = defaultHandler;
        this.context=context;

    }

    @Override
    public void uncaughtException(@NonNull Thread thread, @NonNull Throwable ex) {
        // We are now safely being called after Crashlytics does its own thing.
        // Whoever is the last handler on Thread.getDefaultUncaughtExceptionHandler() will execute first on uncaught exceptions.
        // Firebase Crashlytics will handle its own behavior first before calling ours in its own 'finally' block.
        // You can choose to propagate upwards (it will kill the app by default) or do your own thing and propagate if needed.

        try {
            //do your own thing.
            FirebasePushUtils.INSTANCE.hardRestartApp(context,false);
            System.exit(1);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (defaultHandler != null) {
                defaultHandler.uncaughtException(thread, ex);
                // propagate upwards. With this workaround (and also without any other similar UncaughtExceptionHandler based on ContentProvider),
                // defaultHandler should now be an instance of com.android.internal.os.RuntimeInit.KillApplicationHandler
                // hence properly killing the app via framework calls.
            }
        }
    }
}
