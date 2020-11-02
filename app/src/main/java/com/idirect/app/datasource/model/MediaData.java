package com.idirect.app.datasource.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class MediaData extends com.sanardev.instagramapijava.model.direct.messagetype.MediaData {

    @JsonIgnore
    private boolean isLocal=false;
    @JsonIgnore
    private String localFilePath;
    @JsonIgnore
    private int localDuration = 0;

    public boolean isLocal() {
        return isLocal;
    }

    public void setLocal(boolean local) {
        isLocal = local;
    }

    public String getLocalFilePath() {
        return localFilePath;
    }

    public void setLocalFilePath(String localFilePath) {
        this.localFilePath = localFilePath;
    }

    public int getLocalDuration() {
        return localDuration;
    }

    public void setLocalDuration(int localDuration) {
        this.localDuration = localDuration;
    }
}
