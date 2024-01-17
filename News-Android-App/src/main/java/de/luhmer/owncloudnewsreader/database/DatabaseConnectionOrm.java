package de.luhmer.owncloudnewsreader.database;

import static de.luhmer.owncloudnewsreader.ListView.SubscriptionExpandableListAdapter.SPECIAL_FOLDERS;
import static de.luhmer.owncloudnewsreader.ListView.SubscriptionExpandableListAdapter.SPECIAL_FOLDERS.ALL_DOWNLOADED_PODCASTS;
import static de.luhmer.owncloudnewsreader.ListView.SubscriptionExpandableListAdapter.SPECIAL_FOLDERS.ALL_ITEMS;
import static de.luhmer.owncloudnewsreader.ListView.SubscriptionExpandableListAdapter.SPECIAL_FOLDERS.ALL_STARRED_ITEMS;
import static de.luhmer.owncloudnewsreader.ListView.SubscriptionExpandableListAdapter.SPECIAL_FOLDERS.ALL_UNREAD_ITEMS;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;
import android.util.SparseArray;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;

import de.greenrobot.dao.query.LazyList;
import de.greenrobot.dao.query.QueryBuilder;
import de.greenrobot.dao.query.WhereCondition;
import de.luhmer.owncloudnewsreader.Constants;
import de.luhmer.owncloudnewsreader.NewsReaderApplication;
import de.luhmer.owncloudnewsreader.database.model.CurrentRssItemViewDao;
import de.luhmer.owncloudnewsreader.database.model.DaoSession;
import de.luhmer.owncloudnewsreader.database.model.Feed;
import de.luhmer.owncloudnewsreader.database.model.FeedDao;
import de.luhmer.owncloudnewsreader.database.model.Folder;
import de.luhmer.owncloudnewsreader.database.model.FolderDao;
import de.luhmer.owncloudnewsreader.database.model.RssItem;
import de.luhmer.owncloudnewsreader.database.model.RssItemDao;
import de.luhmer.owncloudnewsreader.helper.AsyncTaskHelper;
import de.luhmer.owncloudnewsreader.helper.NewsFileUtils;
import de.luhmer.owncloudnewsreader.helper.StopWatch;
import de.luhmer.owncloudnewsreader.model.PodcastFeedItem;
import de.luhmer.owncloudnewsreader.model.PodcastItem;
import de.luhmer.owncloudnewsreader.services.PodcastDownloadService;

public class DatabaseConnectionOrm {

    public static final List<String> ALLOWED_PODCASTS_TYPES = new ArrayList<String>() {
        {
            this.add("audio/mp3");
            this.add("audio/mp4");
            this.add("audio/mpeg");
            this.add("audio/ogg");
            this.add("audio/opus");
            this.add("audio/ogg;codecs=opus");
            this.add("audio/x-m4a");
            this.add("youtube");
            this.add("video/mp4");
        }
    };

    private final String TAG = getClass().getCanonicalName();
    //private static final String[] VIDEO_FORMATS = { "youtube", "video/mp4" };
    private static final String[] VIDEO_FORMATS = { "video/mp4" };
    public enum SORT_DIRECTION { asc, desc }

    private final DaoSession daoSession;

    private final static int PageSize = 25;

    private final Context context;

    protected @Inject @Named("databaseFileName") String databasePath;

    public void resetDatabase() {
        daoSession.getRssItemDao().deleteAll();
        daoSession.getFeedDao().deleteAll();
        daoSession.getFolderDao().deleteAll();
        daoSession.getCurrentRssItemViewDao().deleteAll();
    }

    public DatabaseConnectionOrm(Context context) {
        this.context = context;
        if(databasePath == null) {
            ((NewsReaderApplication) context.getApplicationContext()).getAppComponent().injectDatabaseConnection(this);
        }
        daoSession = DatabaseHelperOrm.getDaoSession(context, databasePath);
    }

    public void deleteOldAndInsertNewFolders (final Folder... folder) {
        daoSession.runInTx(() -> {
            daoSession.getFolderDao().deleteAll();
            daoSession.getFolderDao().insertInTx(folder);
        });
    }

    public void deleteOldAndInsertNewFolders (final Iterable<Folder> folder) {
        daoSession.runInTx(() -> {
            daoSession.getFolderDao().deleteAll();
            daoSession.getFolderDao().insertInTx(folder);
        });
    }

    public void insertNewFolders(final Iterable<Folder> folder) {
        daoSession.getFolderDao().insertInTx(folder);
    }

    public void insertNewFeed (Iterable<Feed> feeds) {
        daoSession.getFeedDao().insertOrReplaceInTx(feeds);
    }

    public void insertNewItems(Iterable<RssItem> items) {
        daoSession.getRssItemDao().insertOrReplaceInTx(items);
    }

    public List<Folder> getListOfFolders() {
        // return daoSession.getFolderDao().loadAll();
        return daoSession.getFolderDao().queryBuilder().orderAsc(FolderDao.Properties.Label).list();
    }

/*
    public List<Folder> getListOfFoldersWithUnreadItems() {
        return daoSession.getFolderDao().queryBuilder().where(
                new WhereCondition.PropertyCondition(FolderDao.Properties.Id, " IN "
                        + "(SELECT " + FeedDao.Properties.FolderId.columnName + " FROM " + FeedDao.TABLENAME + " feed "
                        + " JOIN " + RssItemDao.TABLENAME + " rss ON feed." + FeedDao.Properties.Id.columnName + " = rss." + RssItemDao.Properties.FeedId.columnName
                        + " WHERE rss." + RssItemDao.Properties.Read_temp.columnName + " != 1)")
        ).list();
    }
*/

    public List<Feed> getListOfFeeds() {
        return daoSession.getFeedDao().queryBuilder().orderAsc(FeedDao.Properties.FeedTitle).list();
    }

    public List<Feed> getListOfFeedsWithUnreadItems() {
        List<Feed> feedsWithUnreadItems = new ArrayList<>();

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

    public Folder getFolderByLabel(String label) {
        return daoSession.getFolderDao().queryBuilder().where(FolderDao.Properties.Label.eq(label)).unique();
    }

    public Feed getFeedById(long feedId) {
        return daoSession.getFeedDao().queryBuilder().where(FeedDao.Properties.Id.eq(feedId)).unique();
    }

    public List<Feed> getListOfFeedsWithFolders() {
        return daoSession.getFeedDao().queryBuilder().orderAsc(FeedDao.Properties.FeedTitle).where(FeedDao.Properties.FolderId.isNotNull()).list();
    }

    public List<Feed> getListOfFeedsWithoutFolders(boolean onlyWithUnreadRssItems) {
        if(onlyWithUnreadRssItems) {
            return daoSession.getFeedDao().queryBuilder().orderAsc(FeedDao.Properties.FeedTitle).where(FeedDao.Properties.FolderId.eq(0L),
                    new WhereCondition.StringCondition(FeedDao.Properties.Id.columnName + " IN " + "(SELECT " + RssItemDao.Properties.FeedId.columnName + " FROM " + RssItemDao.TABLENAME + " WHERE " + RssItemDao.Properties.Read_temp.columnName + " != 1)")).list();
        } else {
            return daoSession.getFeedDao().queryBuilder().orderAsc(FeedDao.Properties.FeedTitle).where(FeedDao.Properties.FolderId.eq(0L)).list();
        }
    }

    public List<Feed> getAllFeedsWithUnreadRssItems() {
        return daoSession.getFeedDao().queryRaw(", " + RssItemDao.TABLENAME + " R " +
                " WHERE R." + RssItemDao.Properties.FeedId.columnName + " = T._id " +
                " AND " + RssItemDao.Properties.Read_temp.columnName + " != 1 GROUP BY T._id");
    }

    public List<Feed> getAllFeedsWithUnreadRssItemsForFolder(long folderId) {
        return daoSession.getFeedDao().queryBuilder().orderAsc(FeedDao.Properties.FeedTitle).where(FeedDao.Properties.FolderId.eq(folderId)).list();
    }

    public List<Feed> getAllFeedsWithStarredRssItems() {
        return daoSession.getFeedDao().queryBuilder().orderAsc(FeedDao.Properties.FeedTitle).where(
                new WhereCondition.StringCondition(FeedDao.Properties.Id.columnName + " IN " + "(SELECT " + RssItemDao.Properties.FeedId.columnName + " FROM " + RssItemDao.TABLENAME + " WHERE " + RssItemDao.Properties.Starred_temp.columnName + " = 1)")).list();
    }

    public List<Feed> getAllFeedsWithDownloadedPodcasts(Context context) {
        var ids = NewsFileUtils.getDownloadedPodcastsFingerprints(context);
        var files = Arrays.stream(ids).map((f) -> "\"" + f + "\"").collect(Collectors.toList());
        return daoSession.getFeedDao().queryBuilder().orderAsc(FeedDao.Properties.FeedTitle).where(
                new WhereCondition.StringCondition(FeedDao.Properties.Id.columnName + " IN " + "(SELECT " + RssItemDao.Properties.FeedId.columnName + " FROM " + RssItemDao.TABLENAME + " WHERE " + RssItemDao.Properties.Fingerprint.columnName + " in (" + String.join(",", files) + "))")).list();
    }

    public List<PodcastFeedItem> getListOfFeedsWithAudioPodcasts() {
        WhereCondition whereCondition = new WhereCondition.StringCondition(FeedDao.Properties.Id.columnName + " IN " + "(SELECT " + RssItemDao.Properties.FeedId.columnName + " FROM " + RssItemDao.TABLENAME + " WHERE " + RssItemDao.Properties.EnclosureMime.columnName + " IN(\"" + join(ALLOWED_PODCASTS_TYPES, "\",\"") + "\"))");
        List<Feed> feedsWithPodcast = daoSession.getFeedDao().queryBuilder().orderAsc(FeedDao.Properties.FeedTitle).where(whereCondition).list();

        List<PodcastFeedItem> podcastFeedItemsList = new ArrayList<>(feedsWithPodcast.size());
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
        List<PodcastItem> result = new ArrayList<>();

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

        return (countUnreadRead + countStarredUnstarred) > 0;
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
    public void changeStarrUnstarrStateOfItem(List<String> itemIds, boolean markAsStarred)
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

    public void markAllItemsAsReadForCurrentView() {
        /*
        String sql = "UPDATE " + RssItemDao.TABLENAME + " SET " + RssItemDao.Properties.Read_temp.columnName + " = 1 " +
                "WHERE " + RssItemDao.Properties.Id.columnName + " IN (SELECT " + CurrentRssItemViewDao.Properties.RssItemId.columnName + " FROM " + CurrentRssItemViewDao.TABLENAME + ")";
        daoSession.getDatabase().execSQL(sql);
        */

        // 100 causes android.database.sqlite.SQLiteBlobTooBigException on some devices
        final int itemsPerIteration = 25;

        WhereCondition whereCondition = new WhereCondition.StringCondition(RssItemDao.Properties.Id.columnName + " IN " +
                "(SELECT " + CurrentRssItemViewDao.Properties.RssItemId.columnName + " FROM " + CurrentRssItemViewDao.TABLENAME + ")");

        int iterationCount = 0;
        List<RssItem> rssItemList;
        do {
            int offset = iterationCount * itemsPerIteration;
            rssItemList = daoSession
                    .getRssItemDao()
                    .queryBuilder()
                    .where(whereCondition)
                    .limit(itemsPerIteration)
                    .offset(offset)
                    .listLazy();
            for (RssItem rssItem : rssItemList) {
                rssItem.setRead_temp(true);
            }
            daoSession.getRssItemDao().updateInTx(rssItemList);

            iterationCount++;
        } while(rssItemList.size() == itemsPerIteration);
    }


    public List<String> getRssItemsIdsFromList(List<RssItem> rssItemList) {
        List<String> itemIds = new ArrayList<>();
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

    public LazyList<RssItem> getAllUnreadRssItemsForWidget() {
        return daoSession.getRssItemDao().queryBuilder().where(RssItemDao.Properties.Read_temp.eq(false)).limit(100).orderDesc(RssItemDao.Properties.PubDate).listLazy();
    }

    public Set<String> getNotificationGroups() {
        List<Feed> feeds = daoSession.getFeedDao().loadAll();
        String[] notificationChannelsGroups = feeds
                .stream()
                .map(Feed::getNotificationChannel)
                .filter(nc -> !nc.equals("none"))
                .toArray(String[]::new);
        return new HashSet<>(Arrays.asList(notificationChannelsGroups));
    }

    public QueryBuilder<RssItem> getAllUnreadRssItemsForNotificationGroup(SORT_DIRECTION sortDirection, String notificationGroup) {
        QueryBuilder<RssItem> qb = daoSession.getRssItemDao().queryBuilder()
                .where(RssItemDao.Properties.Read_temp.eq(false));

        // filter for notification group
        qb.join(RssItemDao.Properties.FeedId, Feed.class, FeedDao.Properties.Id)
                .where(FeedDao.Properties.NotificationChannel.eq(notificationGroup));

        if (sortDirection == SORT_DIRECTION.asc) {
            qb = qb.orderAsc(RssItemDao.Properties.PubDate);
        } else {
            qb = qb.orderDesc(RssItemDao.Properties.PubDate);
        }

        return qb;
    }

    public void markAllItemsAsRead() {
        StopWatch sw = new StopWatch();
        sw.start();

        String sql = "UPDATE " + RssItemDao.TABLENAME + " SET " + RssItemDao.Properties.Read_temp.columnName + " = 1 WHERE " + RssItemDao.Properties.Read_temp.columnName + " = 0";
        daoSession.getDatabase().execSQL(sql);

        sw.stop();
        Log.v(TAG, "Time needed for marking all unread items as read: " + sw);
    }

    public LazyList<RssItem> getAllUnreadRssItemsForDownloadWebPageService() {
        return daoSession.getRssItemDao().queryBuilder().where(RssItemDao.Properties.Read_temp.eq(false)).orderDesc(RssItemDao.Properties.PubDate).listLazy();
    }

    public LazyList<RssItem> getAllItemsWithIdHigher(long id) {
        return daoSession.getRssItemDao().queryBuilder().where(RssItemDao.Properties.Id.ge(id)).listLazy();
    }

    /***
     * Warning: This methods performs database operations asynchronously. Therefore this method
     * will return immediately - even though the operation might not be completed
     */
    public void updateRssItem(RssItem rssItem) {
        AsyncTaskHelper.StartAsyncTask(new UpdateRssItemAsyncTask(rssItem));
    }


    class UpdateRssItemAsyncTask extends AsyncTask<Void, Void, Void> {

        private final RssItem rssItem;

        UpdateRssItemAsyncTask(RssItem rssItem) {
            this.rssItem = rssItem;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            daoSession.getRssItemDao().update(rssItem);

            // Code below is used to deduplicate rss items (see https://github.com/nextcloud/news-android/issues/513)
            if(rssItem.getRead_temp()) {
                // Get all rss items with the same fingerprint (This operation is very slow)
                List<RssItem> rssItemList = daoSession.getRssItemDao().queryBuilder().where(
                        RssItemDao.Properties.Fingerprint.eq(rssItem.getFingerprint()),
                        RssItemDao.Properties.Id.notEq(rssItem.getId()))
                        .list();

                // Sync the read-state of the items
                for (RssItem rssItem1 : rssItemList) {
                    rssItem1.setRead_temp(rssItem.getRead_temp());
                }

                // Update in database
                daoSession.getRssItemDao().updateInTx(rssItemList);
            }

            return null;
        }
    }

    public void removeFeedById(final long feedId) {
        daoSession.runInTx(() -> {
            daoSession.getFeedDao().deleteByKey(feedId);

            List<RssItem> list = daoSession.getRssItemDao().queryBuilder().where(RssItemDao.Properties.FeedId.eq(feedId)).list();
            for (RssItem rssItem : list) {
                daoSession.getRssItemDao().delete(rssItem);
            }
        });
    }

    public void renameFeedById(long feedId, String newTitle) {
        Feed feed = daoSession.getFeedDao().queryBuilder().where(FeedDao.Properties.Id.eq(feedId)).unique();
        feed.setFeedTitle(newTitle);
        daoSession.getFeedDao().update(feed);
    }

    public SparseArray<String> getUrlsToFavIcons() {
        SparseArray<String> favIconUrls = new SparseArray<>();

        for(Feed feed : getListOfFeeds())
            favIconUrls.put((int) feed.getId(), feed.getFaviconUrl());

        return favIconUrls;
    }

    public long getCurrentRssItemViewCount() {
        return daoSession.getCurrentRssItemViewDao().count();

    }

    public List<RssItem> getCurrentRssItemView(int page) {
        String where_clause = ", " + CurrentRssItemViewDao.TABLENAME + " C "
                + " WHERE C." + CurrentRssItemViewDao.Properties.RssItemId.columnName + " = T."
                + RssItemDao.Properties.Id.columnName
                + " AND C._id > " + page * PageSize + " AND c._id <= " + ((page+1) * PageSize)
                + " ORDER BY C." + CurrentRssItemViewDao.Properties.Id.columnName;

        return daoSession.getRssItemDao().queryRaw(where_clause);
    }

    public LazyList<RssItem> getAllRssItems() {
        String where_clause = ", " + CurrentRssItemViewDao.TABLENAME + " C "
                + " WHERE C." + CurrentRssItemViewDao.Properties.RssItemId.columnName + " = T."
                + RssItemDao.Properties.Id.columnName
                + " ORDER BY C." + CurrentRssItemViewDao.Properties.Id.columnName;

        return daoSession.getRssItemDao().queryRawCreate(where_clause).listLazy();
    }

    /**
     * Removes only the folder, without removing feeds inside the folder
     */
    public void removeFolderById(final long folderId) {
        daoSession.getFolderDao().deleteByKey(folderId);
    }

    public void renameFolderById(long folderId, String newLabel) {
        Folder folder = daoSession.getFolderDao().queryBuilder().where(FolderDao.Properties.Id.eq(folderId)).unique();
        folder.setLabel(newLabel);
        daoSession.getFolderDao().update(folder);
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
        Feed feed = rssItem.getFeed();
        podcastItem.author = feed.getFeedTitle();// rssItem.getAuthor();
        podcastItem.itemId = rssItem.getId();
        podcastItem.title = rssItem.getTitle();
        podcastItem.link = rssItem.getEnclosureLink();
        podcastItem.mimeType = rssItem.getEnclosureMime();
        podcastItem.favIcon = feed.getFaviconUrl();
        podcastItem.fingerprint = rssItem.getFingerprint();

        if("image/jpeg".equals(podcastItem.mimeType)) {
            // We don't want to accidentally think that enclosed images are podcasts
            podcastItem.link = "";
            podcastItem.mimeType = "";
        }

        podcastItem.isVideoPodcast = Arrays.asList(DatabaseConnectionOrm.VIDEO_FORMATS).contains(podcastItem.mimeType);

        File file = new File(PodcastDownloadService.getUrlToPodcastFile(context, podcastItem.fingerprint, podcastItem.link, false));
        podcastItem.offlineCached = file.exists();

        return podcastItem;
    }


    public String getAllItemsIdsForFeedSQL(long idFeed, boolean onlyUnread, boolean onlyStarredItems, SORT_DIRECTION sortDirection) {

        String buildSQL =  "SELECT " + RssItemDao.Properties.Id.columnName +
                " FROM " + RssItemDao.TABLENAME +
                " WHERE " + RssItemDao.Properties.FeedId.columnName + " = " + idFeed;

        if(onlyUnread && !onlyStarredItems)
            buildSQL += " AND " + RssItemDao.Properties.Read_temp.columnName + " != 1";
        else if(onlyStarredItems)
            buildSQL += " AND " + RssItemDao.Properties.Starred_temp.columnName + " = 1";

        buildSQL += " ORDER BY " + RssItemDao.Properties.PubDate.columnName + " " + sortDirection.toString();

        return buildSQL;
    }

    public String getAllItemsIdsForFeedSQLFilteredByTitle(final long feedId, boolean onlyUnread, boolean onlyStarredItems, SORT_DIRECTION sortDirection, final String searchString) {
        String buildSQL = getAllItemsIdsForFeedSQL(feedId, onlyUnread, onlyStarredItems, sortDirection);

        return new StringBuilder(
                buildSQL).insert(buildSQL.indexOf("ORDER"), " AND " + getSearchSQLForColumn(RssItemDao.Properties.Title.columnName, searchString)).toString();
    }

    public String getAllItemsIdsForFeedSQLFilteredByBodySQL(final long feedId, boolean onlyUnread, boolean onlyStarredItems, SORT_DIRECTION sortDirection, final String searchString) {
        String buildSQL = getAllItemsIdsForFeedSQL(feedId, onlyUnread, onlyStarredItems, sortDirection);

        return new StringBuilder(
                buildSQL).insert(buildSQL.indexOf("ORDER"), " AND " + getSearchSQLForColumn(RssItemDao.Properties.Body.columnName, searchString)).toString();
    }

    public String getAllItemsIdsForFeedSQLFilteredByTitleAndBodySQL(final long feedId, boolean onlyUnread, boolean onlyStarredItems, SORT_DIRECTION sortDirection, final String searchString) {
        String buildSQL = getAllItemsIdsForFeedSQL(feedId, onlyUnread, onlyStarredItems, sortDirection);
        String titleQuery = getSearchSQLForColumn(RssItemDao.Properties.Title.columnName, searchString);
        String bodyQuery = getSearchSQLForColumn(RssItemDao.Properties.Body.columnName, searchString);

        return new StringBuilder(
                buildSQL).insert(buildSQL.indexOf("ORDER"), " AND (" + titleQuery + " OR " + bodyQuery + ")" ).toString();
    }

    private String getSearchSQLForColumn(String column, String searchString) {
        return column + " LIKE \"%" + searchString + "%\"";
    }

    public Long getLowestItemIdByFolder(Long id_folder) {
        WhereCondition whereCondition = new WhereCondition.StringCondition(RssItemDao.Properties.FeedId.columnName + " IN " +
                        "(SELECT " + FeedDao.Properties.Id.columnName +
                        " FROM " + FeedDao.TABLENAME +
                        " WHERE " + FeedDao.Properties.FolderId.columnName + " = " + id_folder + ")");

        RssItem rssItem = daoSession.getRssItemDao().queryBuilder().orderAsc(RssItemDao.Properties.Id).where(whereCondition).limit(1).unique();
        return (rssItem != null) ? rssItem.getId() : 0;
    }


    public String getAllItemsIdsForFolderSQL(long ID_FOLDER, boolean onlyUnread, SORT_DIRECTION sortDirection, Context context) {
        String buildSQL = "SELECT " + RssItemDao.Properties.Id.columnName +
                " FROM " + RssItemDao.TABLENAME;

        if(!(ID_FOLDER == ALL_UNREAD_ITEMS.getValue() || ID_FOLDER == ALL_STARRED_ITEMS.getValue() || ID_FOLDER == ALL_DOWNLOADED_PODCASTS.getValue()) || ID_FOLDER == ALL_ITEMS.getValue())//Wenn nicht Alle Artikel ausgewaehlt wurde (-10) oder (-11) fuer Starred Feeds
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
        else if (ID_FOLDER == ALL_DOWNLOADED_PODCASTS.getValue()) {
            var ids = NewsFileUtils.getDownloadedPodcastsFingerprints(context);
            var files = Arrays.stream(ids).map((f) -> "\"" + f + "\"").collect(Collectors.toList());
            buildSQL += " WHERE " + RssItemDao.Properties.Fingerprint.columnName + " in (" + String.join(",", files) + ")";
        }

        buildSQL += " ORDER BY " + RssItemDao.Properties.PubDate.columnName + " " + sortDirection.toString();

        return buildSQL;
    }

    public String getAllItemsIdsForFolderSQLSearch(long ID_FOLDER, SORT_DIRECTION sortDirection, List<String> columns, String searchString) {
        String buildSQL = "SELECT " + RssItemDao.Properties.Id.columnName +
                " FROM " + RssItemDao.TABLENAME;

        if (!(ID_FOLDER == ALL_UNREAD_ITEMS.getValue() || ID_FOLDER == ALL_STARRED_ITEMS.getValue()) || ID_FOLDER == ALL_ITEMS.getValue())//Wenn nicht Alle Artikel ausgewaehlt wurde (-10) oder (-11) fuer Starred Feeds
        {
            buildSQL += " WHERE " + RssItemDao.Properties.FeedId.columnName + " IN " +
                    "(SELECT sc." + FeedDao.Properties.Id.columnName +
                    " FROM " + FeedDao.TABLENAME + " sc " +
                    " JOIN " + FolderDao.TABLENAME + " f ON sc." + FeedDao.Properties.FolderId.columnName + " = f." + FolderDao.Properties.Id.columnName +
                    " WHERE f." + FolderDao.Properties.Id.columnName + " = " + ID_FOLDER + ") AND ";
        } else {
            buildSQL += " WHERE ";
        }

        columns = columns.stream().map(c -> c + " LIKE \"%" + searchString + "%\"").collect(Collectors.toList());
        buildSQL += String.join(" OR ", columns);

        buildSQL += " ORDER BY " + RssItemDao.Properties.PubDate.columnName + " " + sortDirection.toString();

        return buildSQL;
    }

    public void insertIntoRssCurrentViewTable(String SQL_SELECT) {
        StopWatch sw = new StopWatch();
        sw.start();

        SQL_SELECT = "INSERT INTO " + CurrentRssItemViewDao.TABLENAME +
                " (" + CurrentRssItemViewDao.Properties.RssItemId.columnName + ") " + SQL_SELECT;

        final String SQL_INSERT_STATEMENT = SQL_SELECT;

        daoSession.runInTx(new Runnable() {
            @Override
            public void run() {
                daoSession.getCurrentRssItemViewDao().deleteAll();
                daoSession.getDatabase().execSQL(SQL_INSERT_STATEMENT);
            }
        });

        sw.stop();
        Log.v(TAG, "Time needed for insert: " + sw);
    }

    public String getUnreadItemsCountForSpecificFolder(SPECIAL_FOLDERS specialFolder) {
        String buildSQL = "SELECT COUNT(1)" +
                " FROM " + RssItemDao.TABLENAME + " rss ";

        if(specialFolder != null && specialFolder.equals(SPECIAL_FOLDERS.ALL_STARRED_ITEMS)) {
            buildSQL += " WHERE " + RssItemDao.Properties.Starred_temp.columnName + " = 1 ";
        } else {
            buildSQL += " WHERE " + RssItemDao.Properties.Read_temp.columnName + " != 1 ";
        }

        SparseArray<String> values = getStringSparseArrayFromSQL(buildSQL, 0, 0);
        return values.valueAt(0);
    }

    /**
     *
     * @return [0] = unread items count for folders, [1] = unread items count for feeds
     */
    public SparseArray<String>[] getUnreadItemCountFeedFolder() {
        SparseArray<String>[] values = new SparseArray[2];

        String buildSQL = "SELECT f." + FolderDao.Properties.Id.columnName + ", feed." + FeedDao.Properties.Id.columnName + ", COUNT(1)" +
                " FROM " + RssItemDao.TABLENAME + " rss " +
                " JOIN " + FeedDao.TABLENAME + " feed ON rss." + RssItemDao.Properties.FeedId.columnName + " = feed." + FeedDao.Properties.Id.columnName +
                " LEFT OUTER JOIN " + FolderDao.TABLENAME + " f ON feed." + FeedDao.Properties.FolderId.columnName + " = f." + FolderDao.Properties.Id.columnName +
                " WHERE " + RssItemDao.Properties.Read_temp.columnName + " != 1 " +
                " GROUP BY f." + FolderDao.Properties.Id.columnName + ", feed." + FeedDao.Properties.Id.columnName;
                //" GROUP BY (case when f." + FolderDao.Properties.Id.columnName + " IS NULL then feed." + FeedDao.Properties.Id.columnName + " ELSE f." + FolderDao.Properties.Id.columnName + " end)";

        values[0] = new SparseArray<>();
        values[1] = new SparseArray<>();

        int totalUnreadItemsCount = 0;

        try (Cursor cursor = daoSession.getDatabase().rawQuery(buildSQL, null)) {
            if (cursor != null) {
                if (cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    do {
                        int folderId = cursor.getInt(0);
                        int feedId = cursor.getInt(1);
                        int unreadCount = cursor.getInt(2);

                        totalUnreadItemsCount += unreadCount;

                        values[1].put(feedId, String.valueOf(unreadCount));
                        if (folderId != 0) {
                            if (values[0].get(folderId) != null) {
                                unreadCount += Integer.parseInt(values[0].get(folderId));
                            }

                            values[0].put(folderId, String.valueOf(unreadCount));
                        }
                    } while (cursor.moveToNext());
                }
            }
        }


        values[0].put(SPECIAL_FOLDERS.ALL_UNREAD_ITEMS.getValue(), String.valueOf(totalUnreadItemsCount));
        values[0].put(SPECIAL_FOLDERS.ALL_STARRED_ITEMS.getValue(), getUnreadItemsCountForSpecificFolder(SPECIAL_FOLDERS.ALL_STARRED_ITEMS));


        return values;

    }

    public SparseArray<String> getStarredItemCount() {
        String buildSQL = "SELECT " + RssItemDao.Properties.FeedId.columnName + ", COUNT(1)" + // rowid as _id,
                " FROM " + RssItemDao.TABLENAME +
                " WHERE " + RssItemDao.Properties.Starred_temp.columnName + " = 1 " +
                " GROUP BY " + RssItemDao.Properties.FeedId.columnName;

        return getStringSparseArrayFromSQL(buildSQL, 0, 1);
    }

    public int getDownloadedPodcastsCount(Context context) {
        var ids = NewsFileUtils.getDownloadedPodcastsFingerprints(context);
        var files = Arrays.stream(ids).map((f) -> "\"" + f + "\"").collect(Collectors.toList());

        String buildSQL = "SELECT COUNT(1)" +
                " FROM " + RssItemDao.TABLENAME +
                " WHERE " + RssItemDao.Properties.Fingerprint.columnName + " in (" + String.join(",", files) + ")";

        return (int) getLongValueBySQL(buildSQL);
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
            Log.v(TAG, "Clearing Database oversize");

            int overSize = total - max;
            //Soll verhindern, dass ungelesene Artikel gelöscht werden
            if(overSize > read)
                overSize = read;

            var downloadedPodcastsFingerprints = NewsFileUtils.getDownloadedPodcastsFingerprints(context);
            var files = Arrays.stream(downloadedPodcastsFingerprints).map((f) -> "\"" + f + "\"").collect(Collectors.toList());

            String sqlStatement = "DELETE FROM " + RssItemDao.TABLENAME + " WHERE " + RssItemDao.Properties.Id.columnName +
                                    " IN (SELECT " + RssItemDao.Properties.Id.columnName + " FROM " + RssItemDao.TABLENAME +
                                    " WHERE " + RssItemDao.Properties.Read_temp.columnName + " = 1 AND " + RssItemDao.Properties.Starred_temp.columnName + " != 1 " +
                                    " AND " + RssItemDao.Properties.Fingerprint.columnName + " NOT IN (" + String.join(",", files) + ")" + // This means that the article has downloaded podcast media
                                    " AND " + RssItemDao.Properties.Id.columnName + " NOT IN (SELECT " + CurrentRssItemViewDao.Properties.RssItemId.columnName + " FROM " + CurrentRssItemViewDao.TABLENAME + ")" +
                                    " ORDER BY " + RssItemDao.Properties.Id.columnName + " asc LIMIT " + overSize + ")";
            daoSession.getDatabase().execSQL(sqlStatement);
    		/* SELECT * FROM rss_item WHERE read_temp = 1 ORDER BY rowid asc LIMIT 3; */
        } else {
            Log.v(TAG, "Clearing Database oversize not necessary");
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

        try (Cursor cursor = daoSession.getDatabase().rawQuery(buildSQL, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                result = cursor.getLong(0);
            }
        }

        return result;
    }

    public SparseArray<Integer> getIntegerSparseArrayFromSQL(String buildSQL, int indexKey, int indexValue) {
        SparseArray<Integer> result = new SparseArray<>();

        try (Cursor cursor = daoSession.getDatabase().rawQuery(buildSQL, null)) {
            if (cursor != null) {
                if (cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    do {
                        int key = cursor.getInt(indexKey);
                        Integer value = cursor.getInt(indexValue);
                        result.put(key, value);
                    } while (cursor.moveToNext());
                }
            }
        }

        return result;
    }

    public SparseArray<String> getStringSparseArrayFromSQL(String buildSQL, int indexKey, int indexValue) {
        SparseArray<String> result = new SparseArray<>();

        try (Cursor cursor = daoSession.getDatabase().rawQuery(buildSQL, null)) {
            if (cursor != null) {
                if (cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    do {
                        int key = cursor.getInt(indexKey);
                        String value = cursor.getString(indexValue);
                        result.put(key, value);
                    } while (cursor.moveToNext());
                }
            }
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

    public void clearSessionCache() {
        daoSession.clear();
    }
}
