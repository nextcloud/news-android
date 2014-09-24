package de.luhmer.owncloudnewsreader.database;

import android.content.Context;
import android.database.Cursor;
import android.util.SparseArray;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import de.greenrobot.dao.query.LazyList;
import de.greenrobot.dao.query.WhereCondition;
import de.luhmer.owncloudnewsreader.Constants;
import de.luhmer.owncloudnewsreader.database.model.CurrentRssItemViewDao;
import de.luhmer.owncloudnewsreader.database.model.DaoSession;
import de.luhmer.owncloudnewsreader.database.model.Feed;
import de.luhmer.owncloudnewsreader.database.model.FeedDao;
import de.luhmer.owncloudnewsreader.database.model.Folder;
import de.luhmer.owncloudnewsreader.database.model.FolderDao;
import de.luhmer.owncloudnewsreader.database.model.RssItem;
import de.luhmer.owncloudnewsreader.database.model.RssItemDao;
import de.luhmer.owncloudnewsreader.model.PodcastFeedItem;
import de.luhmer.owncloudnewsreader.model.PodcastItem;
import de.luhmer.owncloudnewsreader.services.PodcastDownloadService;

import static de.luhmer.owncloudnewsreader.ListView.SubscriptionExpandableListAdapter.SPECIAL_FOLDERS;
import static de.luhmer.owncloudnewsreader.ListView.SubscriptionExpandableListAdapter.SPECIAL_FOLDERS.ALL_ITEMS;
import static de.luhmer.owncloudnewsreader.ListView.SubscriptionExpandableListAdapter.SPECIAL_FOLDERS.ALL_STARRED_ITEMS;
import static de.luhmer.owncloudnewsreader.ListView.SubscriptionExpandableListAdapter.SPECIAL_FOLDERS.ALL_UNREAD_ITEMS;

/**
 * Created by David on 15.07.2014.
 */
public class DatabaseConnectionOrm {

    public static final List<String> ALLOWED_PODCASTS_TYPES = new ArrayList<String>() {
        {
            this.add("audio/mp3");
            this.add("audio/mpeg");
            this.add("audio/ogg");
            this.add("audio/opus");
            this.add("audio/ogg;codecs=opus");
            this.add("youtube");
        }
    };

    public static final String[] VIDEO_FORMATS = { "youtube" };



    DaoSession daoSession;

    public void resetDatabase() {
        daoSession.getRssItemDao().deleteAll();
        daoSession.getFeedDao().deleteAll();
        daoSession.getFolderDao().deleteAll();
        daoSession.getCurrentRssItemViewDao().deleteAll();
    }

    public DatabaseConnectionOrm(Context context) {
        daoSession = DatabaseHelperOrm.getDaoSession(context);
    }

    public void insertNewFolder (Folder folder) {
        daoSession.getFolderDao().insertOrReplace(folder);
    }

    public void insertNewFeed (Feed feed) {
        daoSession.getFeedDao().insertOrReplace(feed);
    }

    public void insertNewItems(RssItem... items) {
        daoSession.getRssItemDao().insertOrReplaceInTx(items);
    }

    public List<Folder> getListOfFolders() {
        return daoSession.getFolderDao().loadAll();
    }

    public List<Folder> getListOfFoldersWithUnreadItems() {
        return daoSession.getFolderDao().queryBuilder().where(
                new WhereCondition.StringCondition(FolderDao.Properties.Id.columnName + " IN "
                        + "(SELECT " + FeedDao.Properties.FolderId.columnName + " FROM " + FeedDao.TABLENAME + " feed "
                        + " JOIN " + RssItemDao.TABLENAME + " rss ON feed." + FeedDao.Properties.Id.columnName + " = rss." + RssItemDao.Properties.FeedId.columnName
                        + " WHERE rss." + RssItemDao.Properties.Read_temp.columnName + " != 1)")
        ).list();
    }

    public List<Feed> getListOfFeeds() {
        return daoSession.getFeedDao().loadAll();
    }

    public List<Feed> getListOfFeedsWithUnreadItems() {
        List<Feed> feedsWithUnreadItems = new ArrayList<Feed>();

        for(Feed feed : getListOfFeeds()) {
            for(RssItem rssItem : feed.getRssItemList()) {
                if (!rssItem.getRead_temp()) {
                    feedsWithUnreadItems.add(feed);
                    break;
                }
            }
        }
        return feedsWithUnreadItems;
    }

    public Folder getFolderById(long folderId) {
        return daoSession.getFolderDao().queryBuilder().where(FolderDao.Properties.Id.eq(folderId)).unique();
    }

    public Feed getFeedById(long feedId) {
        return daoSession.getFeedDao().queryBuilder().where(FeedDao.Properties.Id.eq(feedId)).unique();
    }

    public List<Feed> getListOfFeedsWithFolders() {
        return daoSession.getFeedDao().queryBuilder().where(FeedDao.Properties.FolderId.isNotNull()).list();
    }

    public List<Feed> getListOfFeedsWithoutFolders(boolean onlyWithUnreadRssItems) {
        if(onlyWithUnreadRssItems) {
            return daoSession.getFeedDao().queryBuilder().where(FeedDao.Properties.FolderId.eq(0L),
                    new WhereCondition.StringCondition(FeedDao.Properties.Id.columnName + " IN " + "(SELECT " + RssItemDao.Properties.FeedId.columnName + " FROM " + RssItemDao.TABLENAME + " WHERE " + RssItemDao.Properties.Read_temp.columnName + " != 1)")).list();
        } else {
            return daoSession.getFeedDao().queryBuilder().where(FeedDao.Properties.FolderId.eq(0L)).list();
        }
    }

    public List<Feed> getAllFeedsWithUnreadRssItems() {
        return daoSession.getFeedDao().queryBuilder().where(
                new WhereCondition.StringCondition(FeedDao.Properties.Id.columnName + " IN " + "(SELECT " + RssItemDao.Properties.FeedId.columnName + " FROM " + RssItemDao.TABLENAME + " WHERE " + RssItemDao.Properties.Read_temp.columnName + " != 1)")).list();
    }

    public List<Feed> getAllFeedsWithUnreadRssItemsForFolder(long folderId, boolean onlyUnread) {
        if(onlyUnread) {
            String whereConditionString = " IN " + "(SELECT " + RssItemDao.Properties.FeedId.columnName + " FROM " + RssItemDao.TABLENAME + " WHERE " + RssItemDao.Properties.Read_temp.columnName + " != 1)";
            WhereCondition whereCondition = new WhereCondition.StringCondition(FeedDao.Properties.Id.columnName + whereConditionString);
            return daoSession.getFeedDao().queryBuilder().where(whereCondition, FeedDao.Properties.FolderId.eq(folderId)).list();
        } else {
            return daoSession.getFeedDao().queryBuilder().where(FeedDao.Properties.FolderId.eq(folderId)).list();
        }
    }

    public List<Feed> getAllFeedsWithStarredRssItems() {
        return daoSession.getFeedDao().queryBuilder().where(
                new WhereCondition.StringCondition(FeedDao.Properties.Id.columnName + " IN " + "(SELECT " + RssItemDao.Properties.FeedId.columnName + " FROM " + RssItemDao.TABLENAME + " WHERE " + RssItemDao.Properties.Starred_temp.columnName + " = 1)")).list();
    }

    public List<PodcastFeedItem> getListOfFeedsWithAudioPodcasts() {
        WhereCondition whereCondition = new WhereCondition.StringCondition(FeedDao.Properties.Id.columnName + " IN " + "(SELECT " + RssItemDao.Properties.FeedId.columnName + " FROM " + RssItemDao.TABLENAME + " WHERE " + RssItemDao.Properties.EnclosureMime.columnName + " IN(\"" + join(ALLOWED_PODCASTS_TYPES, "\",\"") + "\"))");
        List<Feed> feedsWithPodcast = daoSession.getFeedDao().queryBuilder().where(whereCondition).list();

        List<PodcastFeedItem> podcastFeedItemsList = new ArrayList<PodcastFeedItem>(feedsWithPodcast.size());
        for(Feed feed : feedsWithPodcast) {
            int podcastCount = 0;
            for(RssItem rssItem : feed.getRssItemList()) {
                if(ALLOWED_PODCASTS_TYPES.contains(rssItem.getEnclosureMime()))
                    podcastCount++;
            }

            podcastFeedItemsList.add(new PodcastFeedItem(feed, podcastCount));
        }
        return podcastFeedItemsList;
    }

    public List<PodcastItem> getListOfAudioPodcastsForFeed(Context context, long feedId) {
        List<PodcastItem> result = new ArrayList<PodcastItem>();

        for(RssItem rssItem : daoSession.getRssItemDao().queryBuilder()
                .where(RssItemDao.Properties.EnclosureMime.in(ALLOWED_PODCASTS_TYPES), RssItemDao.Properties.FeedId.eq(feedId))
                .orderDesc(RssItemDao.Properties.PubDate).list()) {
            PodcastItem podcastItem = ParsePodcastItemFromRssItem(context, rssItem);
            result.add(podcastItem);
        }

        return result;
    }

    public boolean areThereAnyUnsavedChangesInDatabase() {
        long countUnreadRead = daoSession.getRssItemDao().queryBuilder().where(RssItemDao.Properties.Read_temp.notEq(RssItemDao.Properties.Read)).count();
        long countStarredUnstarred = daoSession.getRssItemDao().queryBuilder().where(RssItemDao.Properties.Starred_temp.notEq(RssItemDao.Properties.Starred)).count();

        return (countUnreadRead + countStarredUnstarred) > 0 ? true : false;
    }


    public void updateFeed(Feed feed) {
        daoSession.getFeedDao().update(feed);
    }


    public long getLowestRssItemIdUnread() {
        RssItem rssItem = daoSession.getRssItemDao().queryBuilder().where(RssItemDao.Properties.Read_temp.eq(false)).orderAsc(RssItemDao.Properties.Id).limit(1).unique();
        if(rssItem != null)
            return rssItem.getId();
        else
            return 0;
    }

    public RssItem getLowestRssItemIdByFeed(long idFeed) {
        return daoSession.getRssItemDao().queryBuilder().where(RssItemDao.Properties.FeedId.eq(idFeed)).orderAsc(RssItemDao.Properties.Id).limit(1).unique();
    }

    public RssItem getRssItemById(long rssItemId) {
        return daoSession.getRssItemDao().queryBuilder().where(RssItemDao.Properties.Id.eq(rssItemId)).unique();
    }


    /**
     * Changes the read unread state of the item. This is NOT the temp value!!!
     * @param itemIds
     * @param markAsRead
     */
    public void change_readUnreadStateOfItem(List<String> itemIds, boolean markAsRead)
    {
        if(itemIds != null)
            for(String idItem : itemIds)
                updateIsReadOfRssItem(idItem, markAsRead);
    }

    /**
     * Changes the starred unstarred state of the item. This is NOT the temp value!!!
     * @param itemIds
     * @param markAsStarred
     */
    public void change_starrUnstarrStateOfItem(List<String> itemIds, boolean markAsStarred)
    {
        if(itemIds != null)
            for(String idItem : itemIds)
                updateIsStarredOfRssItem(idItem, markAsStarred);
    }

    public void updateIsReadOfRssItem(String ITEM_ID, Boolean isRead) {
        RssItem rssItem = daoSession.getRssItemDao().queryBuilder().where(RssItemDao.Properties.Id.eq(ITEM_ID)).unique();

        rssItem.setRead(isRead);
        rssItem.setRead_temp(isRead);

        daoSession.getRssItemDao().update(rssItem);
    }

    public void updateIsStarredOfRssItem(String ITEM_ID, Boolean isStarred) {
        RssItem rssItem = daoSession.getRssItemDao().queryBuilder().where(RssItemDao.Properties.Id.eq(ITEM_ID)).unique();

        rssItem.setStarred(isStarred);
        rssItem.setStarred_temp(isStarred);

        daoSession.getRssItemDao().update(rssItem);
    }

    public List<String> getRssItemsIdsFromList(List<RssItem> rssItemList) {
        List<String> itemIds = new ArrayList<String>();
        for(RssItem rssItem : rssItemList) {
            itemIds.add(String.valueOf(rssItem.getId()));
        }
        return itemIds;
    }

    public List<RssItem> getAllNewReadRssItems() {
        return daoSession.getRssItemDao().queryBuilder().where(RssItemDao.Properties.Read.eq(false), RssItemDao.Properties.Read_temp.eq(true)).list();
    }

    public List<RssItem> getAllNewUnreadRssItems() {
        return daoSession.getRssItemDao().queryBuilder().where(RssItemDao.Properties.Read.eq(true), RssItemDao.Properties.Read_temp.eq(false)).list();
    }

    public List<RssItem> getAllNewStarredRssItems() {
        return daoSession.getRssItemDao().queryBuilder().where(RssItemDao.Properties.Starred.eq(false), RssItemDao.Properties.Starred_temp.eq(true)).list();
    }

    public List<RssItem> getAllNewUnstarredRssItems() {
        return daoSession.getRssItemDao().queryBuilder().where(RssItemDao.Properties.Starred.eq(true), RssItemDao.Properties.Starred_temp.eq(false)).list();
    }





    public int getCountOfAllItems(boolean execludeStarred) {//TODO needs testing!
        long count;
        if(execludeStarred)
            count = daoSession.getRssItemDao().queryBuilder().where(RssItemDao.Properties.Starred_temp.notEq(true)).count();
        else
            count = daoSession.getRssItemDao().count();
        return (int) count;
    }

    public List<RssItem> getAllItemsWithIdHigher(long id) {//TODO needs testing!
        return daoSession.getRssItemDao().queryBuilder().where(RssItemDao.Properties.Id.ge(id)).list();
    }

    public void updateRssItem(RssItem rssItem) {
        daoSession.getRssItemDao().update(rssItem);
    }

    public boolean doesRssItemAlreadyExsists (long feedId) {
        List<RssItem> feeds = daoSession.getRssItemDao().queryBuilder().where(RssItemDao.Properties.Id.eq(feedId)).list();
        return (feeds.size() <= 0) ? false : true;
    }


    public void removeFeedById(long feedId) {
        daoSession.getFeedDao().deleteByKey(feedId);
    }




    public SparseArray<String> getUrlsToFavIcons() {
        SparseArray<String> favIconUrls = new SparseArray<String>();

        for(Feed feed : getListOfFeeds())
            favIconUrls.put((int) feed.getId(), feed.getFaviconUrl());

        return favIconUrls;
    }


    public LazyList<RssItem> getCurrentRssItemView(DatabaseConnection.SORT_DIRECTION sortDirection) {
        WhereCondition whereCondition = new WhereCondition.StringCondition(RssItemDao.Properties.Id.columnName + " IN " +
                "(SELECT " + CurrentRssItemViewDao.Properties.RssItemId.columnName + " FROM " + CurrentRssItemViewDao.TABLENAME + ")");

        if(sortDirection.equals(DatabaseConnection.SORT_DIRECTION.asc))
            return daoSession.getRssItemDao().queryBuilder().where(whereCondition).orderAsc(RssItemDao.Properties.PubDate).listLazy();
        else
            return daoSession.getRssItemDao().queryBuilder().where(whereCondition).orderDesc(RssItemDao.Properties.PubDate).listLazy();
    }

    /*
    public void markAllItemsAsReadForCurrentView()
    {
        String sql = "UPDATE " + RssItemDao.TABLENAME + " SET " + RssItemDao.Properties.Read_temp.columnName + " = 1 WHERE " + RssItemDao.Properties.Id.columnName +
                " IN (SELECT " + CurrentRssItemViewDao.Properties.RssItemId.columnName + " FROM " + CurrentRssItemViewDao.TABLENAME + ")";
        daoSession.getDatabase().execSQL(sql);
    }
    */

    public static PodcastItem ParsePodcastItemFromRssItem(Context context, RssItem rssItem) {
        PodcastItem podcastItem = new PodcastItem();
        podcastItem.itemId = rssItem.getId();
        podcastItem.title = rssItem.getTitle();
        podcastItem.link = rssItem.getEnclosureLink();
        podcastItem.mimeType = rssItem.getEnclosureMime();
        podcastItem.favIcon = rssItem.getFeed().getFaviconUrl();

        boolean isVideo = Arrays.asList(DatabaseConnectionOrm.VIDEO_FORMATS).contains(podcastItem.mimeType);
        podcastItem.isVideoPodcast = isVideo;

        File file = new File(PodcastDownloadService.getUrlToPodcastFile(context, podcastItem.link, false));
        podcastItem.offlineCached = file.exists();

        return podcastItem;
    }


    public String getAllItemsIdsForFeedSQL(long idFeed, boolean onlyUnread, boolean onlyStarredItems, DatabaseConnection.SORT_DIRECTION sortDirection) {

        String buildSQL =  "SELECT " + RssItemDao.Properties.Id.columnName +
                " FROM " + RssItemDao.TABLENAME +
                " WHERE " + RssItemDao.Properties.FeedId.columnName + " IN " +
                "(SELECT " + FeedDao.Properties.Id.columnName +
                " FROM " + FeedDao.TABLENAME +
                " WHERE " + FeedDao.Properties.Id.columnName  + " = " + idFeed + ")";

        if(onlyUnread && !onlyStarredItems)
            buildSQL += " AND " + RssItemDao.Properties.Read_temp.columnName + " != 1";
        else if(onlyStarredItems)
            buildSQL += " AND " + RssItemDao.Properties.Starred_temp.columnName + " = 1";

        buildSQL += " ORDER BY " + RssItemDao.Properties.PubDate.columnName + " " + sortDirection.toString();

        return buildSQL;
    }


    public Long getLowestItemIdByFolder(Long id_folder) {
        WhereCondition whereCondition = new WhereCondition.StringCondition(RssItemDao.Properties.FeedId.columnName + " IN " +
                        "(SELECT " + FeedDao.Properties.Id.columnName +
                        " FROM " + FeedDao.TABLENAME +
                        " WHERE " + FeedDao.Properties.FolderId.columnName + " = " + id_folder + ")");

        RssItem rssItem = daoSession.getRssItemDao().queryBuilder().orderAsc(RssItemDao.Properties.Id).where(whereCondition).limit(1).unique();
        return (rssItem != null) ? rssItem.getId() : 0;
    }

    public List<RssItem> getListOfAllItemsForFolder(long ID_FOLDER, boolean onlyUnread, DatabaseConnection.SORT_DIRECTION sortDirection, int limit) {
        String whereStatement = getAllItemsIdsForFolderSQL(ID_FOLDER, onlyUnread, sortDirection);
        whereStatement = whereStatement.replace("SELECT " + RssItemDao.Properties.Id.columnName + " FROM " + RssItemDao.TABLENAME, "");
        whereStatement += " LIMIT " + limit;
        return daoSession.getRssItemDao().queryRaw(whereStatement, null);
    }

    public String getAllItemsIdsForFolderSQL(long ID_FOLDER, boolean onlyUnread, DatabaseConnection.SORT_DIRECTION sortDirection) {
        //If all starred items are requested always return them in desc. order
        if(ID_FOLDER == ALL_STARRED_ITEMS.getValue())
            sortDirection = DatabaseConnection.SORT_DIRECTION.desc;

        String buildSQL = "SELECT " + RssItemDao.Properties.Id.columnName +
                " FROM " + RssItemDao.TABLENAME;

        if(!(ID_FOLDER == ALL_UNREAD_ITEMS.getValue() || ID_FOLDER == ALL_STARRED_ITEMS.getValue()) || ID_FOLDER == ALL_ITEMS.getValue())//Wenn nicht Alle Artikel ausgewaehlt wurde (-10) oder (-11) fuer Starred Feeds
        {
            buildSQL += " WHERE " + RssItemDao.Properties.FeedId.columnName + " IN " +
                    "(SELECT sc." + FeedDao.Properties.Id.columnName +
                    " FROM " + FeedDao.TABLENAME + " sc " +
                    " JOIN " + FolderDao.TABLENAME + " f ON sc." + FeedDao.Properties.FolderId.columnName + " = f." + FolderDao.Properties.Id.columnName +
                    " WHERE f." + FolderDao.Properties.Id.columnName + " = " + ID_FOLDER + ")";

            if(onlyUnread)
                buildSQL += " AND " + RssItemDao.Properties.Read_temp.columnName + " != 1";
        }
        else if(ID_FOLDER == ALL_UNREAD_ITEMS.getValue())
            buildSQL += " WHERE " + RssItemDao.Properties.Read_temp.columnName + " != 1";
        else if(ID_FOLDER == ALL_STARRED_ITEMS.getValue())
            buildSQL += " WHERE " + RssItemDao.Properties.Starred_temp.columnName + " = 1";


        buildSQL += " ORDER BY " + RssItemDao.Properties.PubDate.columnName + " " + sortDirection.toString();


        return buildSQL;
    }

    public void insertIntoRssCurrentViewTable(String SQL_SELECT) {
        SQL_SELECT = "INSERT INTO " + CurrentRssItemViewDao.TABLENAME +
                " (" + CurrentRssItemViewDao.Properties.RssItemId.columnName + ") " + SQL_SELECT;
        daoSession.getCurrentRssItemViewDao().deleteAll();
        daoSession.getDatabase().execSQL(SQL_SELECT);
    }

    public SparseArray<String> getUnreadItemCountForFolder() {
        String buildSQL = "SELECT f." + FolderDao.Properties.Id.columnName + ", COUNT(rss." + RssItemDao.Properties.Id.columnName + ")" +
                " FROM " + RssItemDao.TABLENAME + " rss " +
                " JOIN " + FeedDao.TABLENAME + " feed ON rss." + RssItemDao.Properties.FeedId.columnName + " = feed." + FeedDao.Properties.Id.columnName +
                " JOIN " + FolderDao.TABLENAME + " f ON feed." + FeedDao.Properties.FolderId.columnName + " = f." + FolderDao.Properties.Id.columnName +
                " WHERE " + RssItemDao.Properties.Read_temp.columnName + " != 1 " +
                " GROUP BY f." + FolderDao.Properties.Id.columnName;

        SparseArray<String> values = getStringSparseArrayFromSQL(buildSQL, 0, 1);

        values.put(SPECIAL_FOLDERS.ALL_UNREAD_ITEMS.getValue(), getUnreadItemsCountForSpecificFolder(SPECIAL_FOLDERS.ALL_UNREAD_ITEMS));
        values.put(SPECIAL_FOLDERS.ALL_STARRED_ITEMS.getValue(), getUnreadItemsCountForSpecificFolder(SPECIAL_FOLDERS.ALL_STARRED_ITEMS));


        return values;
    }

    public String getUnreadItemsCountForSpecificFolder(SPECIAL_FOLDERS specialFolder) {
        String buildSQL = "SELECT COUNT(rss." + RssItemDao.Properties.Id.columnName + ")" +
                " FROM " + RssItemDao.TABLENAME + " rss ";

        if(specialFolder != null && specialFolder.equals(SPECIAL_FOLDERS.ALL_STARRED_ITEMS)) {
            buildSQL += " WHERE " + RssItemDao.Properties.Starred_temp.columnName + " = 1 ";
        } else {
            buildSQL += " WHERE " + RssItemDao.Properties.Read_temp.columnName + " != 1 ";
        }

        SparseArray<String> values = getStringSparseArrayFromSQL(buildSQL, 0, 0);
        return values.valueAt(0);
    }

    public SparseArray<String> getUnreadItemCountForFeed() {
        String buildSQL = "SELECT " + RssItemDao.Properties.FeedId.columnName + ", COUNT(" + RssItemDao.Properties.Id.columnName + ")" + // rowid as _id,
                " FROM " + RssItemDao.TABLENAME +
                " WHERE " + RssItemDao.Properties.Read_temp.columnName + " != 1 " +
                " GROUP BY " + RssItemDao.Properties.FeedId.columnName;

        return getStringSparseArrayFromSQL(buildSQL, 0, 1);
    }

    public SparseArray<String> getStarredItemCountForFeed() {
        String buildSQL = "SELECT " + RssItemDao.Properties.FeedId.columnName + ", COUNT(" + RssItemDao.Properties.Id.columnName + ")" + // rowid as _id,
                " FROM " + RssItemDao.TABLENAME +
                " WHERE " + RssItemDao.Properties.Starred_temp.columnName + " = 1 " +
                " GROUP BY " + RssItemDao.Properties.FeedId.columnName;

        return getStringSparseArrayFromSQL(buildSQL, 0, 1);
    }

    public void clearDatabaseOverSize()
    {
        //If i have 9023 rows in the database, when i run that query it should delete 8023 rows and leave me with 1000
        //database.execSQL("DELETE FROM " + RSS_ITEM_TABLE + " WHERE " +  + "ORDER BY rowid DESC LIMIT 1000 *

        //Let's say it said 1005 - you need to delete 5 rows.
        //DELETE FROM table ORDER BY dateRegistered ASC LIMIT 5


        int max = Constants.maxItemsCount;
        int total = (int) getLongValueBySQL("SELECT COUNT(*) FROM " + RssItemDao.TABLENAME);
        int unread = (int) getLongValueBySQL("SELECT COUNT(*) FROM " + RssItemDao.TABLENAME + " WHERE " + RssItemDao.Properties.Read_temp.columnName + " != 1");
        int read = total - unread;

        if(total > max)
        {
            int overSize = total - max;
            //Soll verhindern, dass ungelesene Artikel gelöscht werden
            if(overSize > read)
                overSize = read;

            String sqlStatement = "DELETE FROM " + RssItemDao.TABLENAME + " WHERE " + RssItemDao.Properties.Id.columnName +
                                    " IN (SELECT " + RssItemDao.Properties.Id.columnName + " FROM " + RssItemDao.TABLENAME +
                                    " WHERE " + RssItemDao.Properties.Read_temp.columnName + " = 1 AND " + RssItemDao.Properties.Starred_temp.columnName + " != 1 " +
                                    " ORDER BY " + RssItemDao.Properties.Id.columnName + " asc LIMIT " + overSize + ")";
            daoSession.getDatabase().execSQL(sqlStatement);
    		/* SELECT * FROM rss_item WHERE read_temp = 1 ORDER BY rowid asc LIMIT 3; */
        }
    }

    public long getLastModified()
    {
        List<RssItem> rssItemList = daoSession.getRssItemDao().queryBuilder().orderDesc(RssItemDao.Properties.LastModified).limit(1).list();

        if(rssItemList.size() > 0)
            return rssItemList.get(0).getLastModified().getTime();
        return 0;
    }

    public long getLowestItemId(boolean onlyStarred)
    {
        List<RssItem> rssItemList;

        if(onlyStarred)
            rssItemList = daoSession.getRssItemDao().queryBuilder().orderDesc(RssItemDao.Properties.Starred_temp).orderAsc(RssItemDao.Properties.Id).limit(1).list();
        else
            rssItemList = daoSession.getRssItemDao().queryBuilder().orderAsc(RssItemDao.Properties.Id).limit(1).list();

        if(rssItemList.size() > 0)
            return rssItemList.get(0).getId();
        return 0;
    }

    public long getHighestItemId()
    {
        List<RssItem> rssItemList = daoSession.getRssItemDao().queryBuilder().orderDesc(RssItemDao.Properties.Id).limit(1).list();

        if(rssItemList.size() > 0)
            return rssItemList.get(0).getId();
        return 0;
    }








    public long getLongValueBySQL(String buildSQL)
    {
        long result = -1;

        Cursor cursor = daoSession.getDatabase().rawQuery(buildSQL, null);
        try
        {
            if(cursor != null)
            {
                if(cursor.moveToFirst())
                    result = cursor.getLong(0);
            }
        } finally {
            cursor.close();
        }

        return result;
    }

    public SparseArray<Integer> getIntegerSparseArrayFromSQL(String buildSQL, int indexKey, int indexValue) {
        SparseArray<Integer> result = new SparseArray<Integer>();

        Cursor cursor = daoSession.getDatabase().rawQuery(buildSQL, null);
        try
        {
            if(cursor != null)
            {
                if(cursor.getCount() > 0)
                {
                    cursor.moveToFirst();
                    do {
                        int key = cursor.getInt(indexKey);
                        Integer value = cursor.getInt(indexValue);
                        result.put(key, value);
                    } while(cursor.moveToNext());
                }
            }
        } finally {
            cursor.close();
        }

        return result;
    }

    public SparseArray<String> getStringSparseArrayFromSQL(String buildSQL, int indexKey, int indexValue) {
        SparseArray<String> result = new SparseArray<String>();

        Cursor cursor = daoSession.getDatabase().rawQuery(buildSQL, null);
        try
        {
            if(cursor != null)
            {
                if(cursor.getCount() > 0)
                {
                    cursor.moveToFirst();
                    do {
                        int key = cursor.getInt(indexKey);
                        String value = cursor.getString(indexValue);
                        result.put(key, value);
                    } while(cursor.moveToNext());
                }
            }
        } finally {
            cursor.close();
        }

        return result;
    }



    public static String join(Collection<?> col, String delim) {
        StringBuilder sb = new StringBuilder();
        Iterator<?> iter = col.iterator();
        if (iter.hasNext())
            sb.append(iter.next().toString());
        while (iter.hasNext()) {
            sb.append(delim);
            sb.append(iter.next().toString());
        }
        return sb.toString();
    }
}
