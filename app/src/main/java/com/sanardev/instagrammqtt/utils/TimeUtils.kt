package com.sanardev.instagrammqtt.utils

import java.util.*

class TimeUtils {

    companion object{
        fun getTimeZoneOffset(): Int {
            val mCalendar: Calendar = GregorianCalendar()
            val mTimeZone = mCalendar.timeZone
            val mGMTOffset = mTimeZone.rawOffset
            return mGMTOffset
        }
    }
}