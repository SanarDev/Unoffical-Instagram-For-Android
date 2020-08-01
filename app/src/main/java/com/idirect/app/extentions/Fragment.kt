package com.idirect.app.extensions

import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

fun Fragment.color(color:Int):Int{
    return ContextCompat.getColor(context!!,color)
}

fun Fragment.drawable(drawable:Int):Drawable{
    return ContextCompat.getDrawable(context!!,drawable)!!
}