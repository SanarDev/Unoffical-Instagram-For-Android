package com.sanardev.instagrammqtt.extentions

import android.app.ActivityManager
import android.content.Context
import android.content.ContextWrapper
import android.net.ConnectivityManager
import android.view.View
import androidx.core.content.ContextCompat

fun Context.color(color:Int):Int{
    return ContextCompat.getColor(this,color)
}

@Suppress("DEPRECATION") // Deprecated for third party Services.
fun <T> Context.isServiceRunning(service: Class<T>) =
    (getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager)
        .getRunningServices(Integer.MAX_VALUE)
        .any { it.service.className == service.name }