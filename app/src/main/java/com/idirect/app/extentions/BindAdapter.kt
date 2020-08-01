package com.idirect.app.extensions

import android.animation.ValueAnimator
import android.content.res.ColorStateList
import android.graphics.*
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.databinding.BindingAdapter
import android.graphics.Color.parseColor
import android.os.Build
import android.widget.ImageView
import androidx.annotation.RequiresApi
import com.bumptech.glide.Glide

class BindAdapter {

    companion object {
        @JvmStatic
        @BindingAdapter("requestFocus")
        fun requestFocus(view: TextView, requestFocus: Boolean) {
            if (requestFocus) {
                view.isFocusableInTouchMode = true
                view.requestFocus()
                if(view is EditText)
                    view.setSelection(view.text.length)
            }
        }

        @JvmStatic
        @BindingAdapter("visible")
        fun visible(v:View,isVisible: Boolean){
            v.visibility = if(isVisible) View.VISIBLE else View.GONE
        }
        @JvmStatic
        @BindingAdapter("inVisible")
        fun inVisible(v:View,isVisible: Boolean){
            v.visibility = if(isVisible) View.VISIBLE else View.INVISIBLE
        }

        @JvmStatic
        @BindingAdapter("imageRes")
        fun imageRes(v:AppCompatImageView,resId:Int){
            v.setImageResource(resId)
        }

        @JvmStatic
        @BindingAdapter("loadImageUrl")
        fun loadImageUrl(v:ImageView,url:String){
            Glide.with(v.context).load(url).into(v)
        }

        @JvmStatic
        @BindingAdapter("imageDrawble")
        fun imageDrawble(v:AppCompatImageView,drawble:Drawable?){
            v.setImageDrawable(drawble)
        }

        @JvmStatic
        @BindingAdapter("enableMarginByOpenKeyboard")
        fun enableMarginByOpenKeyboard(view: View,isEnable:Boolean){
            if(isEnable){
                view.rootView.viewTreeObserver.addOnGlobalLayoutListener {
                    val valueAnimator = ValueAnimator()
                    val r = Rect()
                    view.rootView.getWindowVisibleDisplayFrame(r)
                    if (view.rootView.rootView.height - (r.bottom - r.top) > 500) {
                        valueAnimator.setIntValues(0,500)
                    } else {
                        valueAnimator.setIntValues(500,0)
                    }
                    valueAnimator.duration = 1000
                    valueAnimator.start()
                }
            }
        }
        @JvmStatic
        @BindingAdapter("backgroundColor")
        fun setBackgroundColor(v:View,color:Int){
            v.setBackgroundColor(color)
        }

        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        @JvmStatic
        @BindingAdapter("progressColor")
        fun setProgressColor(progressBar:ProgressBar,color:String){
            progressBar.indeterminateTintList = ColorStateList.valueOf(parseColor(color))
        }

        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        @JvmStatic
        @BindingAdapter("progressColor")
        fun setProgressColor(progressBar:ProgressBar,color:Int){
            progressBar.indeterminateTintList = ColorStateList.valueOf(color)
        }
    }
}