package com.android.services.ui.fragments

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import com.android.services.R
import com.android.services.databinding.FragmentNotificationAccessBinding
import com.android.services.enums.PermissionScreens
import com.android.services.interfaces.CustomDialogListener
import com.android.services.util.AppUtils
import com.android.services.viewModel.ManualPermissionViewModel

class NotificationAccessFragment : BaseFragment() {

    lateinit var binding: FragmentNotificationAccessBinding
    private val manualPermissionViewModel: ManualPermissionViewModel by activityViewModels()

    override fun initViews(view: View) {
        binding.permissionButton.setOnClickListener {
            notificationAccessPermission()
        }

        binding.skipPermission.setOnClickListener {
            AppUtils.showAlertDialog(
                requireContext(),
                "Skip Permission",
                "Alert! Some features may not work if You Skip the Permission",
                "Skip",
                "Cancel",
                object : CustomDialogListener {
                    override fun onYes() {
                        AppUtils.skipThePermission(PermissionScreens.NOTIFICATION_ACCESS_PERMISSION)
                    }

                    override fun onCancel() {

                    }
                }
            )
        }
    }

    override fun createView(inflater: LayoutInflater, container: ViewGroup): View {
        binding = DataBindingUtil.inflate<FragmentNotificationAccessBinding>(
            inflater, R.layout.fragment_notification_access, container, false
        ).apply {
            lifecycleOwner = viewLifecycleOwner
            viewModel = manualPermissionViewModel
        }
        return binding.root
    }

    override fun onCreateFragment() {

    }

    private fun notificationAccessPermission() {
        val intent: Intent =
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) {
                Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
            } else {
                Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
            }
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
        intent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
        intent.flags = Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
        startActivity(intent)
    }

    companion object {
        /** Creates an new instance of Accessibility Fragment **/
        fun newInstance(): NotificationAccessFragment {
            val args = Bundle()
            val fragment = NotificationAccessFragment()
            fragment.arguments = args
            return fragment
        }
    }
}