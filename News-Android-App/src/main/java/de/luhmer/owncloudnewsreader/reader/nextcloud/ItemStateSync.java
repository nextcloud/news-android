package de.luhmer.owncloudnewsreader.reader.nextcloud;

import java.io.IOException;
import java.util.List;

import de.luhmer.owncloudnewsreader.database.DatabaseConnectionOrm;
import de.luhmer.owncloudnewsreader.reader.FeedItemTags;

/**
 * Created by david on 26.05.17.
 */

public class ItemStateSync {

    public static void PerformItemStateSync(API api, DatabaseConnectionOrm dbConn) throws IOException {
        //Mark as READ
        List<String> itemIds = dbConn.getRssItemsIdsFromList(dbConn.getAllNewReadRssItems());
        boolean result = PerformTagExecution(itemIds, FeedItemTags.MARK_ITEM_AS_READ, dbConn, api);
        if(result)
            dbConn.change_readUnreadStateOfItem(itemIds, true);

        //Mark as UNREAD
        itemIds = dbConn.getRssItemsIdsFromList(dbConn.getAllNewUnreadRssItems());
        result = PerformTagExecution(itemIds, FeedItemTags.MARK_ITEM_AS_UNREAD, dbConn, api);
        if(result)
            dbConn.change_readUnreadStateOfItem(itemIds, false);

        //Mark as STARRED
        itemIds = dbConn.getRssItemsIdsFromList(dbConn.getAllNewStarredRssItems());
        result = PerformTagExecution(itemIds, FeedItemTags.MARK_ITEM_AS_STARRED, dbConn, api);
        if(result)
            dbConn.changeStarrUnstarrStateOfItem(itemIds, true);

        //Mark as UNSTARRED
        itemIds = dbConn.getRssItemsIdsFromList(dbConn.getAllNewUnstarredRssItems());
        result = PerformTagExecution(itemIds, FeedItemTags.MARK_ITEM_AS_UNSTARRED, dbConn, api);
        if(result)
            dbConn.changeStarrUnstarrStateOfItem(itemIds, false);

    }

    private static boolean PerformTagExecution(List<String> itemIds, FeedItemTags tag, DatabaseConnectionOrm dbConn, API api) throws IOException {
        if(itemIds.size() <= 0) { // Nothing to sync --> Skip
            return true;
        }

        switch(tag) {
            case MARK_ITEM_AS_READ:
                return api.markItemsRead(new ItemIds(itemIds)).execute().isSuccessful();
            case MARK_ITEM_AS_UNREAD:
                return api.markItemsUnread(new ItemIds(itemIds)).execute().isSuccessful();
            case MARK_ITEM_AS_STARRED:
                return api.markItemsStarred(new ItemMap(itemIds, dbConn)).execute().isSuccessful();
            case MARK_ITEM_AS_UNSTARRED:
                return api.markItemsUnstarred(new ItemMap(itemIds, dbConn)).execute().isSuccessful();
        }
        return false;
    }
}
