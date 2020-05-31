package com.sanardev.instagrammqtt.extensions

import android.animation.TimeInterpolator
import android.animation.ValueAnimator
import android.view.View
import android.view.ViewGroup
import android.view.animation.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.transition.AutoTransition
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager

fun ConstraintLayout.changePosition(constraintSet: ConstraintSet){
    val transition = ChangeBounds()
    transition.duration = 300
    transition.interpolator = AccelerateDecelerateInterpolator() as TimeInterpolator?

    TransitionManager.beginDelayedTransition(this,transition)
    constraintSet.applyTo(this)
}
fun View.changeHeight(height:Int,duration:Long = 700){
    val currentHeight = measuredHeight
    val animator = ValueAnimator()
    animator.setIntValues(currentHeight,height)
    animator.addUpdateListener() {
        val size = it.animatedValue as Int
        val param = layoutParams
        param.height = size
        layoutParams = param
    }
    animator.duration = duration
    animator.start()
}

fun View.changeHeightToRealHeight(duration:Long = 700){
    val oldHeight = measuredHeight
    val widthSpec = View.MeasureSpec.makeMeasureSpec(
        width,
        View.MeasureSpec.EXACTLY
    );
    val heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
    measure(widthSpec, heightSpec)

    val currentHeight = measuredHeight
    val animator = ValueAnimator()
    animator.setIntValues(oldHeight ,currentHeight)
    animator.addUpdateListener() {
        val size = it.animatedValue as Int
        val param = layoutParams
        param.height = size
        layoutParams = param
    }
    animator.duration = duration
    animator.start()

}

fun View.changeWidth(width:Int,duration:Long = 700){
    val currentWidth = measuredWidth
    val animator = ValueAnimator()
    animator.setFloatValues(currentWidth.toFloat(),width.toFloat())
    animator.addUpdateListener{
        val size = it.animatedValue as Float
        val param = layoutParams
        param.width = size.toInt()
        layoutParams = param
    }
    animator.duration = duration
    animator.start()
}

fun View.fadeIn(duration: Long = 400){
    visibility = View.VISIBLE
    val fadeIn = AlphaAnimation(0f, 1f)
    fadeIn.interpolator = DecelerateInterpolator() //add this
    fadeIn.duration = duration

    val animation = AnimationSet(false) //change to false
    animation.addAnimation(fadeIn)
//    this.animation = animation
    startAnimation(animation)
}

fun View.fadeOut(duration: Long = 100){
    val fadeOut = AlphaAnimation(1f, 0f)
    fadeOut.interpolator = AccelerateInterpolator() //and this
    fadeOut.duration = duration
    handler.postDelayed({
        visibility = View.GONE
    },duration)

    val animation = AnimationSet(false) //change to false
    animation.addAnimation(fadeOut)
    startAnimation(animation)
}
