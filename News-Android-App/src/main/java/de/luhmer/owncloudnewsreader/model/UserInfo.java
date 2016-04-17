package de.luhmer.owncloudnewsreader.model;

import android.graphics.Bitmap;

import java.io.Serializable;

/**
 * Created by dluhmer on 19.03.16.
 */
public class UserInfo implements Serializable {

    public static class Builder {
        private String userId;
        private String displayName;
        private Bitmap avatar;

        public Builder setUserId(String userId) {
            this.userId = userId;
            return this;
        }

        public Builder setDisplayName(String displayName) {
            this.displayName = displayName;
            return this;
        }

        public Builder setAvatar(Bitmap avatar) {
            this.avatar = avatar;
            return this;
        }

        public UserInfo build() {
            return new UserInfo(userId, displayName, avatar);
        }
    }

    private UserInfo(String userId, String displayName, Bitmap avatar) {
        this.mUserId = userId;
        this.mDisplayName = displayName;
        this.mAvatar = avatar;
    }

    public final String mUserId;
    public final String mDisplayName;
    public final Bitmap mAvatar;
}
