package com.idirect.app.utils

import android.content.res.Resources

class DisplayUtils {

    companion object {
        fun getScreenWidth(): Int {
            return Resources.getSystem().getDisplayMetrics().widthPixels
        }

        fun getScreenHeight(): Int {
            return Resources.getSystem().getDisplayMetrics().heightPixels
        }
    }
}