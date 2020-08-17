package com.idirect.app.utils

import android.content.Context
import android.content.res.Resources
import android.graphics.Rect
import android.view.View
import android.view.Window

class DisplayUtils {

    companion object {
        fun getScreenWidth(): Int {
            return Resources.getSystem().getDisplayMetrics().widthPixels
        }

        fun getScreenHeight(): Int {
            return Resources.getSystem().getDisplayMetrics().heightPixels
        }
        fun getStatusBarHeight(window: Window): Int {
            val rectangle = Rect()
            window.decorView.getWindowVisibleDisplayFrame(rectangle)
            val statusBarHeight: Int = rectangle.top
            val contentViewTop: Int = window.findViewById<View>(Window.ID_ANDROID_CONTENT).top
            val titleBarHeight = contentViewTop - statusBarHeight
            return titleBarHeight
        }
    }
}