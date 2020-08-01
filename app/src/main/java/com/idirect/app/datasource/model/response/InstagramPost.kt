package com.idirect.app.datasource.model.response

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.idirect.app.datasource.model.Media
import com.idirect.app.datasource.model.payload.StatusResult

class InstagramPost:StatusResult() {

    @SerializedName("items")
    @Expose
    lateinit var items:List<Media>
    @SerializedName("num_results")
    @Expose
    var countResult:Int = 0
    @SerializedName("auto_load_more_enabled")
    @Expose
    var autoLoadMoreEnabled:Boolean = false
}