package com.idirect.app.datasource.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class AdditionalCandidates {

    @SerializedName("igtv_first_frame")
    @Expose
    private Frame igtvFirstFrame;
    @SerializedName("first_frame")
    @Expose
    private Frame firstFrame;
}
