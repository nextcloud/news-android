package de.luhmer.owncloudnewsreader.reader.nextcloud;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import de.luhmer.owncloudnewsreader.database.model.Feed;
import de.luhmer.owncloudnewsreader.database.model.Folder;
import de.luhmer.owncloudnewsreader.database.model.RssItem;

/**
 * Created by david on 24.05.17.
 */

public class NextcloudNewsDeserializer<T> implements JsonDeserializer<List<T>> {

    private final String mKey;
    private final Class<T> mType;


    public NextcloudNewsDeserializer(String key, Class<T> type) {
        this.mKey = key;
        this.mType = type;
    }

    public static final String TAG = NextcloudNewsDeserializer.class.getCanonicalName();

    @Override
    public List<T> deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException {
        JsonArray jArr = json.getAsJsonObject().getAsJsonArray(mKey);

        List<T> items = new ArrayList<>();
        for(int i = 0; i < jArr.size(); i++) {
            if(mType == Folder.class) {
                items.add((T) parseFolder(jArr.get(i).getAsJsonObject()));
            } else if(mType == Feed.class) {
                items.add((T) parseFeed(jArr.get(i).getAsJsonObject()));
            } else if(mType == RssItem.class) {
                items.add((T) InsertRssItemIntoDatabase.parseItem(jArr.get(i).getAsJsonObject()));
            }
        }

        return items;
    }


    private Folder parseFolder(JsonObject e) {
        return new Folder(e.get("id").getAsLong(), getNullAsEmptyString(e.get("name")));
    }

    private Feed parseFeed(JsonObject e) {
        String faviconLink = getNullAsEmptyString(e.get("faviconLink"));
        if(faviconLink != null)
            if(faviconLink.equals("null") || faviconLink.trim().equals(""))
                faviconLink = null;

        Feed feed = new Feed();
        feed.setNotificationChannel("default");
        feed.setId(e.get("id").getAsLong());

        JsonElement folderId = e.get("folderId");
        if(folderId.isJsonNull()) {
            feed.setFolderId(0L);
        } else {
            feed.setFolderId(folderId.getAsLong());
        }

        feed.setFaviconUrl(faviconLink);

        //Possible XSS fields
        feed.setFeedTitle(getNullAsEmptyString(e.get("title")));
        feed.setLink(getNullAsEmptyString(e.get("url")));
        //feed.setLink(e.optString("link"));

        return feed;
    }


    private String getNullAsEmptyString(JsonElement jsonElement) {
        return jsonElement.isJsonNull() ? "" : jsonElement.getAsString();
    }
}
