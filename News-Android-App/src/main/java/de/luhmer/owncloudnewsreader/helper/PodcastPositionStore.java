package de.luhmer.owncloudnewsreader.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Remembers the last playback position of podcast episodes (see issue #504).
 *
 * Positions are kept in a dedicated SharedPreferences file (not the main
 * settings file) keyed by rss item id. The item fingerprint is stored
 * alongside the position so a stale entry is ignored when the enclosure
 * changed or the item id belongs to a different account.
 */
public class PodcastPositionStore {

    private static final String TAG = PodcastPositionStore.class.getCanonicalName();

    private static final String PREF_FILE_SUFFIX = "_podcast_positions";
    private static final String KEY_PREFIX = "pos_";
    private static final String SEPARATOR = "|";

    private final SharedPreferences mPrefs;

    public PodcastPositionStore(Context context) {
        mPrefs = context.getSharedPreferences(context.getPackageName() + PREF_FILE_SUFFIX, Context.MODE_PRIVATE);
    }

    /**
     * @return the stored position in milliseconds, or 0 if there is no valid
     *         stored position for this item / fingerprint combination.
     */
    public long getPosition(long itemId, String fingerprint) {
        String value = mPrefs.getString(KEY_PREFIX + itemId, null);
        if (value == null) {
            return 0;
        }

        String[] parts = value.split("\\" + SEPARATOR, 3);
        if (parts.length != 3) {
            return 0;
        }

        try {
            long position = Long.parseLong(parts[0]);
            long duration = Long.parseLong(parts[1]);
            if (position <= 0 || position >= duration || !parts[2].equals(fingerprint)) {
                return 0;
            }
            return position;
        } catch (NumberFormatException e) {
            Log.w(TAG, "Ignoring invalid stored podcast position: " + value);
            return 0;
        }
    }

    public void savePosition(long itemId, String fingerprint, long positionMillis, long durationMillis) {
        String value = positionMillis + SEPARATOR + durationMillis + SEPARATOR + fingerprint;
        mPrefs.edit().putString(KEY_PREFIX + itemId, value).apply();
    }

    public void clearPosition(long itemId) {
        mPrefs.edit().remove(KEY_PREFIX + itemId).apply();
    }

    public void clearAll() {
        mPrefs.edit().clear().apply();
    }

    public List<Long> getStoredItemIds() {
        List<Long> itemIds = new ArrayList<>();
        for (String key : mPrefs.getAll().keySet()) {
            if (key.startsWith(KEY_PREFIX)) {
                try {
                    itemIds.add(Long.parseLong(key.substring(KEY_PREFIX.length())));
                } catch (NumberFormatException e) {
                    Log.w(TAG, "Ignoring invalid podcast position key: " + key);
                }
            }
        }
        return itemIds;
    }

    public void removePositions(Collection<Long> itemIds) {
        if (itemIds.isEmpty()) {
            return;
        }
        SharedPreferences.Editor editor = mPrefs.edit();
        for (Long itemId : itemIds) {
            editor.remove(KEY_PREFIX + itemId);
        }
        editor.apply();
    }
}
