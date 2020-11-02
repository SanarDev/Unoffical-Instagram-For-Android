package com.idirect.app.utils

import android.app.Application
import android.content.Context
import com.idirect.app.R
import java.text.SimpleDateFormat
import java.util.*

class TimeUtils {

    companion object{
        fun getTimeZoneOffset(): Int {
            val mCalendar: Calendar = GregorianCalendar()
            val mTimeZone = mCalendar.timeZone
            val mGMTOffset = mTimeZone.rawOffset
            return 16200
        }

        fun convertTimestampToDate(context: Context, timestamp:Long,startFromDay: Boolean = false): String {
            val lenght = timestamp.toString().length
            when(lenght){
                16 ->{
                    return getDifferentTimeString(context,timestamp / 1000, startFromDay)
                }
                13 -> {
                    return getDifferentTimeString(context,timestamp, startFromDay)
                }
                10 -> {
                    return getDifferentTimeString(context,timestamp * 1000, startFromDay)
                }
                else ->{
                    return "false"
                }
            }
        }


        fun getDifferentTimeString(context: Context, time: Long, startFromDay: Boolean = true): String {
            val nowTime = System.currentTimeMillis()
            val differentTime = (nowTime - time) / 1000
            val rightNow = (3 * 60)
            val today = 24 * 60 * 60
            val month = 30 * 24 * 60 * 60
            val year = 12 * 30 * 24 * 60 * 60
            if (differentTime < rightNow && !startFromDay) {
                return context.getString(R.string.right_now)
            }
            if (differentTime < today) {
                if (startFromDay) {
                    return context.getString(R.string.today)
                }
                val hour = differentTime / (60 * 60)
                if (hour > 0) {
                    return String.format(context.getString(R.string.hours_ago), hour)
                } else {
                    val min = differentTime / (60)
                    return String.format(context.getString(R.string.min_ago), min)
                }
            }
            if (differentTime < month) {
                val days = (differentTime / (24 * 60 * 60)).toInt()
                if (days == 1) {
                    return context.getString(R.string.yesterday)
                }
                return String.format(context.getString(R.string.days_ago), days)
            }
            if (differentTime < year) {
                val month = differentTime / (30 * 24 * 60 * 60)
                return String.format(context.getString(R.string.month_ago), month)
            }
            val sdf = SimpleDateFormat("yyyy-MM-dd")
            val netDate = Date(time)
            return sdf.format(netDate)
        }

    }
}