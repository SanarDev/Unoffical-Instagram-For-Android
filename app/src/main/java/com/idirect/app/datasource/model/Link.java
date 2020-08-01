package com.idirect.app.datasource.model;

import androidx.room.Ignore;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Link {

    @SerializedName("text")
    @Expose
    private String text;
    @SerializedName("link_context")
    @Expose
    private LinkContext linkContext;
    @SerializedName("client_context")
    @Expose
    private String clientContext;
    @SerializedName("mutation_token")
    @Expose
    private String mutationToken;
    @Ignore
    private List<String> linkList;

    public List<String> getLinkList() {
        return linkList;
    }

    public void setLinkList(List<String> linkList) {
        this.linkList = linkList;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public LinkContext getLinkContext() {
        return linkContext;
    }

    public void setLinkContext(LinkContext linkContext) {
        this.linkContext = linkContext;
    }

    public String getClientContext() {
        return clientContext;
    }

    public void setClientContext(String clientContext) {
        this.clientContext = clientContext;
    }

    public String getMutationToken() {
        return mutationToken;
    }

    public void setMutationToken(String mutationToken) {
        this.mutationToken = mutationToken;
    }
}
