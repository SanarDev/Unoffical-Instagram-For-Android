package com.sanardev.instagrammqtt.utils.dialog

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.sanardev.instagrammqtt.datasource.model.DialogModel
import com.sanardev.instagrammqtt.BR
import com.sanardev.instagrammqtt.R
import kotlinx.android.synthetic.main.layout_dialog.view.*

class DialogHelper {

    companion object{
        fun createDialog(context:Context, layoutInflater:LayoutInflater, title:String, message:String, positiveText:String, positiveListener:DialogListener.Positive?=null, negativeText:String?=null, negativeListener: DialogListener.Negative?=null){
            val dialog = Dialog(context)
            val viewDataBinding: ViewDataBinding = DataBindingUtil.inflate(
                layoutInflater,
                R.layout.layout_dialog,
                null,
                false
            )
            viewDataBinding.setVariable(
                BR.obj,
                DialogModel(
                    title = title,
                    message = message,
                    positiveText = positiveText,
                    negativeText = negativeText
                )
            )
            viewDataBinding.root.btnPositive.setOnClickListener {
                dialog.dismiss()
                positiveListener?.onPositiveClick()
            }
            viewDataBinding.root.btnNegative.setOnClickListener {
                dialog.dismiss()
                negativeListener?.onNegativeClick()
            }
            dialog.setContentView(viewDataBinding.root)
            dialog.setCancelable(false)
            dialog.show()
            val window = dialog.window!!
            window.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
    }
}