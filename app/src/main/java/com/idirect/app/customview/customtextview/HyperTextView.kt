package com.idirect.app.customview.customtextview

import android.content.Context
import android.graphics.Color
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
import android.view.View
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

    fun setText(username: String,userId:Long, text: String) {
        val hyperUsername = "<a href=\"$userId\"><b>$username</b></a>"
        val usernameSequence = getHtmlSpannable(hyperUsername)
        val textSequence = getHtmlSpannable(getHyperText(text))
        val spannable = SpannableStringBuilder()
            .append(usernameSequence)
            .append("  ")
            .append(textSequence)

//        val usernameSpan = spannable.getSpans(0, usernameSequence!!.length, ClickableSpan::class.java).get(0)
//        makeLinkClickable(strBuilder = spannable, span = usernameSpan,foregroundColor = Color.WHITE,onClickData = username)
        var urls =
            spannable.getSpans(0, spannable.length, URLSpan::class.java)
        for (span in urls) {
            makeLinkClickable(strBuilder = spannable, span = span,onClickData = span.url)
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
            makeLinkClickable(strBuilder = strBuilder, span = span,onClickData = span.url)
        }
        this.movementMethod = LinkMovementMethod.getInstance()
        super.setText(strBuilder, TextView.BufferType.SPANNABLE)
    }

    private fun makeLinkClickable(
        strBuilder: SpannableStringBuilder,
        span: ClickableSpan,
        onClickData:String,
        haveUnderlineForLink: Boolean = false,
        foregroundColor: Int = -1
    ) {
        val start = strBuilder.getSpanStart(span)
        val end = strBuilder.getSpanEnd(span)
        val flags = strBuilder.getSpanFlags(span)
        val clickable: ClickableSpan = object : ClickableSpan() {
            override fun onClick(view: View) {
                mHyperTextClick?.onClick(view, onClickData)
            }
        }
        strBuilder.setSpan(clickable, start, end, flags)
        if (!haveUnderlineForLink) {
            strBuilder.setSpan(URLSpanNoUnderline(onClickData), start, end, flags)
        }
        if(foregroundColor != -1){
            strBuilder.setSpan(ForegroundColorSpan(foregroundColor), start, end, flags)
        }
        strBuilder.removeSpan(span)
    }

    private fun getHtmlSpannable(text:String): Spanned? {
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
}