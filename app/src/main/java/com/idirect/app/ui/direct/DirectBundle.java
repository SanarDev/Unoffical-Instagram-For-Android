package com.idirect.app.ui.direct;

import android.os.Parcel;
import android.os.Parcelable;

public class DirectBundle implements Parcelable {

    private String name="";
    private String threadId;
    private String threadName;
    private String profileImage;
    private String profileImage2;
    private boolean isActive;
    private long lastActivityAt;
    private String username;
    private long userId;
    private boolean isGroup;


    public DirectBundle(){

    }
    protected DirectBundle(Parcel in) {
        name = in.readString();
        threadId = in.readString();
        threadName = in.readString();
        profileImage = in.readString();
        profileImage2 = in.readString();
        isActive = in.readByte() != 0;
        lastActivityAt = in.readLong();
        username = in.readString();
        userId = in.readLong();
        isGroup = in.readByte() != 0;
    }

    public static final Creator<DirectBundle> CREATOR = new Creator<DirectBundle>() {
        @Override
        public DirectBundle createFromParcel(Parcel in) {
            return new DirectBundle(in);
        }

        @Override
        public DirectBundle[] newArray(int size) {
            return new DirectBundle[size];
        }
    };

    public String getThreadTitle() {
        return threadName;
    }

    public void setThreadTitle(String threadName) {
        this.threadName = threadName;
    }

    public String getProfileImage2() {
        return profileImage2;
    }

    public void setProfileImage2(String profileImage2) {
        this.profileImage2 = profileImage2;
    }

    public boolean isGroup() {
        return isGroup;
    }

    public void setGroup(boolean group) {
        isGroup = group;
    }

    public static Creator<DirectBundle> getCREATOR() {
        return CREATOR;
    }

    public String getThreadId() {
        return threadId;
    }

    public void setThreadId(String threadId) {
        this.threadId = threadId;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public long getLastActivityAt() {
        return lastActivityAt;
    }

    public void setLastActivityAt(long lastActivityAt) {
        this.lastActivityAt = lastActivityAt;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(threadId);
        dest.writeString(threadName);
        dest.writeString(profileImage);
        dest.writeString(profileImage2);
        dest.writeByte((byte) (isActive ? 1 : 0));
        dest.writeLong(lastActivityAt);
        dest.writeString(username);
        dest.writeLong(userId);
        dest.writeByte((byte) (isGroup ? 1 : 0));
    }
}
