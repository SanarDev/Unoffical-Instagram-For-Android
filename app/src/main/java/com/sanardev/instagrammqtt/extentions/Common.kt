package run.tripa.android.extensions

import android.annotation.SuppressLint
import android.app.Application
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Resources
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import android.os.VibrationEffect
import android.os.Build
import android.os.Vibrator
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup
import android.app.Activity
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.view.inputmethod.InputMethodManager


fun Context.toast(message: CharSequence) = Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
fun Context.longToast(message: CharSequence) = Toast.makeText(this, message, Toast.LENGTH_LONG).show()

fun <T> Context.openActivity(it: Class<T>, extras: Bundle.() -> Unit = {}) {
    val intent = Intent(this, it)
    intent.putExtras(Bundle().apply(extras))
    startActivity(intent)
}

fun <T> Context.openActivityForResult(it: Class<T>, resultCode: Int, extras: Bundle.() -> Unit = {}) {
    val intent = Intent(this, it)
    intent.putExtras(Bundle().apply(extras))
    startActivity(intent)
}

fun AppCompatActivity.replaceFragment(fragment: Fragment, @IdRes container: Int, addToBackStack: Boolean = false) {
    val fragmentManager = supportFragmentManager
    val fragmentTransaction = fragmentManager.beginTransaction()
    fragmentTransaction.replace(container, fragment, fragment.javaClass.name)
    if (addToBackStack)
        fragmentTransaction.addToBackStack(null)
    fragmentTransaction.commit()
}

fun Fragment.replaceFragment(fragment: Fragment, @IdRes container: Int, addToBackStack: Boolean = false) {
    val fragmentManager = activity!!.supportFragmentManager
    val fragmentTransaction = fragmentManager.beginTransaction()
    fragmentTransaction.replace(container, fragment, fragment.javaClass.name)
    if (addToBackStack)
        fragmentTransaction.addToBackStack(null)
    fragmentTransaction.commit()
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

fun ByteArray.toHexString() = joinToString("") { "%02x".format(it) }

fun Dialog.openFromBottom(height:Int = ViewGroup.LayoutParams.MATCH_PARENT) {
    assert(window != null)
    window!!.setGravity(Gravity.BOTTOM)
    window!!.setBackgroundDrawableResource(android.R.color.transparent)
    window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, height)
}

fun Resources.dpToPx(dp: Float): Int {
    return dpToPx(dp,this)
}

fun Activity.hideKeyboard(){
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

fun Application.openSharedPref(name:String): SharedPreferences? {
    return getSharedPreferences(name, Context.MODE_PRIVATE)
}