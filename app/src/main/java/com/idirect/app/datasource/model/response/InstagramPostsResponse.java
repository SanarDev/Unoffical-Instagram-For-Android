package com.idirect.app.datasource.model.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.idirect.app.datasource.model.UserPost;
import com.idirect.app.datasource.model.payload.StatusResult;

import java.util.List;

public class InstagramPostsResponse extends StatusResult {

    @SerializedName("num_results")
    @Expose
    private int numResults;
    @SerializedName("more_available")
    @Expose
    private boolean moreAvailable;
    @SerializedName("auto_load_more_enabled")
    @Expose
    private boolean autoLoadMoreEnabled;
    @SerializedName("items")
    @Expose
    private List<UserPost> userPosts;

    public int getNumResults() {
        return numResults;
    }

    public void setNumResults(int numResults) {
        this.numResults = numResults;
    }

    public boolean isMoreAvailable() {
        return moreAvailable;
    }

    public void setMoreAvailable(boolean moreAvailable) {
        this.moreAvailable = moreAvailable;
    }

    public boolean isAutoLoadMoreEnabled() {
        return autoLoadMoreEnabled;
    }

    public void setAutoLoadMoreEnabled(boolean autoLoadMoreEnabled) {
        this.autoLoadMoreEnabled = autoLoadMoreEnabled;
    }

    public List<UserPost> getUserPosts() {
        return userPosts;
    }

    public void setUserPosts(List<UserPost> userPosts) {
        this.userPosts = userPosts;
    }
}
