package com.idirect.app.realtime.subcribers

class SkywalkerSubscriptions {
    companion object{
        fun directSub(userId:String) = "ig/u/v1/${userId}"
        fun liveSub(userId: String) = "ig/live_notification_subscribe/${userId}"
    }
}