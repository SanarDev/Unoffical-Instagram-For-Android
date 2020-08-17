package com.idirect.app.customview.customtextview

import android.content.Context
import android.os.Build
import android.text.*
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.URLSpan
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.widget.TextView
import com.idirect.app.R
import com.idirect.app.extentions.color
import com.idirect.app.utils.URLSpanNoUnderline
import com.vanniktech.emoji.EmojiTextView


class HyperTextView constructor(context: Context, attr: AttributeSet? = null) :
    EmojiTextView(context, attr) {

    init {
        setLinkTextColor(context!!.color(R.color.white))
    }

    interface OnHyperTextClick {
        fun onClick(v: View, url: String)
    }

    var mHyperTextClick: OnHyperTextClick? = null
    private var sourceText: String? = null
    private var username: String? = null
    private var userId: Long = 0

    init {
        setOnTouchListener { v, event ->
            val tv = v as TextView
            if (event.action === MotionEvent.ACTION_UP) {
                val x = event.x.toInt()
                val y = event.y.toInt()
                val layout = tv.layout
                val line = layout.getLineForVertical(y)
                val off = layout.getOffsetForHorizontal(line, x.toFloat())
                val charSequence = tv.text
                if (charSequence is Spannable) {
                    val link: Array<ClickableSpan> =
                        charSequence.getSpans(off, off, ClickableSpan::class.java)
                    if (link.size != 0) {
                        if ((link[0] as URLSpan).url == MORE_BUTTON_ID) {
                            setText(username!!, userId, sourceText!!, FLAG_NO_LIMIT)
                        } else {
                            mHyperTextClick?.onClick(tv, (link[0] as URLSpan).url)
                        }
                    } else {
                        //do other click
                    }
                }
            }
            true
        }
    }

    fun setText(username: String, userId: Long, text: String, haveLimit: Int = FLAG_NO_LIMIT) {
        this.sourceText = text
        this.username = username
        this.userId = userId

        val hyperUsername = "<a href=\"$userId\"><b>$username</b></a>"
        val usernameSequence = getHtmlSpannable(hyperUsername)
        val finalText: String
        val moreSequence: Spanned?
        if (haveLimit == FLAG_NO_LIMIT || text.length <= haveLimit) {
            finalText = sourceText!!
            moreSequence = null
        } else {
            finalText = text.substring(0, haveLimit)
            moreSequence = getHtmlSpannable("<a href=\"$MORE_BUTTON_ID\">&nbsp;&nbsp;More...&nbsp;&nbsp;</a>")
        }
        val textSequence = getHtmlSpannable(getHyperText(finalText))
        val spannable = SpannableStringBuilder()
            .append(usernameSequence)
            .append("  ")
            .append(textSequence)
        if (moreSequence != null)
            spannable.append(moreSequence)

        var urls =
            spannable.getSpans(0, spannable.length, URLSpan::class.java)
        for (span in urls) {
            makeLinkClickable(strBuilder = spannable, span = span, onClickData = span.url)
        }
        this.movementMethod = LinkMovementMethod.getInstance()
        super.setText(spannable, TextView.BufferType.SPANNABLE)
    }

    override fun setText(rawText: CharSequence?, type: BufferType?) {
        val sequence = getHtmlSpannable(getHyperText(rawText.toString()))!!
        val strBuilder = SpannableStringBuilder(sequence)
        val urls =
            strBuilder.getSpans(0, sequence.length, URLSpan::class.java)
        for (span in urls) {
            makeLinkClickable(strBuilder = strBuilder, span = span, onClickData = span.url)
        }
        this.movementMethod = LinkMovementMethod.getInstance()
        super.setText(strBuilder, TextView.BufferType.SPANNABLE)
    }

    private fun makeLinkClickable(
        strBuilder: SpannableStringBuilder,
        span: ClickableSpan,
        onClickData: String,
        haveUnderlineForLink: Boolean = false,
        foregroundColor: Int = -1
    ) {
        val start = strBuilder.getSpanStart(span)
        val end = strBuilder.getSpanEnd(span)
        val flags = strBuilder.getSpanFlags(span)
        if (!haveUnderlineForLink) {
            strBuilder.setSpan(URLSpanNoUnderline(onClickData), start, end, flags)
        }
        if (foregroundColor != -1) {
            strBuilder.setSpan(ForegroundColorSpan(foregroundColor), start, end, flags)
        }
        strBuilder.removeSpan(span)
    }

    private fun getHtmlSpannable(text: String): Spanned? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY)
        } else {
            return Html.fromHtml(text)
        }
    }

    private fun getHyperText(text: String): String {
        return text.replace("(#[a-zA-z0-9_\\u0080-\\u9fff]+)".toRegex(), "<a href=\"$0\">$0</a>")
            .replace("(@[a-zA-z_.0-9]*)[a-zA-Z0-9_]".toRegex(), "<a href=\"$0\">$0</a>")
            .replace("\n", "<br/>")
    }

    companion object {
        const val FLAG_NO_LIMIT = -1
        private const val MORE_BUTTON_ID = "-85755415"

        fun getLikedByHyperText(username: String, userId: Long, likeCount: Int): String {
            return "  Liked by <a href=\"$userId\"><b>$username</b></a> and <a href=\"SeeAllLikers\"><b>$likeCount others</b></a>"
        }

        fun getLikersCountHyperText(likeCount: Int): String {
            return "<a href=\"SeeAllLikers\"><b>$likeCount Likes</b></a>"
        }
    }

}