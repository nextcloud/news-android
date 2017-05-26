package de.luhmer.owncloudnewsreader.di;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.lang.reflect.Type;
import java.util.List;

import de.luhmer.owncloudnewsreader.SettingsActivity;
import de.luhmer.owncloudnewsreader.database.model.Feed;
import de.luhmer.owncloudnewsreader.database.model.Folder;
import de.luhmer.owncloudnewsreader.database.model.RssItem;
import de.luhmer.owncloudnewsreader.model.UserInfo;
import de.luhmer.owncloudnewsreader.reader.OkHttpImageDownloader;
import de.luhmer.owncloudnewsreader.reader.nextcloud.API;
import de.luhmer.owncloudnewsreader.reader.nextcloud.NextcloudDeserializer;
import de.luhmer.owncloudnewsreader.reader.nextcloud.Types;
import de.luhmer.owncloudnewsreader.ssl.MemorizingTrustManager;
import de.luhmer.owncloudnewsreader.ssl.OkHttpSSLClient;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by david on 26.05.17.
 */

public class ApiProvider {

    private final MemorizingTrustManager mMemorizingTrustManager;
    private final SharedPreferences mPrefs;
    private API mApi;
    private Context context;

    public ApiProvider(MemorizingTrustManager mtm, SharedPreferences sp, Context context) {
        this.mMemorizingTrustManager = mtm;
        this.mPrefs = sp;
        this.context = context;
        initApi();
    }

    public void initApi() {
        String username   = mPrefs.getString(SettingsActivity.EDT_USERNAME_STRING, "");
        String password   = mPrefs.getString(SettingsActivity.EDT_PASSWORD_STRING, "");
        String baseUrlStr = mPrefs.getString(SettingsActivity.EDT_OWNCLOUDROOTPATH_STRING, "https://luhmer.de"); // We need to provide some sort of default URL here..
        HttpUrl baseUrl = HttpUrl.parse(baseUrlStr).newBuilder()
                .addPathSegments("index.php/apps/news/api/v1-2/")
                .build();

        Log.d("ApiModule", "HttpUrl: " + baseUrl.toString());

        Type feedList = new TypeToken<List<Feed>>() {}.getType();
        Type folderList = new TypeToken<List<Folder>>() {}.getType();
        Type rssItemsList = new TypeToken<List<RssItem>>() {}.getType();

        // Info: RssItems are handled as a stream (to be more memory efficient - see @OwnCloudSyncService and @RssItemObservable)
        Gson gson = new GsonBuilder()
                .setLenient()
                .registerTypeAdapter(folderList,   new NextcloudDeserializer<>(Types.FOLDERS.toString(), Folder.class))
                .registerTypeAdapter(feedList,     new NextcloudDeserializer<>(Types.FEEDS.toString(), Feed.class))
                .registerTypeAdapter(rssItemsList, new NextcloudDeserializer<>(Types.ITEMS.toString(), RssItem.class))
                .registerTypeAdapter(UserInfo.class, new JsonDeserializer<UserInfo>() {
                    @Override
                    public UserInfo deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
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
                    }
                })
                .create();

        OkHttpClient client = OkHttpSSLClient.GetSslClient(baseUrl, username, password, mPrefs, mMemorizingTrustManager);
        Retrofit retrofit = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .baseUrl(baseUrl)
                .client(client)
                .build();

        initImageLoader(mPrefs, client, context);

        mApi = retrofit.create(API.class);
    }

    private void initImageLoader(SharedPreferences mPrefs, OkHttpClient okHttpClient, Context context) {
        int diskCacheSize = Integer.parseInt(mPrefs.getString(SettingsActivity.SP_MAX_CACHE_SIZE,"500"))*1024*1024;
        if(ImageLoader.getInstance().isInited()) {
            ImageLoader.getInstance().destroy();
        }
        DisplayImageOptions imageOptions = new DisplayImageOptions.Builder()
                .cacheOnDisk(true)
                .cacheInMemory(true)
                .build();

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
                .diskCacheSize(diskCacheSize)
                .memoryCacheSize(10 * 1024 * 1024)
                .diskCacheFileNameGenerator(new Md5FileNameGenerator())
                .defaultDisplayImageOptions(imageOptions)
                .imageDownloader(new OkHttpImageDownloader(context, okHttpClient))
                .build();

        ImageLoader.getInstance().init(config);
    }

    public API getAPI() {
        return mApi;
    }
}
