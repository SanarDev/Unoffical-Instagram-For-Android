package com.sanardev.instagrammqtt.datasource.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.google.gson.internal.LinkedTreeMap;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Thread {

    @SerializedName("thread_id")
    @Expose
    private String threadId;
    @SerializedName("thread_v2_id")
    @Expose
    private String threadV2Id;
    @SerializedName("users")
    @Expose
    private List<User> users = null;
    @SerializedName("left_users")
    @Expose
    private List<User> leftUsers = null;
    @SerializedName("admin_user_ids")
    @Expose
    private List<Object> adminUserIds = null;
    @SerializedName("items")
    @Expose
    private List<Message> messages = null;
    private List<Object> releasesMessage = null;
    @SerializedName("last_activity_at")
    @Expose
    private long lastActivityAt;
    @SerializedName("muted")
    @Expose
    private Boolean muted;
    @SerializedName("is_pin")
    @Expose
    private Boolean isPin;
    @SerializedName("named")
    @Expose
    private Boolean named;
    @SerializedName("canonical")
    @Expose
    private Boolean canonical;
    @SerializedName("pending")
    @Expose
    private Boolean pending;
    @SerializedName("archived")
    @Expose
    private Boolean archived;
    @SerializedName("thread_type")
    @Expose
    private String threadType;
    @SerializedName("viewer_id")
    @Expose
    private long viewerId;
    @SerializedName("thread_title")
    @Expose
    private String threadTitle;
    @SerializedName("folder")
    @Expose
    private Integer folder;
    @SerializedName("vc_muted")
    @Expose
    private Boolean vcMuted = false;
    @SerializedName("is_group")
    @Expose
    private Boolean isGroup = false;
    @SerializedName("mentions_muted")
    @Expose
    private Boolean mentionsMuted;
    @SerializedName("approval_required_for_new_members")
    @Expose
    private Boolean approvalRequiredForNewMembers;
    @SerializedName("input_mode")
    @Expose
    private Integer inputMode;
    @SerializedName("business_thread_folder")
    @Expose
    private Integer businessThreadFolder;
    @SerializedName("read_state")
    @Expose
    private Integer readState;
    @SerializedName("last_non_sender_item_at")
    @Expose
    private long lastNonSenderItemAt;
    @SerializedName("assigned_admin_id")
    @Expose
    private Integer assignedAdminId;
    @SerializedName("shh_mode_enabled")
    @Expose
    private Boolean shhModeEnabled;
    @SerializedName("inviter")
    @Expose
    private Inviter inviter;
    @SerializedName("has_older")
    @Expose
    private Boolean hasOlder;
    @SerializedName("has_newer")
    @Expose
    private Boolean hasNewer;
    @SerializedName("last_seen_at")
    @Expose
    private HashMap<String,Seen> lastSeenAt;
    @SerializedName("newest_cursor")
    @Expose
    private String newestCursor;
    @SerializedName("oldest_cursor")
    @Expose
    private String oldestCursor;
    @SerializedName("next_cursor")
    @Expose
    private String nextCursor;
    @SerializedName("prev_cursor")
    @Expose
    private String prevCursor;
    @SerializedName("is_spam")
    @Expose
    private Boolean isSpam;
    @SerializedName("last_permanent_item")
    @Expose
    private LastPermanentItem lastPermanentItem;
    @JsonIgnore
    private Boolean isActive = false;
    @JsonIgnore
    private Boolean isTyping = false;
    @JsonIgnore
    private Long typingAtMs;

    public Long getTypingAtMs() {
        return typingAtMs;
    }

    public void setTypingAtMs(Long typingAtMs) {
        this.typingAtMs = typingAtMs;
    }

    public Boolean getTyping() {
        return isTyping;
    }

    public void setTyping(Boolean typing) {
        isTyping = typing;
    }

    public Boolean getPin() {
        return isPin;
    }

    public void setPin(Boolean pin) {
        isPin = pin;
    }

    public Boolean getGroup() {
        return isGroup;
    }

    public void setGroup(Boolean group) {
        isGroup = group;
    }

    public Boolean getSpam() {
        return isSpam;
    }

    public void setSpam(Boolean spam) {
        isSpam = spam;
    }

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }

    public String getThreadId() {
        return threadId;
    }

    public void setThreadId(String threadId) {
        this.threadId = threadId;
    }

    public String getThreadV2Id() {
        return threadV2Id;
    }

    public void setThreadV2Id(String threadV2Id) {
        this.threadV2Id = threadV2Id;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public List<User> getLeftUsers() {
        return leftUsers;
    }

    public void setLeftUsers(List<User> leftUsers) {
        this.leftUsers = leftUsers;
    }

    public List<Object> getAdminUserIds() {
        return adminUserIds;
    }

    public void setAdminUserIds(List<Object> adminUserIds) {
        this.adminUserIds = adminUserIds;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    public long getLastActivityAt() {
        return lastActivityAt;
    }

    public void setLastActivityAt(long lastActivityAt) {
        this.lastActivityAt = lastActivityAt;
    }

    public Boolean getMuted() {
        return muted;
    }

    public void setMuted(Boolean muted) {
        this.muted = muted;
    }

    public Boolean getIsPin() {
        return isPin;
    }

    public void setIsPin(Boolean isPin) {
        this.isPin = isPin;
    }

    public Boolean getNamed() {
        return named;
    }

    public void setNamed(Boolean named) {
        this.named = named;
    }

    public Boolean getCanonical() {
        return canonical;
    }

    public void setCanonical(Boolean canonical) {
        this.canonical = canonical;
    }

    public Boolean getPending() {
        return pending;
    }

    public void setPending(Boolean pending) {
        this.pending = pending;
    }

    public Boolean getArchived() {
        return archived;
    }

    public void setArchived(Boolean archived) {
        this.archived = archived;
    }

    public String getThreadType() {
        return threadType;
    }

    public void setThreadType(String threadType) {
        this.threadType = threadType;
    }

    public long getViewerId() {
        return viewerId;
    }

    public void setViewerId(long viewerId) {
        this.viewerId = viewerId;
    }

    public String getThreadTitle() {
        return threadTitle;
    }

    public void setThreadTitle(String threadTitle) {
        this.threadTitle = threadTitle;
    }

    public Integer getFolder() {
        return folder;
    }

    public void setFolder(Integer folder) {
        this.folder = folder;
    }

    public Boolean getVcMuted() {
        return vcMuted;
    }

    public void setVcMuted(Boolean vcMuted) {
        this.vcMuted = vcMuted;
    }

    public Boolean getIsGroup() {
        return isGroup;
    }

    public void setIsGroup(Boolean isGroup) {
        this.isGroup = isGroup;
    }

    public Boolean getMentionsMuted() {
        return mentionsMuted;
    }

    public void setMentionsMuted(Boolean mentionsMuted) {
        this.mentionsMuted = mentionsMuted;
    }

    public Boolean getApprovalRequiredForNewMembers() {
        return approvalRequiredForNewMembers;
    }

    public void setApprovalRequiredForNewMembers(Boolean approvalRequiredForNewMembers) {
        this.approvalRequiredForNewMembers = approvalRequiredForNewMembers;
    }

    public Integer getInputMode() {
        return inputMode;
    }

    public void setInputMode(Integer inputMode) {
        this.inputMode = inputMode;
    }

    public Integer getBusinessThreadFolder() {
        return businessThreadFolder;
    }

    public void setBusinessThreadFolder(Integer businessThreadFolder) {
        this.businessThreadFolder = businessThreadFolder;
    }

    public Integer getReadState() {
        return readState;
    }

    public void setReadState(Integer readState) {
        this.readState = readState;
    }

    public long getLastNonSenderItemAt() {
        return lastNonSenderItemAt;
    }

    public void setLastNonSenderItemAt(long lastNonSenderItemAt) {
        this.lastNonSenderItemAt = lastNonSenderItemAt;
    }

    public Integer getAssignedAdminId() {
        return assignedAdminId;
    }

    public void setAssignedAdminId(Integer assignedAdminId) {
        this.assignedAdminId = assignedAdminId;
    }

    public Boolean getShhModeEnabled() {
        return shhModeEnabled;
    }

    public void setShhModeEnabled(Boolean shhModeEnabled) {
        this.shhModeEnabled = shhModeEnabled;
    }

    public Inviter getInviter() {
        return inviter;
    }

    public void setInviter(Inviter inviter) {
        this.inviter = inviter;
    }

    public Boolean getHasOlder() {
        return hasOlder;
    }

    public void setHasOlder(Boolean hasOlder) {
        this.hasOlder = hasOlder;
    }

    public Boolean getHasNewer() {
        return hasNewer;
    }

    public void setHasNewer(Boolean hasNewer) {
        this.hasNewer = hasNewer;
    }

    public String getNewestCursor() {
        return newestCursor;
    }

    public void setNewestCursor(String newestCursor) {
        this.newestCursor = newestCursor;
    }

    public String getOldestCursor() {
        return oldestCursor;
    }

    public void setOldestCursor(String oldestCursor) {
        this.oldestCursor = oldestCursor;
    }

    public String getNextCursor() {
        return nextCursor;
    }

    public void setNextCursor(String nextCursor) {
        this.nextCursor = nextCursor;
    }

    public String getPrevCursor() {
        return prevCursor;
    }

    public void setPrevCursor(String prevCursor) {
        this.prevCursor = prevCursor;
    }

    public Boolean getIsSpam() {
        return isSpam;
    }

    public void setIsSpam(Boolean isSpam) {
        this.isSpam = isSpam;
    }

    public LastPermanentItem getLastPermanentItem() {
        return lastPermanentItem;
    }

    public void setLastPermanentItem(LastPermanentItem lastPermanentItem) {
        this.lastPermanentItem = lastPermanentItem;
    }

    public HashMap<String,Seen> getLastSeenAt() {
        return lastSeenAt;
    }

    public void setLastSeenAt(HashMap<String,Seen> lastSeenAt) {
        this.lastSeenAt = lastSeenAt;
    }

    public List<Object> getReleasesMessage() {
        return releasesMessage;
    }

    public void setReleasesMessage(List<Object> releasesMessage) {
        this.releasesMessage = releasesMessage;
    }
}