package com.idirect.app.ui.userprofile;

import android.os.Parcel;
import android.os.Parcelable;

public class UserBundle implements Parcelable {

    private String username;
    private String userId;
    private String profilePic;
    private String fullname;

    public UserBundle(){

    }
    protected UserBundle(Parcel in) {
        username = in.readString();
        userId = in.readString();
        profilePic = in.readString();
        fullname = in.readString();
    }

    public static final Creator<UserBundle> CREATOR = new Creator<UserBundle>() {
        @Override
        public UserBundle createFromParcel(Parcel in) {
            return new UserBundle(in);
        }

        @Override
        public UserBundle[] newArray(int size) {
            return new UserBundle[size];
        }
    };

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(username);
        dest.writeString(userId);
        dest.writeString(profilePic);
        dest.writeString(fullname);
    }
}
