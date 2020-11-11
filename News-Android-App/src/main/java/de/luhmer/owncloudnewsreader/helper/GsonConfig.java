package de.luhmer.owncloudnewsreader.helper;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

import de.luhmer.owncloudnewsreader.database.model.Feed;
import de.luhmer.owncloudnewsreader.database.model.Folder;
import de.luhmer.owncloudnewsreader.database.model.RssItem;
import de.luhmer.owncloudnewsreader.model.OcsUser;
import de.luhmer.owncloudnewsreader.reader.nextcloud.NextcloudNewsDeserializer;
import de.luhmer.owncloudnewsreader.reader.nextcloud.NextcloudServerDeserializer;
import de.luhmer.owncloudnewsreader.reader.nextcloud.Types;

/**
 * Created by david on 27.06.17.
 */

public class GsonConfig {

    public static Gson GetGson() {
        Type feedList = new TypeToken<List<Feed>>() {}.getType();
        Type folderList = new TypeToken<List<Folder>>() {}.getType();
        Type rssItemsList = new TypeToken<List<RssItem>>() {}.getType();
        Type ocsUser = new TypeToken<OcsUser>() {}.getType();

        // Info: RssItems are handled as a stream (to be more memory efficient - see @OwnCloudSyncService and @RssItemObservable)
        return new GsonBuilder()
                .setLenient()
                .registerTypeAdapter(folderList,   new NextcloudNewsDeserializer<>(Types.FOLDERS.toString(), Folder.class))
                .registerTypeAdapter(feedList,     new NextcloudNewsDeserializer<>(Types.FEEDS.toString(), Feed.class))
                .registerTypeAdapter(rssItemsList, new NextcloudNewsDeserializer<>(Types.ITEMS.toString(), RssItem.class))
                .registerTypeAdapter(ocsUser,      new NextcloudServerDeserializer<>("ocsUser", OcsUser.class))
                .create();
    }

}