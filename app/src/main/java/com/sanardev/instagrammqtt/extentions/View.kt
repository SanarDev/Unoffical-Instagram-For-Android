package com.sanardev.instagrammqtt.extensions

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Build
import android.text.Html
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.URLSpan
import android.view.View
import android.widget.TextView
import com.sanardev.instagrammqtt.utils.URLSpanNoUnderline
import run.tripa.android.extensions.openUrl


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

fun TextView.setTextHTML(
    context: Context,
    html: String,
    haveUnderlineForLink: Boolean = false
) {
    val htmlText = html.replace("\n","<br/>").replace(
        "(?:(?:https?|ftp):\\/\\/)?[\\w/\\-?=%.]+\\.[\\w/\\-?=%.]+".toRegex(),
        "<a href=\"$0\">$0</a>"
    );
    val sequence =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(htmlText, Html.FROM_HTML_MODE_LEGACY)
        } else {
            Html.fromHtml(htmlText)
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