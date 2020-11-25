package com.idirect.app.ui.customview.toast

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.widget.TextView
import android.widget.Toast
import com.idirect.app.R

class CustomToast{
    companion object{
        fun show(context: Context,text:String,duration:Int){
            val toast: Toast = Toast.makeText(
                context,
                text,
                duration
            )
            val view = toast.view
            view.setBackgroundResource(R.drawable.bg_toast)
            val text: TextView = view.findViewById(android.R.id.message)
            text.setTextColor(Color.WHITE)
            toast.setGravity(Gravity.CENTER, 0, 0)
            toast.show()
        }
        fun show(context: Context,text:String){
            val toast: Toast = Toast.makeText(
                context,
                text,
                Toast.LENGTH_SHORT
            )
            val view = toast.view
            view.setBackgroundResource(R.drawable.bg_toast)
            val text: TextView = view.findViewById(android.R.id.message)
            text.setTextColor(Color.WHITE)
            toast.setGravity(Gravity.CENTER, 0, 0)
            toast.show()
        }
    }
}