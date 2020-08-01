package com.idirect.app.utils.dialog

interface DialogListener {

    interface Positive{
        fun onPositiveClick()
    }
    interface Negative{
        fun onNegativeClick()
    }
}