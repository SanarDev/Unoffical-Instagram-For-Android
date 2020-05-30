package com.sanardev.instagrammqtt.datasource.model.payload

import androidx.annotation.NonNull

 open class StatusResult {
 @NonNull
 var status: String? = null
 var message: String? = null

 var spam: Boolean = false
 var lock: Boolean = false
 var feedback_title: String? = null
 var feedback_message: String? = null
 var error_type: String? = null
 var checkpoint_url: String? = null

 companion object {

  fun setValues(to: StatusResult, from: StatusResult) {
   to.status = from.status
   to.message = from.status
   to.spam = from.spam
   to.lock= from.lock
   to.feedback_title= from.feedback_title
   to.feedback_message = from.feedback_message
   to.error_type = from.error_type
   to.checkpoint_url = from.checkpoint_url
  }
 }
}