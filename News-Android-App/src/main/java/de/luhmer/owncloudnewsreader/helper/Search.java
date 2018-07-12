package de.luhmer.owncloudnewsreader.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.List;

import de.luhmer.owncloudnewsreader.ListView.SubscriptionExpandableListAdapter;
import de.luhmer.owncloudnewsreader.NewsDetailActivity;
import de.luhmer.owncloudnewsreader.SettingsActivity;
import de.luhmer.owncloudnewsreader.database.DatabaseConnectionOrm;
import de.luhmer.owncloudnewsreader.database.model.RssItem;

public class Search {

    private static final String SEARCH_IN_TITLE = "0";
    private static final String SEARCH_IN_BODY = "1";

    public static List<RssItem> PerformSearch(Context context, Long idFolder, Long idFeed, String searchString) {
        DatabaseConnectionOrm.SORT_DIRECTION sortDirection = NewsDetailActivity.getSortDirectionFromSettings(context);
        DatabaseConnectionOrm dbConn = new DatabaseConnectionOrm(context);
        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(context);


        boolean onlyUnreadItems = mPrefs.getBoolean(SettingsActivity.CB_SHOWONLYUNREAD_STRING, false);
        boolean onlyStarredItems = false;

        String sqlSelectStatement = null;
        if (idFeed != null) {
            sqlSelectStatement = getFeedSQLStatement(idFeed, onlyUnreadItems, onlyStarredItems, sortDirection, searchString, dbConn, mPrefs);

        } else if (idFolder != null) {
            if (idFolder == SubscriptionExpandableListAdapter.SPECIAL_FOLDERS.ALL_STARRED_ITEMS.getValue()) {
                onlyUnreadItems = false;
            }
            sqlSelectStatement = getFolderSQLStatement(idFolder, onlyUnreadItems, sortDirection, searchString, dbConn, mPrefs);
        }

        List<RssItem> items = null;
        if (!sqlSelectStatement.equals("")) {
            dbConn.insertIntoRssCurrentViewTable(sqlSelectStatement);
            items = dbConn.getCurrentRssItemView(0);
        }
        return items;

    }



    private static String getFeedSQLStatement(final long idFeed,
                                       final boolean onlyUnreadItems,
                                       final boolean onlyStarredItems,
                                       final DatabaseConnectionOrm.SORT_DIRECTION sortDirection,
                                       final String searchString,
                                       final DatabaseConnectionOrm dbConn,
                                       final SharedPreferences mPrefs) {
        String sql = "";
        String searchIn = mPrefs.getString(SettingsActivity.SP_SEARCH_IN,"0");
        if(searchIn.equals(SEARCH_IN_TITLE)) {
            sql = dbConn.getAllItemsIdsForFeedSQLFilteredByTitle(idFeed, onlyUnreadItems, onlyStarredItems, sortDirection, searchString);
        } else if(searchIn.equals(SEARCH_IN_BODY)) {
            sql = dbConn.getAllItemsIdsForFeedSQLFilteredByBodySQL(idFeed, onlyUnreadItems, onlyStarredItems, sortDirection, searchString);
        }
        return sql;
    }

    private static String getFolderSQLStatement(final long ID_FOLDER,
                                         final boolean onlyUnreadItems,
                                         final DatabaseConnectionOrm.SORT_DIRECTION sortDirection,
                                         final String searchString,
                                         final DatabaseConnectionOrm dbConn,
                                         final SharedPreferences mPrefs) {
        String sql = "";
        String searchIn = mPrefs.getString(SettingsActivity.SP_SEARCH_IN,"0");
        if(searchIn.equals(SEARCH_IN_TITLE)) {
            sql = dbConn.getAllItemsIdsForFolderSQLFilteredByTitle(ID_FOLDER, onlyUnreadItems, sortDirection, searchString);
        } else if(searchIn.equals(SEARCH_IN_BODY)) {
            sql = dbConn.getAllItemsIdsForFolderSQLFilteredByBody(ID_FOLDER, onlyUnreadItems, sortDirection, searchString);
        }

        return sql;
    }
}
