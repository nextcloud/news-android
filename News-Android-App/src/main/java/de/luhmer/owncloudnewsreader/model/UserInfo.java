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
        private Long lastLoginTimestamp;

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

        public Builder setLastLoginTimestamp(Long lastLoginTimestamp) {
            this.lastLoginTimestamp = lastLoginTimestamp;
            return this;
        }

        public UserInfo build() {
            return new UserInfo(userId, displayName, avatar, lastLoginTimestamp);
        }
    }

    private UserInfo(String userId, String displayName, Bitmap avatar, Long lastLoginTimestamp) {
        this.userId = userId;
        this.displayName = displayName;
        this.avatar = avatar;
        this.lastLoginTimestamp = lastLoginTimestamp;
    }

    public final String userId;
    public final String displayName;
    public final Bitmap avatar;
    public final Long lastLoginTimestamp;
}
