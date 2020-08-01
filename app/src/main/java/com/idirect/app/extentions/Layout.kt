package com.idirect.app.extensions

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.constraintlayout.widget.ConstraintLayout

fun LinearLayout(context: Context?, orientation: Int = LinearLayout.VERTICAL): LinearLayout = LinearLayout(context).apply {
    val layout = LinearLayout(context!!)
    layout.orientation = orientation
    return layout
}

fun LinearLayout.addView(v: View, width: Int = ViewGroup.LayoutParams.WRAP_CONTENT, height: Int = ViewGroup.LayoutParams.MATCH_PARENT, weight: Float = -1f, marginTop: Int = 0, marginBottom: Int = 0, marginStart: Int = 0, marginEnd: Int = 0) {
    val param = LinearLayout.LayoutParams(width, height)
    param.topMargin = marginTop
    param.marginStart = marginStart
    param.marginEnd = marginEnd
    param.bottomMargin = marginBottom
    if (weight >= 0)
        param.weight = weight
    this.addView(v, param)
}

fun ConstraintLayout.addView(v: View, width: Int = ViewGroup.LayoutParams.WRAP_CONTENT, height: Int = ViewGroup.LayoutParams.MATCH_PARENT,centerInParent:Boolean = false ,marginTop: Int = 0, marginBottom: Int = 0, marginStart: Int = 0, marginEnd: Int = 0, startToStartOf: Int = -1,endToStartOf:Int = -1, startToEndOf: Int = -1, topToTopOf: Int = -1, topToBottomOf: Int = -1, endToEnd: Int = -1, bottomToBottomOf: Int = -1) {
    val param = ConstraintLayout.LayoutParams(width, height)
    param.topMargin = marginTop
    param.bottomMargin = marginBottom
    param.marginStart = marginStart
    param.marginEnd = marginEnd
    if (endToStartOf >= 0)
        param.endToStart = endToStartOf
    if (topToBottomOf >= 0)
        param.topToBottom = topToBottomOf
    if (startToStartOf >= 0)
        param.startToStart = startToStartOf
    if (endToEnd >= 0)
        param.endToEnd = endToEnd
    if (topToTopOf >= 0)
        param.topToTop = topToTopOf
    if (bottomToBottomOf >= 0)
        param.bottomToBottom = bottomToBottomOf
    if (startToEndOf >= 0)
        param.startToEnd = startToEndOf

    if(centerInParent){
        param.startToStart = 0
        param.endToEnd = 0
        param.topToTop= 0
        param.bottomToBottom= 0
    }
    this.addView(v, param)
}

fun RelativeLayout.addView(v: View, width: Int = ViewGroup.LayoutParams.WRAP_CONTENT, height: Int = ViewGroup.LayoutParams.MATCH_PARENT, alignParent: Int = -1, alignRelative: Int = -1, anchorRelative: Int = -1, marginTop: Int = 0, marginBottom: Int = 0, marginStart: Int = 0, marginEnd: Int = 0) {
    val param = RelativeLayout.LayoutParams(width, height)
    param.topMargin = marginTop
    param.marginStart = marginStart
    param.marginEnd = marginEnd
    param.bottomMargin = marginBottom
    if (alignParent >= 0)
        param.addRule(alignParent)
    if (alignRelative >= 0 && anchorRelative >= 0)
        param.addRule(alignRelative, anchorRelative)

    this.addView(v, param)
}