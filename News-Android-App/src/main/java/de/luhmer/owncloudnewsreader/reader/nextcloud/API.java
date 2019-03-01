package de.luhmer.owncloudnewsreader.reader.nextcloud;

import java.util.List;
import java.util.Map;

import de.luhmer.owncloudnewsreader.database.model.Feed;
import de.luhmer.owncloudnewsreader.database.model.Folder;
import de.luhmer.owncloudnewsreader.database.model.RssItem;
import de.luhmer.owncloudnewsreader.model.NextcloudNewsVersion;
import de.luhmer.owncloudnewsreader.model.NextcloudStatus;
import de.luhmer.owncloudnewsreader.model.UserInfo;
import io.reactivex.Completable;
import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Streaming;

/**
 * Created by david on 22.05.17.
 */


public interface API {

    String mApiEndpoint = "/index.php/apps/news/api/v1-2/";

    /** Since 6.0.5 **/
    @GET("user")
    Observable<UserInfo> user();


    @GET("status")
    Observable<NextcloudStatus> status();

    @GET("version")
    Observable<NextcloudNewsVersion> version();


    /** FOLDERS **/
    @GET("folders")
    Observable<List<Folder>> folders();

    /** FEEDS **/
    @GET("feeds")
    Observable<List<Feed>> feeds();

    @POST("folders")
    Call<List<Folder>> createFolder(@Body Map<String, Object> folderMap);

    @POST("feeds")
    Call<List<Feed>> createFeed(@Body Map<String, Object> feedMap);


    @PUT("feeds/{feedId}/rename")
    Completable renameFeed(@Path("feedId") long feedId, @Body Map<String, String> paramMap);


    @PUT("feeds/{feedId}/move")
    Completable moveFeed(@Path("feedId") long feedId, @Body Map<String,Long> folderIdMap);


    @DELETE("feeds/{feedId}")
    Completable deleteFeed(@Path("feedId") long feedId);


    /** ITEMS **/
    @GET("items")
    Call<List<RssItem>> items(
            @Query("batchSize") long batchSize,
            @Query("offset") long offset,
            @Query("type") int type,
            @Query("id") long id,
            @Query("getRead") boolean getRead,
            @Query("oldestFirst") boolean oldestFirst
    );

    @GET("items/updated")
    @Streaming
    Observable<ResponseBody> updatedItems(
            @Query("lastModified") long lastModified,
            @Query("type") int type,
            @Query("id") long id
    );


    @PUT("items/read/multiple")
    Call<Void> markItemsRead(@Body ItemIds items);

    @PUT("items/unread/multiple")
    Call<Void> markItemsUnread(@Body ItemIds items);

    @PUT("items/star/multiple")
    Call<Void> markItemsStarred(@Body ItemMap itemMap);

    @PUT("items/unstar/multiple")
    Call<Void> markItemsUnstarred(@Body ItemMap itemMap);



}
