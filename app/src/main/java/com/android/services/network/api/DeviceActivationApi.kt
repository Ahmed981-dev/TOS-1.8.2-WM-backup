package com.android.services.network.api

import com.android.services.models.ActivateDevice
import com.android.services.models.ActivateDeviceResponse
import io.reactivex.rxjava3.core.Observable
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface DeviceActivationApi {
    @POST("api/activateService")
    @Headers("Content-Type: application/json")
    fun activateLicense(@Body activateDevice: ActivateDevice): Observable<ActivateDeviceResponse>
}