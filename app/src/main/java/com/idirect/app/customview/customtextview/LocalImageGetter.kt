package com.idirect.app.customview.customtextview

import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.text.Html.ImageGetter
import android.util.Log
import com.idirect.app.extensions.drawble

class LocalImageGetter(var context:Context) : ImageGetter {
    override fun getDrawable(source: String): Drawable {
         var d:Drawable ? = null
        try {
            d = context.resources.drawble(source !!.toInt())
            d.setBounds(0, 0, d.intrinsicWidth, d.intrinsicHeight)
        } catch (e: Resources.NotFoundException){
            Log.e("log_tag", "Image not found. Check the ID.", e)
        } catch(e:NumberFormatException){
            Log.e("log_tag", "Source string not a valid resource ID.", e)
        }
        return d !!
    }
}