package com.idirect.app.utils

import android.app.Application
import com.idirect.app.R
import java.text.SimpleDateFormat
import java.util.*

class TimeUtils {

    companion object{
        fun getTimeZoneOffset(): Int {
            val mCalendar: Calendar = GregorianCalendar()
            val mTimeZone = mCalendar.timeZone
            val mGMTOffset = mTimeZone.rawOffset
            return mGMTOffset
        }

        fun convertTimestampToDate(application: Application, timestamp:Long): String {
            if (timestamp.toString().length == 16) {
                return getDifferentTimeString(application,timestamp / 1000, false)
            } else {
                return getDifferentTimeString(application,timestamp, false)
            }
        }


        fun getDifferentTimeString(application:Application,time: Long, startFromDay: Boolean = true): String {
            val nowTime = System.currentTimeMillis()
            val differentTime = (nowTime - time) / 1000
            val rightNow = (3 * 60)
            val today = 24 * 60 * 60
            val month = 30 * 24 * 60 * 60
            val year = 12 * 30 * 24 * 60 * 60
            if (differentTime < rightNow && !startFromDay) {
                return application.getString(R.string.right_now)
            }
            if (differentTime < today) {
                if (startFromDay) {
                    return application.getString(R.string.today)
                }
                val hour = differentTime / (60 * 60)
                if (hour > 0) {
                    return String.format(application.getString(R.string.hours_ago), hour)
                } else {
                    val min = differentTime / (60)
                    return String.format(application.getString(R.string.min_ago), min)
                }
            }
            if (differentTime < month) {
                val days = (differentTime / (24 * 60 * 60)).toInt()
                if (days == 1) {
                    return application.getString(R.string.yesterday)
                }
                return String.format(application.getString(R.string.days_ago), days)
            }
            if (differentTime < year) {
                val month = differentTime / (30 * 24 * 60 * 60)
                return String.format(application.getString(R.string.month_ago), month)
            }
            val sdf = SimpleDateFormat("yyyy-MM-dd")
            val netDate = Date(time)
            return sdf.format(netDate)
        }

    }
}