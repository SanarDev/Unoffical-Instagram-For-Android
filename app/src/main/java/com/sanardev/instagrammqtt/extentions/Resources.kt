package com.sanardev.instagrammqtt.extensions

import android.content.res.Resources
import android.graphics.drawable.Drawable
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.res.ResourcesCompat

fun Resources.color(@ColorRes color:Int,theme:Resources.Theme? = null):Int{
    return ResourcesCompat.getColor(this,color,theme)
}

fun Resources.drawble(@DrawableRes drawable:Int,theme:Resources.Theme? = null): Drawable{
    return ResourcesCompat.getDrawable(this,drawable,theme)!!
}



