package com.sanardev.instagrammqtt.extensions

import android.animation.ValueAnimator
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat


fun View.startWidthAnim(startSize: Int, endSize: Int, duration: Int = 300) {
    val valueAnimator = ValueAnimator.ofInt(startSize, endSize)
    valueAnimator.addUpdateListener { valueAnimator ->
        layoutParams.width = valueAnimator.animatedValue as Int
        requestLayout()
    }
    valueAnimator.duration = duration.toLong()
    valueAnimator.start()
}

fun visible(vararg views: View) {
    for (view in views) {
        view.visibility = View.VISIBLE
    }
}

fun gone(vararg views: View) {
    for (view in views) {
        view.visibility = View.GONE
    }
}

fun invisible(vararg views: View) {
    for (view in views) {
        view.visibility = View.INVISIBLE
    }
}

fun TextView.setTextViewDrawableColor(color: Int) {
    for (drawable in compoundDrawables) {
        if (drawable != null) {
            drawable.colorFilter = PorterDuffColorFilter(
                color
                , PorterDuff.Mode.SRC_IN
            )
        }
    }
}