package com.idirect.app.extensions

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.Rect
import android.os.Build
import android.text.Html
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.URLSpan
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.app.ActivityCompat.postponeEnterTransition
import androidx.core.app.ActivityCompat.startPostponedEnterTransition
import androidx.fragment.app.Fragment
import com.idirect.app.utils.URLSpanNoUnderline
import com.idirect.app.extentions.openUrl


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

fun TextView.stripUnderlines() {
    val s: Spannable = SpannableString(this.text)
    val spans = s.getSpans(0, s.length, URLSpan::class.java)
    for (span in spans) {
        val start = s.getSpanStart(span)
        val end = s.getSpanEnd(span)
        s.removeSpan(span)
        s.setSpan(URLSpanNoUnderline(span.url), start, end, 0)
    }
    this.text = s
}

const val REGEX_FIND_URL = "(?:(?:https?|ftp):\\/\\/)?[\\w/\\-?=%.]+\\.[\\w/\\-?=%.]+"
fun TextView.setTextLinkHTML(
    context: Context,
    html: String,
    haveUnderlineForLink: Boolean = false
) {
    val htmlText = html.toLowerCase().replace("\n","<br/>").replace(
        REGEX_FIND_URL.toRegex(),
        "<a href=\"$0\">$0</a>"
    );
    setTextHTML(context,htmlText,haveUnderlineForLink)
}

fun TextView.setTextHTML(
    context: Context,
    html: String,
    haveUnderlineForLink: Boolean = false
) {
    val sequence =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY)
        } else {
            Html.fromHtml(html)
        }
    val strBuilder = SpannableStringBuilder(sequence)
    val urls =
        strBuilder.getSpans(0, sequence.length, URLSpan::class.java)
    for (span in urls) {
        makeLinkClickable(context, strBuilder, span,haveUnderlineForLink)
    }
    this.text = strBuilder
    this.movementMethod = LinkMovementMethod.getInstance()
}

private fun makeLinkClickable(
    context: Context,
    strBuilder: SpannableStringBuilder,
    span: URLSpan,
    haveUnderlineForLink:Boolean = false
) {
    val start = strBuilder.getSpanStart(span)
    val end = strBuilder.getSpanEnd(span)
    val flags = strBuilder.getSpanFlags(span)
    val clickable: ClickableSpan = object : ClickableSpan() {
        override fun onClick(view: View) {
            openUrl(context, span.url)
        }
    }
    strBuilder.setSpan(clickable, start, end, flags)
    if(!haveUnderlineForLink){
        strBuilder.setSpan(URLSpanNoUnderline(span.url), start, end, flags)
    }
    strBuilder.removeSpan(span)
}

fun View.locateViewInScreen(): Rect? {
    val loc_int = IntArray(2)
    try {
        this.getLocationOnScreen(loc_int)
    } catch (npe: NullPointerException) {
        //Happens when the view doesn't exist on screen anymore.
        return null
    }
    val location = Rect()
    location.left = loc_int[0]
    location.top = loc_int[1]
    location.right = location.left + this.width
    location.bottom = location.top + this.height
    return location
}

fun Fragment.waitForTransition(v:View){
    postponeEnterTransition()
    v.viewTreeObserver.addOnPreDrawListener {
        startPostponedEnterTransition()
        true
    }
}

fun ProgressBar.setProgressColor(color:Int){
    indeterminateTintList = ColorStateList.valueOf(color)
    progressTintList = ColorStateList.valueOf(color)
}