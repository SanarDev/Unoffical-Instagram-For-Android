package com.idirect.app.datasource.model.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.idirect.app.datasource.model.ClientGapEnforcerMatrix;
import com.idirect.app.datasource.model.FeedItem;
import com.idirect.app.datasource.model.StartupPrefetchConfigs;
import com.idirect.app.datasource.model.UserPost;

import java.util.List;

public class InstagramFeedTimeLineResponse {

    @SerializedName("num_results")
    @Expose
    private Long numResults;
    @SerializedName("more_available")
    @Expose
    private Boolean moreAvailable;
    @SerializedName("auto_load_more_enabled")
    @Expose
    private Boolean autoLoadMoreEnabled;
    @SerializedName("feed_items")
    @Expose
    private List<FeedItem> feedItems = null;
    @SerializedName("is_direct_v2_enabled")
    @Expose
    private Boolean isDirectV2Enabled;
    @SerializedName("next_max_id")
    @Expose
    private String nextMaxId;
    @SerializedName("view_state_version")
    @Expose
    private String viewStateVersion;
    @SerializedName("client_feed_changelist_applied")
    @Expose
    private Boolean clientFeedChangelistApplied;
    @SerializedName("feed_pill_text")
    @Expose
    private String feedPillText;
    @SerializedName("request_id")
    @Expose
    private String requestId;
    @SerializedName("session_id")
    @Expose
    private String sessionId;
    @SerializedName("pull_to_refresh_window_ms")
    @Expose
    private Long pullToRefreshWindowMs;
    @SerializedName("client_gap_enforcer_matrix")
    @Expose
    private List<ClientGapEnforcerMatrix> clientGapEnforcerMatrix = null;
    @SerializedName("startup_prefetch_configs")
    @Expose
    private StartupPrefetchConfigs startupPrefetchConfigs;
    @SerializedName("status")
    @Expose
    private String status;
    @JsonIgnore
    private List<UserPost> posts = null;

    public Boolean getDirectV2Enabled() {
        return isDirectV2Enabled;
    }

    public void setDirectV2Enabled(Boolean directV2Enabled) {
        isDirectV2Enabled = directV2Enabled;
    }

    public List<UserPost> getPosts() {
        return posts;
    }

    public void setPosts(List<UserPost> posts) {
        this.posts = posts;
    }

    public Long getNumResults() {
        return numResults;
    }

    public void setNumResults(Long numResults) {
        this.numResults = numResults;
    }

    public Boolean getMoreAvailable() {
        return moreAvailable;
    }

    public void setMoreAvailable(Boolean moreAvailable) {
        this.moreAvailable = moreAvailable;
    }

    public Boolean getAutoLoadMoreEnabled() {
        return autoLoadMoreEnabled;
    }

    public void setAutoLoadMoreEnabled(Boolean autoLoadMoreEnabled) {
        this.autoLoadMoreEnabled = autoLoadMoreEnabled;
    }

    public List<FeedItem> getFeedItems() {
        return feedItems;
    }

    public void setFeedItems(List<FeedItem> feedItems) {
        this.feedItems = feedItems;
    }

    public Boolean getIsDirectV2Enabled() {
        return isDirectV2Enabled;
    }

    public void setIsDirectV2Enabled(Boolean isDirectV2Enabled) {
        this.isDirectV2Enabled = isDirectV2Enabled;
    }

    public String getNextMaxId() {
        return nextMaxId;
    }

    public void setNextMaxId(String nextMaxId) {
        this.nextMaxId = nextMaxId;
    }

    public String getViewStateVersion() {
        return viewStateVersion;
    }

    public void setViewStateVersion(String viewStateVersion) {
        this.viewStateVersion = viewStateVersion;
    }

    public Boolean getClientFeedChangelistApplied() {
        return clientFeedChangelistApplied;
    }

    public void setClientFeedChangelistApplied(Boolean clientFeedChangelistApplied) {
        this.clientFeedChangelistApplied = clientFeedChangelistApplied;
    }

    public String getFeedPillText() {
        return feedPillText;
    }

    public void setFeedPillText(String feedPillText) {
        this.feedPillText = feedPillText;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public Long getPullToRefreshWindowMs() {
        return pullToRefreshWindowMs;
    }

    public void setPullToRefreshWindowMs(Long pullToRefreshWindowMs) {
        this.pullToRefreshWindowMs = pullToRefreshWindowMs;
    }

    public List<ClientGapEnforcerMatrix> getClientGapEnforcerMatrix() {
        return clientGapEnforcerMatrix;
    }

    public void setClientGapEnforcerMatrix(List<ClientGapEnforcerMatrix> clientGapEnforcerMatrix) {
        this.clientGapEnforcerMatrix = clientGapEnforcerMatrix;
    }

    public StartupPrefetchConfigs getStartupPrefetchConfigs() {
        return startupPrefetchConfigs;
    }

    public void setStartupPrefetchConfigs(StartupPrefetchConfigs startupPrefetchConfigs) {
        this.startupPrefetchConfigs = startupPrefetchConfigs;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
