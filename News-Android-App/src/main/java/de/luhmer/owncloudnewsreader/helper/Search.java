package de.luhmer.owncloudnewsreader.helper;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.List;

import de.luhmer.owncloudnewsreader.SettingsActivity;
import de.luhmer.owncloudnewsreader.database.DatabaseConnectionOrm;
import de.luhmer.owncloudnewsreader.database.model.RssItem;
import de.luhmer.owncloudnewsreader.database.model.RssItemDao;

public class Search {

    private static final String SEARCH_IN_TITLE = "0";
    private static final String SEARCH_IN_BODY = "1";
    
    public static List<RssItem> PerformSearch(Context context, Long idFolder, Long idFeed, String searchString, SharedPreferences mPrefs) {
        DatabaseConnectionOrm.SORT_DIRECTION sortDirection = DatabaseUtilsKt.getSortDirectionFromSettings(mPrefs);
        DatabaseConnectionOrm dbConn = new DatabaseConnectionOrm(context);
        String sqlSelectStatement = null;
        if (idFeed != null) {
            sqlSelectStatement = getFeedSQLStatement(idFeed, sortDirection, searchString, dbConn, mPrefs);
        } else if (idFolder != null) {
            sqlSelectStatement = getFolderSQLStatement(idFolder, sortDirection, searchString, dbConn, mPrefs);
        }

        List<RssItem> items = new ArrayList<>();
        if (sqlSelectStatement != null) {
            dbConn.insertIntoRssCurrentViewTable(sqlSelectStatement);
            items = dbConn.getCurrentRssItemView(0);
        }
        return items;

    }



    private static String getFeedSQLStatement(final long idFeed,
                                       final DatabaseConnectionOrm.SORT_DIRECTION sortDirection,
                                       final String searchString,
                                       final DatabaseConnectionOrm dbConn,
                                       final SharedPreferences mPrefs) {
        String sql = "";
        String searchIn = mPrefs.getString(SettingsActivity.SP_SEARCH_IN,"0");
        if(searchIn.equals(SEARCH_IN_TITLE)) {
            sql = dbConn.getAllItemsIdsForFeedSQLFilteredByTitle(idFeed, false, false, sortDirection, searchString);
        } else if(searchIn.equals(SEARCH_IN_BODY)) {
            sql = dbConn.getAllItemsIdsForFeedSQLFilteredByBodySQL(idFeed, false, false, sortDirection, searchString);
        }
        return sql;
    }

    private static String getFolderSQLStatement(final long ID_FOLDER,
                                         final DatabaseConnectionOrm.SORT_DIRECTION sortDirection,
                                         final String searchString,
                                         final DatabaseConnectionOrm dbConn,
                                         final SharedPreferences mPrefs) {
        String sql = "";
        String searchIn = mPrefs.getString(SettingsActivity.SP_SEARCH_IN,"0");
        if(searchIn.equals(SEARCH_IN_TITLE)) {
            sql = dbConn.getAllItemsIdsForFolderSQLSearch(ID_FOLDER, sortDirection, RssItemDao.Properties.Title.columnName, searchString);
        } else if(searchIn.equals(SEARCH_IN_BODY)) {
            sql = dbConn.getAllItemsIdsForFolderSQLSearch(ID_FOLDER, sortDirection, RssItemDao.Properties.Body.columnName, searchString);
        }

        return sql;
    }
}
