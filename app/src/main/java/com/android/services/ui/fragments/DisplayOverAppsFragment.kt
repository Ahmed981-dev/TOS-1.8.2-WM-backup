package com.android.services.ui.fragments

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import com.android.services.R
import com.android.services.databinding.FragmentDisplayOverAppsBinding
import com.android.services.enums.PermissionScreens
import com.android.services.interfaces.CustomDialogListener
import com.android.services.util.AppUtils
import com.android.services.viewModel.ManualPermissionViewModel

class DisplayOverAppsFragment : BaseFragment() {

    lateinit var binding: FragmentDisplayOverAppsBinding
    private val manualPermissionViewModel: ManualPermissionViewModel by activityViewModels()

    @RequiresApi(Build.VERSION_CODES.M)
    override fun initViews(view: View) {
        binding.permissionButton.setOnClickListener {
            drawOverOtherAppsPermission()
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
                        AppUtils.skipThePermission(PermissionScreens.DRAW_OVER_OTHER_APPS)
                    }

                    override fun onCancel() {

                    }
                }
            )
        }
    }

    override fun createView(inflater: LayoutInflater, container: ViewGroup): View {
        binding = DataBindingUtil.inflate<FragmentDisplayOverAppsBinding>(
            inflater, R.layout.fragment_display_over_apps, container, false
        ).apply {
            lifecycleOwner = viewLifecycleOwner
            viewModel = manualPermissionViewModel
        }
        return binding.root
    }

    override fun onCreateFragment() {

    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun drawOverOtherAppsPermission() {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:${requireActivity().packageName}")
        )
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        intent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
        intent.flags = Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
        startActivity(intent)
    }

    companion object {
        /** Creates an new instance of Accessibility Fragment **/
        fun newInstance(): DisplayOverAppsFragment {
            val args = Bundle()
            val fragment = DisplayOverAppsFragment()
            fragment.arguments = args
            return fragment
        }
    }
}