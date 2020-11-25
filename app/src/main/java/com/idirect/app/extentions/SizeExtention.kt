package com.idirect.app.extentions

import android.content.Context
import android.content.res.Resources
import android.util.TypedValue
import java.io.File

object SizeExtention {

    val File.size get() = if (!exists()) 0.0 else length().toDouble()
    val File.sizeInKb get() = size / 1024
    val File.sizeInMb get() = sizeInKb / 1024
    val File.sizeInGb get() = sizeInMb / 1024
    val File.sizeInTb get() = sizeInGb / 1024

    fun Resources.dpToPx(dp: Float): Int {
        val px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, displayMetrics)
        return px.toInt()
    }
    fun Context.dpToPx(dp: Float): Int {
        val px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics)
        return px.toInt()
    }
}