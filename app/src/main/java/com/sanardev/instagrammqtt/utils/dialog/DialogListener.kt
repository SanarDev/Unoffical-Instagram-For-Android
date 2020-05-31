package com.sanardev.instagrammqtt.utils.dialog

interface DialogListener {

    interface Positive{
        fun onPositiveClick()
    }
    interface Negative{
        fun onNegativeClick()
    }
}