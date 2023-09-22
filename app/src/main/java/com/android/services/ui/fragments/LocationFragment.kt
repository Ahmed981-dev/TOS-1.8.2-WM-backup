package com.android.services.ui.fragments

import android.Manifest
import android.annotation.TargetApi
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import com.android.services.R
import com.android.services.databinding.FragmentLocationBinding
import com.android.services.enums.PermissionScreens
import com.android.services.interfaces.CustomDialogListener
import com.android.services.ui.activities.ManualPermissionActivity
import com.android.services.util.AppConstants
import com.android.services.util.AppUtils
import com.android.services.util.logVerbose
import com.android.services.viewModel.ManualPermissionViewModel

class LocationFragment : BaseFragment() {

    lateinit var binding: FragmentLocationBinding
    private val manualPermissionViewModel: ManualPermissionViewModel by activityViewModels()

    override fun onCreateFragment() {

    }

    override fun initViews(view: View) {
        binding.permissionButton.setOnClickListener {
            permissionsResultLauncher.launch(permissions)
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
                        AppUtils.skipThePermission(PermissionScreens.LOCATION_PERMISSION)
                    }

                    override fun onCancel() {

                    }
                }
            )
        }
    }

    override fun createView(inflater: LayoutInflater, container: ViewGroup): View {
        binding = DataBindingUtil.inflate<FragmentLocationBinding>(
            inflater, R.layout.fragment_location, container, false
        ).apply {
            lifecycleOwner = viewLifecycleOwner
            viewModel = manualPermissionViewModel
        }
        return binding.root
    }

    private val permissionsResultLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            // Handle Permission granted/rejected
            var isPermissionDenied = false
            permissions.entries.forEachIndexed { _, entry ->
                val isGranted = entry.value
                if (!isGranted) {
                    isPermissionDenied = true
                }
            }
            if (!isPermissionDenied) {
                logVerbose("${ManualPermissionActivity.TAG} Location permission Granted")
            } else {
                logVerbose("${ManualPermissionActivity.TAG} Missing Location Permissions")
                if (AppConstants.locationPermissionCounter == 2) {
                    val intent = Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.fromParts("package", requireActivity().packageName, null)
                    )
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    startActivity(intent)
                } else {
                    AppConstants.locationPermissionCounter += 1
                }
            }
        }

    companion object {

        @TargetApi(Build.VERSION_CODES.M)
        var permissions = arrayOf(
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
        )

        /** Creates an new instance of Accessibility Fragment **/
        fun newInstance(): LocationFragment {
            val args = Bundle()
            val fragment = LocationFragment()
            fragment.arguments = args
            return fragment
        }
    }
}