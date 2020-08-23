package com.idirect.app.utils

import android.content.Context
import com.idirect.app.R

class TextUtil {

    companion object{
        fun getStringNumber(context:Context,number: Int): String {
            var strNumber = ""
            when(number){
                in 0..9999 ->{
                    strNumber = number.toString()
                }
                in 10000..999999 ->{
                    val splitedNumber = "%.1f".format((number.toFloat() / 1000))

                    strNumber = String.format(
                        context.getString(R.string.k),
                        splitedNumber
                    )
                }
                else ->{
                    val splitedNumber = "%.1f".format((number.toFloat() / 1000000))
                    strNumber = String.format(
                        context.getString(R.string.m),
                        splitedNumber
                    )
                }
            }
            return strNumber
        }
    }
}