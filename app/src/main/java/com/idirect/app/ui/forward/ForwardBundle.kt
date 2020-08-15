package com.idirect.app.ui.forward

data class ForwardBundle(
    var mediaId: String,
    var mediaType: Int,
    var isStoryShare: Boolean,
    var reelId:Long = 0
) {
}