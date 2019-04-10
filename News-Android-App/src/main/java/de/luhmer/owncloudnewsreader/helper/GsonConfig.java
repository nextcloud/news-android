package de.luhmer.owncloudnewsreader.helper;

import android.graphics.BitmapFactory;
import android.util.Base64;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

import de.luhmer.owncloudnewsreader.database.model.Feed;
import de.luhmer.owncloudnewsreader.database.model.Folder;
import de.luhmer.owncloudnewsreader.database.model.RssItem;
import de.luhmer.owncloudnewsreader.model.UserInfo;
import de.luhmer.owncloudnewsreader.reader.nextcloud.NextcloudDeserializer;
import de.luhmer.owncloudnewsreader.reader.nextcloud.Types;
import de.luhmer.owncloudnewsreader.ssl.OkHttpSSLClient;

/**
 * Created by david on 27.06.17.
 */

public class GsonConfig {

    public static Gson GetGson() {
        Type feedList = new TypeToken<List<Feed>>() {}.getType();
        Type folderList = new TypeToken<List<Folder>>() {}.getType();
        Type rssItemsList = new TypeToken<List<RssItem>>() {}.getType();

        // Info: RssItems are handled as a stream (to be more memory efficient - see @OwnCloudSyncService and @RssItemObservable)
        return new GsonBuilder()
                .setLenient()
                .registerTypeAdapter(folderList,   new NextcloudDeserializer<>(Types.FOLDERS.toString(), Folder.class))
                .registerTypeAdapter(feedList,     new NextcloudDeserializer<>(Types.FEEDS.toString(), Feed.class))
                .registerTypeAdapter(rssItemsList, new NextcloudDeserializer<>(Types.ITEMS.toString(), RssItem.class))
                .registerTypeAdapter(UserInfo.class, (JsonDeserializer<UserInfo>) (json, typeOfT, context) -> {
                    try {
                        JsonObject jObj = json.getAsJsonObject();
                        JsonElement avatar = jObj.get("avatar");
                        byte[] decodedString = {};
                        if (!avatar.isJsonNull()) {
                            decodedString = Base64.decode(avatar.getAsJsonObject().get("data").getAsString(), Base64.DEFAULT);
                        }
                        return new UserInfo.Builder()
                                .setDisplayName(jObj.get("displayName").getAsString())
                                .setUserId(jObj.get("userId").getAsString())
                                .setAvatar(BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length))
                                .setLastLoginTimestamp(jObj.get("lastLoginTimestamp").getAsLong())
                                .build();
                    } catch(IllegalStateException ex) {
                        throw OkHttpSSLClient.HandleExceptions(ex);
                    }
                })
                .create();
    }

}