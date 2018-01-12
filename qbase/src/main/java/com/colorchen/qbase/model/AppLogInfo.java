package com.colorchen.qbase.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by wangsye on 2017-12-5.
 */

public class AppLogInfo implements Parcelable {
    public String username;
    public String identifier;
    public String widgetVersion;
    public String platform;
    public String appId;
    public String appVersion;
    public String sessionKey;

    public AppLogInfo(String uid, String identifier, String widgetVersion, String platform, String appId, String appVersion, String sessionKey) {
        this.username = uid;
        this.identifier = identifier;
        this.widgetVersion = widgetVersion;
        this.platform = platform;
        this.appId = appId;
        this.appVersion = appVersion;
        this.sessionKey = sessionKey;
    }

    public AppLogInfo() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.username);
        dest.writeString(this.identifier);
        dest.writeString(this.widgetVersion);
        dest.writeString(this.platform);
        dest.writeString(this.appId);
        dest.writeString(this.appVersion);
        dest.writeString(this.sessionKey);
    }

    protected AppLogInfo(Parcel in) {
        this.username = in.readString();
        this.identifier = in.readString();
        this.widgetVersion = in.readString();
        this.platform = in.readString();
        this.appId = in.readString();
        this.appVersion = in.readString();
        this.sessionKey = in.readString();
    }

    public static final Creator<AppLogInfo> CREATOR = new Creator<AppLogInfo>() {
        @Override
        public AppLogInfo createFromParcel(Parcel source) {
            return new AppLogInfo(source);
        }

        @Override
        public AppLogInfo[] newArray(int size) {
            return new AppLogInfo[size];
        }
    };
}
