package com.idirect.app.datasource.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ClientGapEnforcerMatrix {

    @SerializedName("list")
    @Expose
    private List<Long> list = null;

    public List<Long> getList() {
        return list;
    }

    public void setList(List<Long> list) {
        this.list = list;
    }
}
