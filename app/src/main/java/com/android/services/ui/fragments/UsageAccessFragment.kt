package com.android.services.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import com.android.services.R
import com.android.services.databinding.FragmentDeviceAdminAppsBinding
import com.android.services.databinding.FragmentUsageAccessBinding
import com.android.services.enums.PermissionScreens
import com.android.services.interfaces.CustomDialogListener
import com.android.services.util.AppUtils
import com.android.services.viewModel.ManualPermissionViewModel

class UsageAccessFragment : BaseFragment() {
    lateinit var binding: FragmentUsageAccessBinding
    private val manualPermissionViewModel: ManualPermissionViewModel by activityViewModels()
    override fun initViews(view: View) {
        binding.permissionButton.setOnClickListener {
            AppUtils.requestUsageAccessPermission(requireContext())
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
                        AppUtils.skipThePermission(PermissionScreens.USAGE_ACCESS_PERMISSION)
                    }

                    override fun onCancel() {

                    }
                }
            )
        }
    }

    override fun createView(inflater: LayoutInflater, container: ViewGroup): View? {
        binding = DataBindingUtil.inflate<FragmentUsageAccessBinding>(
            inflater, R.layout.fragment_usage_access, container, false
        ).apply {
            lifecycleOwner = viewLifecycleOwner
            viewModel = manualPermissionViewModel
        }
        return binding.root
    }

    override fun onCreateFragment() {
    }
    companion object {
        /** Creates an new instance of Usage Access Fragment **/
        fun newInstance(): UsageAccessFragment {
            val args = Bundle()
            val fragment = UsageAccessFragment()
            fragment.arguments = args
            return fragment
        }
    }
}