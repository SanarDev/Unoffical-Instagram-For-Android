package com.sanardev.instagrammqtt.helper.dialog

interface DialogListener {

    interface Positive{
        fun onPositiveClick()
    }
    interface Negative{
        fun onNegativeClick()
    }
}