package com.idirect.app.datasource.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName



class Nametag {

    @SerializedName("mode")
    @Expose
    var mode: Int? = null
    @SerializedName("gradient")
    @Expose
    var gradient: String? = null
    @SerializedName("emoji")
    @Expose
    var emoji: String? = null
    @SerializedName("emoji_color")
    @Expose
    var emojiColor: String? = null
    @SerializedName("selfie_sticker")
    @Expose
    var selfieSticker: String? = null
}