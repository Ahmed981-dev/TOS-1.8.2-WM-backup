package com.android.services.sealed

import com.android.services.models.ActivateDeviceResponse


sealed class NetworkState
data class NetworkError(val message: String) : NetworkState()
object Initialized : NetworkState()
object Loading : NetworkState()
data class ActivationSuccess(val activateDeviceResponse: ActivateDeviceResponse) : NetworkState()