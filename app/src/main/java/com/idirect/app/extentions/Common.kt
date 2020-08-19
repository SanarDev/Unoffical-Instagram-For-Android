package com.idirect.app.extentions

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Resources
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.idirect.app.R
import com.vanniktech.emoji.EmojiPopup
import java.io.File
import java.lang.StringBuilder


fun Context.toast(message: CharSequence) = Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
fun Context.longToast(message: CharSequence) =
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()

fun <T> Context.openActivity(it: Class<T>, extras: Bundle.() -> Unit = {}) {
    val intent = Intent(this, it)
    intent.putExtras(Bundle().apply(extras))
    startActivity(intent)
}

fun <T> Context.openActivityForResult(
    it: Class<T>,
    resultCode: Int,
    extras: Bundle.() -> Unit = {}
) {
    val intent = Intent(this, it)
    intent.putExtras(Bundle().apply(extras))
    startActivity(intent)
}

fun AppCompatActivity.replaceFragment(
    fragment: Fragment,
    @IdRes container: Int,
    addToBackStack: Boolean = false
) {
    val backStateName: String = fragment.javaClass.getName()

    val manager: FragmentManager = supportFragmentManager
    val fragmentPopped: Boolean = manager.popBackStackImmediate(backStateName, 0)

    if (!fragmentPopped && manager.findFragmentByTag(backStateName) == null) { //fragment not in back stack, create it.
        val ft: FragmentTransaction = manager.beginTransaction()
        ft.replace(container, fragment, backStateName)
        ft.setCustomAnimations(
            R.anim.enter_from_right,
            R.anim.exit_to_left,
            R.anim.enter_from_left,
            R.anim.exit_to_right
        );
        ft.addToBackStack(backStateName)
        ft.commit()
    }
}

fun Fragment.replaceFragment(
    fragment: Fragment,
    @IdRes container: Int,
    addToBackStack: Boolean = false
) {
    val backStateName: String = fragment.javaClass.getName()

    val manager: FragmentManager = activity!!.supportFragmentManager
    val fragmentPopped: Boolean = manager.popBackStackImmediate(backStateName, 0)

    if (!fragmentPopped && manager.findFragmentByTag(backStateName) == null) { //fragment not in back stack, create it.
        val ft: FragmentTransaction = manager.beginTransaction()
        ft.setCustomAnimations(
            R.anim.enter_from_right,
            R.anim.exit_to_left,
            R.anim.enter_from_left,
            R.anim.exit_to_right
        );
        ft.replace(container, fragment, backStateName)
        ft.addToBackStack(backStateName)
        ft.commit()
    }
}

val View.realHeight: Int
    get() {
        val widthSpec = View.MeasureSpec.makeMeasureSpec(
            width,
            View.MeasureSpec.EXACTLY
        );
        val heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        measure(widthSpec, heightSpec)
        return measuredHeight
    }

@SuppressLint("MissingPermission")
fun Context.vibration(duration: Long = 500) {
    val v = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        v.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
    } else {
        v.vibrate(duration)
    }
}

@SuppressLint("MissingPermission")
fun Context.vibration(vararg duration: Long) {
    val v = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        v.vibrate(VibrationEffect.createWaveform(duration, -1))
    } else {
        v.vibrate(duration, -1)
    }
}

fun ByteArray.toHexString() = joinToString("") { "%02x".format(it) }

fun Dialog.openFromBottom(height: Int = ViewGroup.LayoutParams.MATCH_PARENT) {
    assert(window != null)
    window!!.setGravity(Gravity.BOTTOM)
    window!!.setBackgroundDrawableResource(android.R.color.transparent)
    window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, height)
}

fun Resources.dpToPx(dp: Float): Int {
    return dpToPx(dp, this)
}

private const val HTTPS = "https://"
private const val HTTP = "http://"

fun openUrl(context: Context, url: String) {
    var url = url
    if (!url.startsWith(HTTP) && !url.startsWith(HTTPS)) {
        url = HTTP + url
    }
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    context.startActivity(
        Intent.createChooser(
            intent,
            "Choose browser"
        )
    ) // Choose browser is arbitrary :)
}

fun List<Any>.toStringList(): String {
    var str = "["
    for (i in this.indices) {
        val user = this[i]
        if (i < this.size - 1) {
            str += "\"${this[i]}\","
        } else {
            str += "\"${this[i]}\""
        }
    }
    str += "]"
    return str
}

fun Activity.hideKeyboard() {
    val imm = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    //Find the currently focused view, so we can grab the correct window token from it.
    var view = currentFocus
    //If no view currently has focus, create a new one, just so we can grab a window token from it
    if (view == null) {
        view = View(this)
    }
    imm.hideSoftInputFromWindow(view!!.windowToken, 0)
}

fun dpToPx(dp: Float, resources: Resources): Int {
    val px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics)
    return px.toInt()
}

fun Application.openSharedPref(name: String): SharedPreferences? {
    return getSharedPreferences(name, Context.MODE_PRIVATE)
}

fun Activity.shareText(shareBody: String) {
    val sendIntent: Intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, shareBody)
        type = "text/plain"
    }

    val shareIntent = Intent.createChooser(sendIntent, null)
    startActivity(shareIntent)
}
fun String.getLinesOf(from:Int,to:Int): String {
    val lines = lines()
    val strBuilder = StringBuilder()
    for(index in from..to){
        strBuilder.append(lines[index])
        strBuilder.append("\r\n")
    }
    return strBuilder.toString()
}
val File.size get() = if (!exists()) 0.0 else length().toDouble()
val File.sizeInKb get() = size / 1024
val File.sizeInMb get() = sizeInKb / 1024
val File.sizeInGb get() = sizeInMb / 1024
val File.sizeInTb get() = sizeInGb / 1024
