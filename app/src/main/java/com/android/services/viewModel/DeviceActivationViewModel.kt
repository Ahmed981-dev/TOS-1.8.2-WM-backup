package com.android.services.viewModel

import android.content.Context
import android.net.wifi.WifiManager
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.android.services.models.ActivateDevice
import com.android.services.network.api.TOSApi
import com.android.services.sealed.*
import com.android.services.util.AppConstants
import com.android.services.util.AppUtils
import com.android.services.util.DeviceInformationUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject


@HiltViewModel
class DeviceActivationViewModel @Inject constructor(
    @ApplicationContext application: Context,
) : ViewModel() {

    @Inject
    lateinit var tosApi: TOSApi
//    @Inject
//    lateinit var deviceActivationApi: DeviceActivationApi
    private val compositeDisposable = CompositeDisposable()

    /** Notifies the current Network State [NetworkState] is a sealed class which defines all the States for a Network Operation **/
    private val _networkState: MutableLiveData<NetworkState> =
        MutableLiveData<NetworkState>(Initialized)
    val networkState: LiveData<NetworkState> = _networkState

    /** Live Data Observer for Activation Code EditText **/
    val activationCode = MutableLiveData<String>("")

    /**
     * Observer Data changes for [ActivateDevice] and Notifies the Live Data by [activateDevice] Observer
     */
    private val _activateDevice = MutableLiveData<ActivateDevice>()
    val activateDevice: LiveData<ActivateDevice> = _activateDevice

    /**
     * This method handles the Device Activation Request, It activates the Service with the Code Provided
     * [activateDevice] Object contains all the device info Required to activate The Service
     */
    fun activateDevice(activateDevice: ActivateDevice) {
        val disposable = tosApi.activateLicense(activateDevice)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe {
                _networkState.value = Loading
            }.subscribe(
                {
                    _networkState.value = ActivationSuccess(it)
                }, {
                    _networkState.value = NetworkError(it.message ?: "Null Throwable")
                }
            )
        compositeDisposable.add(disposable)
    }

    /**
     * Initiates the Device Activation Process
     */
    fun onDeviceActivation(application: Context) {
        val identifier= AppUtils.createUniqueIdentifier()
        AppConstants.deviceIdentifier=identifier
        val activateDevice = ActivateDevice(
            DeviceInformationUtil.deviceModel,
            DeviceInformationUtil.deviceOS,
            DeviceInformationUtil.getSimId(application),
            DeviceInformationUtil.getIMEINumber(application),
            activationCode.value!!,
            "android",
            "Version = ${DeviceInformationUtil.versionName}, Code = ${DeviceInformationUtil.versionCode}",
            DeviceInformationUtil.getNetworkOperator(application),
            DeviceInformationUtil.getMccMnc(application),
            deviceIdentifier = identifier
        )
        _activateDevice.value = activateDevice

    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}