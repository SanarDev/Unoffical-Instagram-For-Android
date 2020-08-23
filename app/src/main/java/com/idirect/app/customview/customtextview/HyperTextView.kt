package com.idirect.app.customview.customtextview

import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.os.Build
import android.text.Html
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.URLSpan
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import com.idirect.app.R
import com.idirect.app.extensions.drawble
import com.idirect.app.extentions.color
import com.idirect.app.extentions.getLinesOf
import com.idirect.app.utils.HtmlUtils
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
    private var isVerified: Boolean = false
    private val mLocalImageGetter = LocalImageGetter(context)

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
                            setText(username!!, userId, isVerified,sourceText!!, FLAG_NO_LIMIT)
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

    fun setText(username: String,userId: Long,isVerified: Boolean){

        var hyperUsername = "<a href=\"$userId\"><b>$username</b></a>"
        if(isVerified){
            hyperUsername += HtmlUtils.SPACE + String.format("<img src =\"%s\"/>",R.drawable.ic_verify)
        }
        val usernameSequence = getHtmlSpannable(hyperUsername)
        val spannable = SpannableStringBuilder()
            .append(usernameSequence)

        var urls =
            spannable.getSpans(0, spannable.length, URLSpan::class.java)
        for (span in urls) {
            makeLinkClickable(strBuilder = spannable, span = span, onClickData = span.url)
        }
        this.movementMethod = LinkMovementMethod.getInstance()
        super.setText(spannable, TextView.BufferType.SPANNABLE)
        gravity = Gravity.CENTER_VERTICAL
    }

    fun setText(username: String, userId: Long,isVerified:Boolean, text: String, haveLineLimit: Int = FLAG_NO_LIMIT) {
        this.sourceText = text
        this.username = username
        this.userId = userId
        this.isVerified = isVerified

        var hyperUsername = "<a href=\"$userId\"><b>$username</b></a>"
        if(isVerified){
            hyperUsername += HtmlUtils.SPACE + String.format("<img src =\"%s\"/>",R.drawable.ic_verify)
        }
        val usernameSequence = getHtmlSpannable(hyperUsername)
        val finalText: String
        val moreSequence: Spanned?
        if (haveLineLimit == FLAG_NO_LIMIT || text.length <= haveLineLimit) {
            finalText = sourceText!!
            moreSequence = null
        } else {
            finalText = text.substring(0,haveLineLimit)
            moreSequence = getHtmlSpannable("<a href=\"$MORE_BUTTON_ID\">${HtmlUtils.SPACE}${HtmlUtils.SPACE}More...${HtmlUtils.SPACE}${HtmlUtils.SPACE}</a>")
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
            return Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY,mLocalImageGetter,null)
        } else {
            return Html.fromHtml(text,mLocalImageGetter,null)
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

        fun getLikedByHyperText(username: String, userId: Long, likeCount: String): String {
            return "  Liked by <a href=\"$userId\"><b>$username</b></a> and <a href=\"SeeAllLikers\"><b>$likeCount others</b></a>"
        }

        fun getLikersCountHyperText(likeCount: String): String {
            return "<a href=\"SeeAllLikers\"><b>$likeCount Likes</b></a>"
        }
    }

}