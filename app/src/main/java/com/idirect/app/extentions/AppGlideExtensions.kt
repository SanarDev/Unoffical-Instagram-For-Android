package com.idirect.app.extentions

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import androidx.annotation.Nullable
import androidx.annotation.RawRes
import androidx.appcompat.widget.AppCompatImageView
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.idirect.app.R
import com.idirect.app.di.module.GlideApp

object ViewGlideExtensions{
    fun AppCompatImageView.load(url:String?){
        GlideApp.with(context)
            .load(url)
            .apply(AppGlideExtensions.default())
            .into(this)
    }

    fun AppCompatImageView.load(drawable:Drawable){
        GlideApp.with(context)
            .load(drawable)
            .apply(AppGlideExtensions.default())
            .into(this)
    }
    fun AppCompatImageView.load(@RawRes @DrawableRes @Nullable id:Int){
        GlideApp.with(context)
            .load(id)
            .apply(AppGlideExtensions.default())
            .into(this)
    }
}
object AppGlideExtensions {
    fun default(): RequestOptions {
        return RequestOptions()
            .diskCacheStrategy(DiskCacheStrategy.ALL)
    }

    fun circleProfile(): RequestOptions {
        return RequestOptions()
            .circleCrop()
            .placeholder(R.mipmap.ic_profile_place_holder)
            .error(R.mipmap.ic_profile_place_holder)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
    }

    fun tabImage(): RequestOptions {
        return RequestOptions()
            .placeholder(R.drawable.placeholder_loading)
            .error(R.drawable.placeholder_loading)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
    }
}