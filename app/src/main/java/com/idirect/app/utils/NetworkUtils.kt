package com.idirect.app.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build

class NetworkUtils {

    enum class NetworkType {
        NONE,
        MOBILE_DATA,
        WIFI,
        VPN
    }
    companion object {
        fun getConnectionType(context: Context): NetworkType {
            var result = NetworkType.NONE // Returns connection type. 0: none; 1: mobile data; 2: wifi
            val cm =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (cm != null) {
                    val capabilities =
                        cm.getNetworkCapabilities(cm.activeNetwork)
                    if (capabilities != null) {
                        if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                            result = NetworkType.WIFI
                        } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                            result = NetworkType.MOBILE_DATA
                        } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN)) {
                            result = NetworkType.VPN
                        }
                    }
                }
            } else {
                if (cm != null) {
                    val activeNetwork = cm.activeNetworkInfo
                    if (activeNetwork != null) {
                        // connected to the internet
                        if (activeNetwork.type == ConnectivityManager.TYPE_WIFI) {
                            result = NetworkType.WIFI
                        } else if (activeNetwork.type == ConnectivityManager.TYPE_MOBILE) {
                            result = NetworkType.MOBILE_DATA
                        } else if (activeNetwork.type == ConnectivityManager.TYPE_VPN) {
                            result = NetworkType.VPN
                        }
                    }
                }
            }
            return result
        }
    }
}