package com.idirect.app.datasource.model.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.idirect.app.datasource.model.Tray;
import com.idirect.app.datasource.model.payload.StatusResult;

import java.util.HashMap;

public class InstagramStoryMediaResponse extends StatusResult {

    @SerializedName("reels")
    @Expose
    private HashMap<Long, Tray> reels;

    public HashMap<Long, Tray> getReels() {
        return reels;
    }

    public void setReels(HashMap<Long, Tray> reels) {
        this.reels = reels;
    }
}
