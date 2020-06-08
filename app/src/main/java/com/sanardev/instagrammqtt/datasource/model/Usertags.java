package com.sanardev.instagrammqtt.datasource.model;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Usertags {

    @SerializedName("in")
    @Expose
    private List<In> in = null;

    public List<In> getIn() {
        return in;
    }

    public void setIn(List<In> in) {
        this.in = in;
    }

}