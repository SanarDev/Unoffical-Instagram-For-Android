package com.sanardev.instagrammqtt.utils

import android.os.Handler

class TaskUtils {

    companion object {
        // memory leak
        fun startTask(everyMiliSecond: Long, function: () -> Unit) {
            val mHandler = Handler()
            java.lang.Thread {
                while (true) {
                    mHandler.post {
                        function.invoke()
                    }
                    Thread.sleep(everyMiliSecond)
                }
            }.start()
        }

    }
}