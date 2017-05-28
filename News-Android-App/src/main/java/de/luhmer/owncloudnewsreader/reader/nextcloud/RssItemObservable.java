package de.luhmer.owncloudnewsreader.reader.nextcloud;

import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import de.luhmer.owncloudnewsreader.Constants;
import de.luhmer.owncloudnewsreader.database.DatabaseConnectionOrm;
import de.luhmer.owncloudnewsreader.database.model.RssItem;
import de.luhmer.owncloudnewsreader.reader.FeedItemTags;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import okhttp3.ResponseBody;
import okio.BufferedSource;

/**
 * onNext returns the current amount of synced items
 */
public class RssItemObservable implements Publisher<Integer> {

    private DatabaseConnectionOrm mDbConn;
    private API mApi;
    private SharedPreferences mPrefs;
    private static final String TAG = RssItemObservable.class.getCanonicalName();
    private static int maxSizePerSync = 300;

    public RssItemObservable(DatabaseConnectionOrm dbConn, API api, SharedPreferences prefs) {
        this.mDbConn = dbConn;
        this.mApi = api;
        this.mPrefs = prefs;
    }

    @Override
    public void subscribe(Subscriber<? super Integer> s) {
        try {
            //throw new RuntimeException("");
            sync(s);
            s.onComplete();
        } catch (Exception ex) {
            s.onError(ex);
        }
    }

    public void sync(Subscriber<? super Integer> subscriber) throws IOException {

        mDbConn.clearDatabaseOverSize();

        //String authKey = AuthenticationManager.getGoogleAuthKey(username, password);
        //SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        //int maxItemsInDatabase = Integer.parseInt(mPrefs.getString(SettingsActivity.SP_MAX_ITEMS_SYNC, "200"));

        long lastModified = mDbConn.getLastModified();
        //dbConn.clearDatabaseOverSize();

        int requestCount = 0;
        int totalCount   = 0;
        int maxSyncSize = maxSizePerSync;

        if(lastModified == 0)//Only on first sync
        {
            long offset = 0;

            Log.v(TAG, "First sync!!");
            int maxItemsInDatabase = Constants.maxItemsCount;

            do {
                Log.v(TAG, "offset=" + offset + ",  requestCount=" + requestCount + "");
                List<RssItem> buffer = (mApi.items(maxSyncSize, offset, Integer.valueOf(FeedItemTags.ALL.toString()), 0, false, true).execute().body());

                requestCount = 0;
                if(buffer != null) {
                    requestCount = buffer.size();
                    performDatabaseBatchInsert(mDbConn, buffer);
                }

                if(requestCount > 0)
                    offset = mDbConn.getHighestItemId();
                totalCount += requestCount;

                subscriber.onNext(totalCount);
            } while(requestCount == maxSyncSize);

            Log.v(TAG, "offset=" + offset + ",  requestCount=" + requestCount + ", maxSyncSize=" + maxSyncSize);

            Log.v(TAG, "Sync all items done - Starting starred now");

            mPrefs.edit().putInt(Constants.LAST_UPDATE_NEW_ITEMS_COUNT_STRING, totalCount).apply();

            do {
                offset = mDbConn.getLowestItemId(true);
                List<RssItem> buffer = mApi.items(maxSyncSize, offset, Integer.valueOf(FeedItemTags.ALL_STARRED.toString()), 0, false, true).execute().body();

                requestCount = 0;
                if(buffer != null) {
                    requestCount = buffer.size();
                    performDatabaseBatchInsert(mDbConn, buffer);
                }
                //if(requestCount > 0)
                //	offset = dbConn.getLowestItemId(true);
                totalCount += requestCount;

                subscriber.onNext(totalCount);
            } while(requestCount == maxSyncSize && totalCount < maxItemsInDatabase);
        }
        else
        {
            Log.v(TAG, "Incremental sync!!");
            //First reset the count of last updated items
            mPrefs.edit().putInt(Constants.LAST_UPDATE_NEW_ITEMS_COUNT_STRING, 0).apply();

            long highestItemIdBeforeSync = mDbConn.getHighestItemId();

            //Get all updated items
            mApi.updatedItems(lastModified+1, Integer.valueOf(FeedItemTags.ALL.toString()), highestItemIdBeforeSync)
                    .flatMap(new Function<ResponseBody, ObservableSource<RssItem>>() {
                        @Override
                        public ObservableSource<RssItem> apply(@NonNull ResponseBody responseBody) throws Exception {
                            return events(responseBody.source());
                        }
                    })
                    .subscribe(new Observer<RssItem>() {
                        int totalUpdatedUnreadItemCount = 0;
                        final int bufferSize = 150;
                        List<RssItem> buffer = new ArrayList<>(bufferSize); //Buffer of size X

                        @Override
                        public void onSubscribe(@NonNull Disposable d) {
                            Log.v(TAG, "onSubscribe() called with: d = [" + d + "]");
                        }

                        @Override
                        public void onNext(@NonNull RssItem rssItem) {
                            if(!rssItem.getRead()) { //If updates item is unread
                                totalUpdatedUnreadItemCount++;
                            }

                            buffer.add(rssItem);
                            if (buffer.size() >= bufferSize) {
                                performDatabaseBatchInsert(mDbConn, buffer);
                            }
                        }

                        @Override
                        public void onError(@NonNull Throwable e) {
                            Log.v(TAG, "onError() called with: e = [" + e + "]");
                        }

                        @Override
                        public void onComplete() {
                            Log.v(TAG, "onComplete() called");
                            performDatabaseBatchInsert(mDbConn, buffer);

                            //If no exception occurs, set the number of updated items
                            mPrefs.edit().putInt(Constants.LAST_UPDATE_NEW_ITEMS_COUNT_STRING, totalUpdatedUnreadItemCount).apply();
                        }
                    });
        }
    }

    public static boolean performDatabaseBatchInsert(DatabaseConnectionOrm dbConn, List<RssItem> buffer) {
        Log.v(TAG, "performDatabaseBatchInsert() called with: dbConn = [" + dbConn + "], buffer = [" + buffer + "]");
        dbConn.insertNewItems(buffer);
        buffer.clear();
        return true;
    }

    public static Observable<RssItem> events(final BufferedSource source) {
        return Observable.create(new ObservableOnSubscribe<RssItem>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<RssItem> e) throws Exception {
                try {
                    InputStreamReader isr = new InputStreamReader(source.inputStream());
                    BufferedReader br = new BufferedReader(isr);
                    JsonReader reader = new JsonReader(br);

                    try {
                        reader.beginObject();

                        String currentName;
                        while(reader.hasNext() && (currentName = reader.nextName()) != null) {
                            if(currentName.equals("items")) {
                                break;
                            } else {
                                reader.skipValue();
                            }
                        }

                        reader.beginArray();
                        while (reader.hasNext()) {
                            JsonObject jsonObj = getJsonObjectFromReader(reader);
                            RssItem item = InsertRssItemIntoDatabase.parseItem(jsonObj);
                            e.onNext(item);
                        }
                        reader.endArray();
                    } finally {
                        reader.close();
                        br.close();
                        isr.close();
                    }
                } catch (IOException err) {
                    err.printStackTrace();
                    e.onError(err);
                }
                e.onComplete();
            }
        });
    }


    private static JsonObject getJsonObjectFromReader(JsonReader jsonReader) {
        JsonObject jObj = new JsonObject();
        JsonToken tokenInstance;
        try {
            tokenInstance = jsonReader.peek();
            if(tokenInstance == JsonToken.BEGIN_OBJECT)
                jsonReader.beginObject();
            else if (tokenInstance == JsonToken.BEGIN_ARRAY)
                jsonReader.beginArray();

            while(jsonReader.hasNext()) {
                JsonToken token;
                String name;
                try {
                    name = jsonReader.nextName();
                    token = jsonReader.peek();

                    //Log.d(TAG, token.toString());

                    switch(token) {
                        case NUMBER:
                            jObj.addProperty(name, jsonReader.nextLong());
                            break;
                        case NULL:
                            jsonReader.skipValue();
                            break;
                        case BOOLEAN:
                            jObj.addProperty(name, jsonReader.nextBoolean());
                            break;
                        case BEGIN_OBJECT:
                            jObj.add(name, getJsonObjectFromReader(jsonReader));
                            break;
                        case BEGIN_ARRAY:
                            jsonReader.skipValue();
                            break;
                        default:
                            jObj.addProperty(name, jsonReader.nextString());
                    }
                } catch(Exception ex) {
                    ex.printStackTrace();
                    jsonReader.skipValue();
                }
            }

            if(tokenInstance == JsonToken.BEGIN_OBJECT)
                jsonReader.endObject();
            else if (tokenInstance == JsonToken.BEGIN_ARRAY)
                jsonReader.endArray();

            return jObj;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
