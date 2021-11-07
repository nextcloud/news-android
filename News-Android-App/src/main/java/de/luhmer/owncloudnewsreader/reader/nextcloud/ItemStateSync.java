package de.luhmer.owncloudnewsreader.reader.nextcloud;

import android.util.Log;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import de.luhmer.owncloudnewsreader.database.DatabaseConnectionOrm;
import de.luhmer.owncloudnewsreader.reader.FeedItemTags;
import retrofit2.Response;

/**
 * Created by david on 26.05.17.
 */

public class ItemStateSync {

    private static final String TAG = ItemStateSync.class.getCanonicalName();

    public static void PerformItemStateSync(NewsAPI newsApi, DatabaseConnectionOrm dbConn) throws IOException {
        int MAX_SYNC_ITEMS_PER_REQUEST = 300;

        Map<FeedItemTags, List<String>> itemsToSync = new HashMap<>();
        itemsToSync.put(
                FeedItemTags.MARK_ITEM_AS_READ,
                dbConn.getRssItemsIdsFromList(dbConn.getAllNewReadRssItems())
        );
        itemsToSync.put(
                FeedItemTags.MARK_ITEM_AS_UNREAD,
                dbConn.getRssItemsIdsFromList(dbConn.getAllNewUnreadRssItems())
        );
        itemsToSync.put(
                FeedItemTags.MARK_ITEM_AS_STARRED,
                dbConn.getRssItemsIdsFromList(dbConn.getAllNewStarredRssItems())
        );
        itemsToSync.put(
                FeedItemTags.MARK_ITEM_AS_UNSTARRED,
                dbConn.getRssItemsIdsFromList(dbConn.getAllNewUnstarredRssItems())
        );

        Log.d(TAG, "itemsToSync[MARK_ITEM_AS_READ]:" + itemsToSync.get(FeedItemTags.MARK_ITEM_AS_READ).size());
        Log.d(TAG, "itemsToSync[MARK_ITEM_AS_UNREAD]:" + itemsToSync.get(FeedItemTags.MARK_ITEM_AS_UNREAD).size());
        Log.d(TAG, "itemsToSync[MARK_ITEM_AS_STARRED]:" + itemsToSync.get(FeedItemTags.MARK_ITEM_AS_STARRED).size());
        Log.d(TAG, "itemsToSync[MARK_ITEM_AS_UNSTARRED]:" + itemsToSync.get(FeedItemTags.MARK_ITEM_AS_UNSTARRED).size());


        for(Map.Entry<FeedItemTags, List<String>> entry : itemsToSync.entrySet()) {
            FeedItemTags operation = entry.getKey();
            Collection<List<String>> itemIdsPartitioned = partitionBasedOnSize(entry.getValue(), MAX_SYNC_ITEMS_PER_REQUEST);
            for(List<String> itemIds : itemIdsPartitioned) {
                Log.d(TAG, "Marking " + itemIds.size() + " items as " + operation.toString());
                PerformTagExecution(itemIds, operation, dbConn, newsApi);
            }
        }
    }

    static <T> Collection<List<T>> partitionBasedOnSize(List<T> inputList, int size) {
        final AtomicInteger counter = new AtomicInteger(0);
        return inputList.stream()
                .collect(Collectors.groupingBy(s -> counter.getAndIncrement() / size))
                .values();
    }

    private static void executeRequest(ExecuteRequestCallable<Response> data, OnSuccessCallable<Void> onSuccess) throws IOException {
        Response response = data.call();
        if (response.isSuccessful()) {
            onSuccess.call();
        } else {
            if (response.errorBody() != null) {
                throw new IOException(response.errorBody().toString());
            } else {
                throw new IOException("mark item as read failed - http code: " + response.code());
            }
        }

    }

    private static void PerformTagExecution(List<String> itemIds, FeedItemTags tag, DatabaseConnectionOrm dbConn, NewsAPI newsApi) throws IOException {
        if (itemIds.size() <= 0) { // Nothing to sync --> Skip
            return;
        }

        switch (tag) {
            case MARK_ITEM_AS_READ:
                executeRequest(
                        () -> newsApi.markItemsRead(new ItemIds(itemIds)).execute(),
                        () -> dbConn.change_readUnreadStateOfItem(itemIds, true)
                );
                break;
            case MARK_ITEM_AS_UNREAD:
                executeRequest(
                        () -> newsApi.markItemsUnread(new ItemIds(itemIds)).execute(),
                        () -> dbConn.change_readUnreadStateOfItem(itemIds, false)
                );
                break;
            case MARK_ITEM_AS_STARRED:
                executeRequest(
                        () -> newsApi.markItemsStarred(new ItemMap(itemIds, dbConn)).execute(),
                        () -> dbConn.changeStarrUnstarrStateOfItem(itemIds, true)
                );
                break;
            case MARK_ITEM_AS_UNSTARRED:
                executeRequest(
                        () -> newsApi.markItemsUnstarred(new ItemMap(itemIds, dbConn)).execute(),
                        () -> dbConn.changeStarrUnstarrStateOfItem(itemIds, false)
                );
                break;
        }
    }

    interface ExecuteRequestCallable<T> {
        T call() throws IOException;
    }

    interface OnSuccessCallable<T> {
        void call();
    }
}
