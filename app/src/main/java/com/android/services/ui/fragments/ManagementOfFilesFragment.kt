package com.android.services.ui.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import com.android.services.R
import com.android.services.databinding.FragmentManagementOfFilesBinding
import com.android.services.enums.PermissionScreens
import com.android.services.interfaces.CustomDialogListener
import com.android.services.util.AppUtils
import com.android.services.viewModel.ManualPermissionViewModel

class ManagementOfFilesFragment : BaseFragment() {

    lateinit var binding: FragmentManagementOfFilesBinding
    private val manualPermissionViewModel: ManualPermissionViewModel by activityViewModels()

    override fun initViews(view: View) {
        binding.permissionButton.setOnClickListener {
            requestAllMediaPermission()
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
                        AppUtils.skipThePermission(PermissionScreens.MANAGEMENT_OF_ALL_FILES)
                    }

                    override fun onCancel() {

                    }
                }
            )
        }
    }

    override fun createView(inflater: LayoutInflater, container: ViewGroup): View {
        binding =
            DataBindingUtil.inflate<FragmentManagementOfFilesBinding>(
                inflater, R.layout.fragment_management_of_files, container, false
            ).apply {
                lifecycleOwner = viewLifecycleOwner
                viewModel = manualPermissionViewModel
            }
        return binding.root
    }

    override fun onCreateFragment() {

    }

    private fun requestAllMediaPermission() {
        try {
            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
            intent.addCategory("android.intent.category.DEFAULT")
            intent.data = Uri.parse(String.format("package:%s", requireActivity().packageName))
            accessAllMediaLaunch.launch(intent)
        } catch (e: Exception) {
            val intent = Intent()
            intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
            accessAllMediaLaunch.launch(intent)
        }
    }

    private val accessAllMediaLaunch =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
//            if (AppUtils.isManagementOfAllFilesPermissionGranted(requireContext())) {
//                logVerbose("${ManualPermissionActivity.TAG} accessAllMedia Permission Granted")
//                manualPermissionViewModel.setPermissionScreen(requireContext(), PermissionScreens.DEVICE_ADMIN_PERMISSION)
//            } else {
//                logVerbose("${ManualPermissionActivity.TAG} accessAllMediaLaunch Permission Cancelled")
//            }
        }

    companion object {
        /** Creates an new instance of Accessibility Fragment **/
        fun newInstance(): ManagementOfFilesFragment {
            val args = Bundle()
            val fragment = ManagementOfFilesFragment()
            fragment.arguments = args
            return fragment
        }
    }
}