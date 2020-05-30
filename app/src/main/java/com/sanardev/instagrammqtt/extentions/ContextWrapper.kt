package com.sanardev.instagrammqtt.extensions

import android.content.Context
import android.content.ContextWrapper
import android.net.ConnectivityManager
import android.view.View
import androidx.core.content.ContextCompat

fun ContextWrapper.isNetworkConnected(): Boolean {
    val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val connection = cm.activeNetworkInfo
    return connection != null && connection.isConnectedOrConnecting
}
fun Context.color(color:Int):Int{
    return ContextCompat.getColor(this,color)
}