package com.hsuanparty.unbox_parity.utils.networkChecker

import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.lifecycle.MutableLiveData

/**
 * Author: Tsung Hsuan, Lai
 * Created on: 2019/3/27
 * Description: record the latest network state
 */
class NetworkState {

    companion object {
        var isNetworkConnected = MutableLiveData<Boolean>()

        private fun updateNetworkState(context: Context) {
            var isConnected = false

            val localPackageManager = context.packageManager
            if (localPackageManager.checkPermission("android.permission.ACCESS_NETWORK_STATE",
                    context.packageName) != PackageManager.PERMISSION_GRANTED) {
                return
            }

            val localConnectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networks = localConnectivityManager.allNetworks
            for (network in networks) {
                val capabilities = localConnectivityManager.getNetworkCapabilities(network)
                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN) ||
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                    isConnected = true
                    break
                }
            }

            isNetworkConnected.value = isConnected
        }

        fun refresh(context: Context) {
            updateNetworkState(context)
        }
    }
}