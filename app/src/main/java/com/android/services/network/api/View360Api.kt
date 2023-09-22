package com.android.services.network.api

import com.android.services.models.View360User
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface View360Api {
    @POST("sipactivate")
    @Headers("Content-Type: application/json")
    fun activateView360User(@Body view360User: View360User): Call<ResponseBody>
}