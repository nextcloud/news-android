package de.luhmer.owncloudnewsreader.reader.nextcloud;

import com.google.gson.reflect.TypeToken;
import com.nextcloud.android.sso.aidl.NextcloudRequest;
import com.nextcloud.android.sso.api.NextcloudAPI;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.luhmer.owncloud.accountimporter.helper.Okhttp3Helper;
import de.luhmer.owncloud.accountimporter.helper.ReactivexHelper;
import de.luhmer.owncloud.accountimporter.helper.Retrofit2Helper;
import de.luhmer.owncloudnewsreader.database.model.Feed;
import de.luhmer.owncloudnewsreader.database.model.Folder;
import de.luhmer.owncloudnewsreader.database.model.RssItem;
import de.luhmer.owncloudnewsreader.helper.GsonConfig;
import de.luhmer.owncloudnewsreader.model.NextcloudNewsVersion;
import de.luhmer.owncloudnewsreader.model.NextcloudStatus;
import de.luhmer.owncloudnewsreader.model.UserInfo;
import io.reactivex.Completable;
import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.Call;


public class API_SSO implements API {

    private static final String mApiEndpoint = "/index.php/apps/news/api/v1-2/";
    private NextcloudAPI nextcloudAPI;

    public API_SSO(NextcloudAPI nextcloudAPI) {
        this.nextcloudAPI = nextcloudAPI;
    }

    public NextcloudAPI getNextcloudAPI() {
        return nextcloudAPI;
    }




    @Override
    public Observable<UserInfo> user() {
        final Type type = UserInfo.class;
        NextcloudRequest request = new NextcloudRequest.Builder()
                .setMethod("GET")
                .setUrl(mApiEndpoint + "user")
                .build();
        return nextcloudAPI.performRequestObservable(type, request);
    }

    @Override
    public Observable<NextcloudStatus> status() {
        Type type = NextcloudStatus.class;
        NextcloudRequest request = new NextcloudRequest.Builder()
                .setMethod("GET")
                .setUrl(mApiEndpoint + "status")
                .build();
        return nextcloudAPI.performRequestObservable(type, request);
    }

    @Override
    public Observable<NextcloudNewsVersion> version() {
        Type type = NextcloudNewsVersion.class;
        NextcloudRequest request = new NextcloudRequest.Builder()
                .setMethod("GET")
                .setUrl(mApiEndpoint + "version")
                .build();
        return nextcloudAPI.performRequestObservable(type, request);
    }

    @Override
    public Observable<List<Folder>> folders() {
        Type type = new TypeToken<List<Folder>>() {}.getType();
        NextcloudRequest request = new NextcloudRequest.Builder()
                .setMethod("GET")
                .setUrl(mApiEndpoint + "folders")
                .build();
        return nextcloudAPI.performRequestObservable(type, request);
    }

    @Override
    public Observable<List<Feed>> feeds() {
        Type type = new TypeToken<List<Feed>>() {}.getType();
        NextcloudRequest request = new NextcloudRequest.Builder()
                .setMethod("GET")
                .setUrl(mApiEndpoint + "feeds")
                .build();

        return nextcloudAPI.performRequestObservable(type, request);
    }

    @Override
    public Call<List<Folder>> createFolder(Map<String, Object> folderMap) {
        String body = GsonConfig.GetGson().toJson(folderMap);
        NextcloudRequest request = new NextcloudRequest.Builder()
                .setMethod("POST")
                .setUrl(mApiEndpoint + "folders")
                .setRequestBody(body)
                .build();
        return Retrofit2Helper.WrapInCall(nextcloudAPI, request, Folder.class);
    }


    @Override
    public Call<List<Feed>> createFeed(Map<String, Object> feedMap) {
        Type feedListType = new TypeToken<List<Feed>>() {}.getType();
        String body = GsonConfig.GetGson().toJson(feedMap);
        NextcloudRequest request = new NextcloudRequest.Builder()
                .setMethod("POST")
                .setUrl(mApiEndpoint + "feeds")
                .setRequestBody(body)
                .build();
        return Retrofit2Helper.WrapInCall(nextcloudAPI, request, feedListType);
    }

    @Override
    public Completable renameFeed(long feedId, Map<String, String> feedTitleMap) {
        String body = GsonConfig.GetGson().toJson(feedTitleMap);
        final NextcloudRequest request = new NextcloudRequest.Builder()
                .setMethod("PUT")
                .setUrl(mApiEndpoint + "feeds/" + feedId + "/rename")
                .setRequestBody(body)
                .build();
        return ReactivexHelper.WrapInCompletable(nextcloudAPI, request);
    }

    @Override
    public Completable deleteFeed(long feedId) {
        final NextcloudRequest request = new NextcloudRequest.Builder()
                .setMethod("DELETE")
                .setUrl(mApiEndpoint + "feeds/" + feedId)
                .build();
        return ReactivexHelper.WrapInCompletable(nextcloudAPI, request);
    }


    @Override
    public Call<List<RssItem>> items(long batchSize, long offset, int type, long id, boolean getRead, boolean oldestFirst) {
        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("batchSize", String.valueOf(batchSize));
        parameters.put("offset", String.valueOf(offset));
        parameters.put("type", String.valueOf(type));
        parameters.put("id", String.valueOf(id));
        parameters.put("getRead", String.valueOf(getRead));
        parameters.put("oldestFirst", String.valueOf(oldestFirst));

        Type resType = new TypeToken<List<RssItem>>() {}.getType();
        NextcloudRequest request = new NextcloudRequest.Builder()
                .setParameter(parameters)
                .setMethod("GET")
                .setUrl(mApiEndpoint + "items")
                .build();

        return Retrofit2Helper.WrapInCall(nextcloudAPI, request, resType);
    }

    @Override
    public Observable<ResponseBody> updatedItems(long lastModified, int type, long id) {
        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("lastModified", String.valueOf(lastModified));
        parameters.put("type", String.valueOf(type));
        parameters.put("id", String.valueOf(id));

        final NextcloudRequest request = new NextcloudRequest.Builder()
                .setMethod("GET")
                .setUrl(mApiEndpoint + "items/updated")
                .setParameter(parameters)
                .build();
        return Observable.just(Okhttp3Helper.getResponseBodyFromRequest(nextcloudAPI, request));
    }

    // https://github.com/owncloud/news/wiki/Items-1.2#mark-multiple-items-as-read
    @Override
    public Call<Void> markItemsRead(ItemIds items) {
        String body = GsonConfig.GetGson().toJson(items);
        return markItems("items/read/multiple", body);
    }

    // https://github.com/owncloud/news/wiki/Items-1.2#mark-multiple-items-as-read
    @Override
    public Call<Void> markItemsUnread(ItemIds items) {
        String body = GsonConfig.GetGson().toJson(items);
        return markItems("items/unread/multiple", body);
    }

    // https://github.com/owncloud/news/wiki/Items-1.2#mark-multiple-items-as-read
    @Override
    public Call<Void> markItemsStarred(ItemMap itemMap) {
        String body = GsonConfig.GetGson().toJson(itemMap);
        return markItems("items/star/multiple", body);
    }

    // https://github.com/owncloud/news/wiki/Items-1.2#mark-multiple-items-as-read
    @Override
    public Call<Void> markItemsUnstarred(ItemMap itemMap) {
        String body = GsonConfig.GetGson().toJson(itemMap);
        return markItems("items/unstar/multiple", body);
    }


    private Call<Void> markItems(String endpoint, String body) {
        NextcloudRequest request = new NextcloudRequest.Builder()
                .setMethod("PUT")
                .setUrl(mApiEndpoint + endpoint)
                .setRequestBody(body)
                .build();
        try {
            nextcloudAPI.performRequest(Void.class, request);
            return Retrofit2Helper.WrapVoidCall(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Retrofit2Helper.WrapVoidCall(false);
    }
}

