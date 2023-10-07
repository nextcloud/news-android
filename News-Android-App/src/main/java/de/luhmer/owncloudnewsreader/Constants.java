package de.luhmer.owncloudnewsreader;

import android.content.SharedPreferences;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Constants {
	public static final Boolean debugModeWidget = false;

    public static final int maxItemsCount = 5000;
    public static final String LAST_UPDATE_NEW_ITEMS_COUNT_STRING = "LAST_UPDATE_NEW_ITEMS_COUNT_STRING";
    public static final String NOTIFICATION_ACTION_STOP_STRING = "NOTIFICATION_STOP";
    public static final String NOTIFICATION_ACTION_MARK_ALL_AS_READ_STRING = "NOTIFICATION_MARK_ALL_AS_READ";
    protected static final String NEWS_WEB_VERSION_NUMBER_STRING = "NewsWebVersionNumber";

    protected static final int MIN_NEXTCLOUD_FILES_APP_VERSION_CODE = 30030052;

    public static final String USER_INFO_STRING = "USER_INFO";

    public static final String PREVIOUS_VERSION_CODE = "PREVIOUS_VERSION_CODE";

    protected static boolean isNextCloud(SharedPreferences prefs) {
        int[] version = extractVersionNumberFromString(prefs.getString(Constants.NEWS_WEB_VERSION_NUMBER_STRING, ""));
        if (version[0] == 0) {
            // not initialized yet..
            return true; // let's assume that it is nextcloud..
        }
        return version[0] >= 9;
    }

    private static int[] extractVersionNumberFromString(String appVersion) {
        Pattern p = Pattern.compile("(\\d+).(\\d+).(\\d+)");
        Matcher m = p.matcher(appVersion);

        int[] version = new int[] { 0, 0, 0 };
        if (m.matches()) {
            version[0] = Integer.parseInt(m.group(1));
            version[1] = Integer.parseInt(m.group(2));
            version[2] = Integer.parseInt(m.group(3));
        }
        return version;
    }
}
