package de.luhmer.owncloudnewsreader.helper;

import android.content.Context;

import java.util.Calendar;
import java.util.Date;

import de.luhmer.owncloudnewsreader.R;

public class DateTimeFormatter {
    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;
    private static final int WEEK_MILLIS = 7 * DAY_MILLIS;

    public static String getTimeAgo(Date date) {
        Date now = Calendar.getInstance().getTime();
        final long diff = now.getTime() - date.getTime();

        if (diff < SECOND_MILLIS) {
            return "0";
        } else if (diff < MINUTE_MILLIS) {
            return diff / SECOND_MILLIS + "now";
        } else if (diff < 2 * MINUTE_MILLIS) {
            return "1m";
        } else if (diff < 59 * MINUTE_MILLIS) {
            return diff / MINUTE_MILLIS + "m";
        } else if (diff < 90 * MINUTE_MILLIS) {
            return "1h";
        } else if (diff < 24 * HOUR_MILLIS) {
            return diff / HOUR_MILLIS + "h";
        } else if (diff < 48 * HOUR_MILLIS) {
            return "1d";
        } else if (diff < 6 * DAY_MILLIS) {
            return diff / DAY_MILLIS + "d";
        } else if (diff < 11 * DAY_MILLIS) {
            return "1w";
        } else {
            return diff / WEEK_MILLIS + "w";
        }
    }
}
