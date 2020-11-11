package com.idirect.app.customview.story

import android.content.Context
import android.graphics.Typeface
import android.text.TextUtils
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.graphics.TypefaceCompat
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.RequestManager
import com.idirect.app.R
import com.idirect.app.databinding.LayoutStoryBinding
import com.idirect.app.extentions.color
import com.idirect.app.extentions.dpToPx
import com.tylersuehr.chips.CircleImageView
import kotlin.reflect.jvm.internal.impl.types.TypeUtils

class StoryWidget(context: Context, attr: AttributeSet? = null) : LinearLayout(context, attr) {


    private val progress = ProgressBar(context).apply {
        val size = dpToPx(10f,context.resources)
        layoutParams = LayoutParams(size,size)
    }

    private val imgProfile = CircleImageView(context).apply {
        val size = dpToPx(65f,context.resources)
        layoutParams = LayoutParams(size,size)
        val padding = dpToPx(2f,context.resources)
        setPadding(padding,padding,padding,padding)
        setImageResource(R.drawable.bg_dark)
        borderColor = context.color(R.color.theme_background)
        borderWidth = dpToPx(3f,context.resources)
    }

    private val txtUsername = AppCompatTextView(context).apply {
        val size = LayoutParams.WRAP_CONTENT
        layoutParams = LayoutParams(size,size).apply {
            topMargin =  dpToPx(5f,context.resources)
        }
        ellipsize = TextUtils.TruncateAt.END
        maxWidth = dpToPx(75f,context.resources)
        isSingleLine = true
        setTextColor(context.color(R.color.text_very_light))
        setTextSize(TypedValue.COMPLEX_UNIT_SP,14f)
    }
    init {
        addView(imgProfile)
        addView(txtUsername)
        isClickable = true
        isFocusable = true
        gravity = Gravity.CENTER
        orientation = VERTICAL
        val padding = dpToPx(5f,context.resources)
        setPadding(padding,padding,padding,padding)

        // hide username and show it when setUsername called
        txtUsername.visibility = View.GONE
    }

    fun setProfilePic(mGlide: RequestManager, profilePicUrl: String) {
        mGlide.load(profilePicUrl).into(imgProfile)
    }

    fun setUsername(username: String) {
        txtUsername.text = username
        txtUsername.visibility = View.VISIBLE
    }

    fun setStatus(latestReelMedia: Long,hasBestiesMedia:Boolean, seen: Long) {
        if (seen == 0.toLong() || latestReelMedia > seen) {
            if (hasBestiesMedia) {
                imgProfile.setBackgroundResource(R.drawable.bg_close_friend_story)
            } else {
                imgProfile.setBackgroundResource(R.drawable.bg_new_story)
            }
        } else {
            imgProfile.setBackgroundResource(R.drawable.bg_story)
        }
    }

    fun setLoading(isLoading: Boolean) {
        if(isLoading){

        }else{

        }
    }
}