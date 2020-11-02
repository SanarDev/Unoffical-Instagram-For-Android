package com.idirect.app.datasource.model;

import androidx.room.Ignore;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Link extends com.sanardev.instagramapijava.model.direct.messagetype.Link {

    @Ignore
    private List<String> linkList;

    public List<String> getLinkList() {
        return linkList;
    }

    public void setLinkList(List<String> linkList) {
        this.linkList = linkList;
    }

}
