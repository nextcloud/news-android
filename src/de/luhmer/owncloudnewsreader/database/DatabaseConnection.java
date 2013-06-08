package de.luhmer.owncloudnewsreader.database;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.luhmer.owncloudnewsreader.Constants;
import de.luhmer.owncloudnewsreader.ListView.SubscriptionExpandableListAdapter;
import de.luhmer.owncloudnewsreader.ListView.Subscription_ListViewAdapter;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class DatabaseConnection {	
	private DatabaseHelper openHelper;
    private SQLiteDatabase database;
    
    public static final String FOLDER_TABLE = "folder";
    public static final String FOLDER_LABEL = "label";
    public static final String FOLDER_LABEL_ID = "label_id";
    
    public static final String SUBSCRIPTION_TABLE = "subscription";
    public static final String SUBSCRIPTION_HEADERTEXT = "header_text";    
    //public static final String SUBSCRIPTION_SUBSCRIPTION_ID = "subscription_id_subscription";
    public static final String SUBSCRIPTION_FOLDER_ID = "folder_idfolder";
    public static final String SUBSCRIPTION_ID = "subscription_id";
    public static final String SUBSCRIPTION_FAVICON_URL = "favicon_url";
    public static final String SUBSCRIPTION_LINK = "link";
    
    public static final String RSS_ITEM_TABLE = "rss_item";
    public static final String RSS_ITEM_SUBSCRIPTION_ID = "subscription_id_subscription";    
    public static final String RSS_ITEM_TITLE = "title";
    public static final String RSS_ITEM_LINK = "link";
    public static final String RSS_ITEM_BODY = "body";
    public static final String RSS_ITEM_READ = "read";
    public static final String RSS_ITEM_RSSITEM_ID = "rssitem_id";
    public static final String RSS_ITEM_STARRED = "starred";
    public static final String RSS_ITEM_PUBDATE = "pubdate";
    public static final String RSS_ITEM_AUTHOR = "author";
    public static final String RSS_ITEM_GUID = "guid";
    public static final String RSS_ITEM_GUIDHASH = "guidHash";
    
    public static final String RSS_ITEM_READ_TEMP = "read_temp";
    public static final String RSS_ITEM_STARRED_TEMP = "starred_temp";
	
	
    public static final boolean DATABASE_DEBUG_MODE = false; //(false && Constants.DEBUG_MODE) ? true: false;
    

    public DatabaseConnection(Context aContext) {         
        openHelper = new DatabaseHelper(aContext);
        openDatabase();   
    }

    /*
	public Cursor getAllData(String TABLE_NAME) {		 
        String buildSQL = "SELECT rowid as _id, * FROM " + TABLE_NAME; 
        Log.d("DB_HELPER", "getAllData SQL: " + buildSQL); 
        return database.rawQuery(buildSQL, null);
    }
	*/

	public Cursor getAllTopSubscriptions(boolean onlyUnread) {
		//String buildSQL = "SELECT rowid as _id, * FROM " + SUBSCRIPTION_TABLE + " WHERE subscription_id_subscription IS NULL"; 
		String buildSQL = "SELECT f.rowid as _id, * FROM " + FOLDER_TABLE + " f ";
        if(onlyUnread)
        {
            buildSQL += " JOIN " + SUBSCRIPTION_TABLE + " sc ON f.rowid = sc." + SUBSCRIPTION_FOLDER_ID +
                        " JOIN " + RSS_ITEM_TABLE + " rss ON sc.rowid = rss." + RSS_ITEM_SUBSCRIPTION_ID +
                        " WHERE " + RSS_ITEM_READ_TEMP + " != 1" + 
                        " GROUP BY f.rowid " +
                        " HAVING COUNT(*) > 0";
        }

        if(DATABASE_DEBUG_MODE)
        	Log.d("DB_HELPER", "getAllTopSubscriptions SQL: " + buildSQL);
        return database.rawQuery(buildSQL, null);
	}
	
	public Cursor getAllTopSubscriptionsWithoutFolder(boolean onlyUnread) {
		//String buildSQL = "SELECT rowid as _id, * FROM " + SUBSCRIPTION_TABLE + " WHERE subscription_id_subscription IS NULL"; 
		String buildSQL = "SELECT sc.rowid as _id, * FROM " + SUBSCRIPTION_TABLE + " sc ";
		
		if(onlyUnread)
		{
			buildSQL += " JOIN " + RSS_ITEM_TABLE + " rss ON sc.rowid = rss." + RSS_ITEM_SUBSCRIPTION_ID +
						" WHERE " + SUBSCRIPTION_FOLDER_ID + " IS NULL" +
						" AND " + RSS_ITEM_READ_TEMP + " != 1" +
                        " GROUP BY sc.rowid " +
                        " HAVING COUNT(*) > 0";
        }
		else
			buildSQL += " WHERE " + SUBSCRIPTION_FOLDER_ID + " IS NULL";

        if(DATABASE_DEBUG_MODE)
        	Log.d("DB_HELPER", "getAllTopSubscriptions SQL: " + buildSQL);
        return database.rawQuery(buildSQL, null);
	}

    /*
	public Cursor getAllTopSubscriptionsWithUnreadFeeds() {
		//String buildSQL = "SELECT rowid as _id, * FROM " + SUBSCRIPTION_TABLE + " WHERE subscription_id_subscription IS NULL"; 
		String buildSQL = "SELECT f.rowid as _id, * FROM " + FOLDER_TABLE + " f "+
							" JOIN " + SUBSCRIPTION_TABLE + " sc ON f.rowid = sc." + SUBSCRIPTION_FOLDER_ID +
							" JOIN " + RSS_ITEM_TABLE + " rss ON sc.rowid = rss." + RSS_ITEM_SUBSCRIPTION_ID +
							" GROUP BY f.rowid " +
							" HAVING COUNT(*) > 0";
        Log.d("DB_HELPER", "getAllTopData SQL: " + buildSQL); 
        return database.rawQuery(buildSQL, null);
	}*/


	public Cursor getAllSubSubscriptions() {
		//String buildSQL = "SELECT rowid as _id, * FROM " + SUBSCRIPTION_TABLE + " WHERE subscription_id_subscription IS NOT NULL"; 
		String buildSQL = "SELECT rowid as _id, * FROM " + SUBSCRIPTION_TABLE;

		if(DATABASE_DEBUG_MODE)
        	Log.d("DB_HELPER", "getAllSubSubscriptions SQL: " + buildSQL); 
        return database.rawQuery(buildSQL, null);
	}
	
	public Cursor getSubSubscriptionsByID(String ID_FEED_DB) {//Feeds		 
		String buildSQL = "SELECT rowid as _id, * FROM " + SUBSCRIPTION_TABLE + " WHERE rowid = '" + ID_FEED_DB + "'";

		if(DATABASE_DEBUG_MODE)
        	Log.d("DB_HELPER", "getSubSubscriptionsByID SQL: " + buildSQL); 
        return database.rawQuery(buildSQL, null);
	}
	
	public String getCountItemsForSubscription(String ID_SUBSCRIPTION, boolean onlyUnread, boolean execludeStarredItems) {
		
		String buildSQL = "SELECT COUNT(*) " + 
	 			" FROM " + RSS_ITEM_TABLE +  
				//" WHERE read != 1" +
				" WHERE " + RSS_ITEM_READ_TEMP +" != 1 AND " + RSS_ITEM_STARRED + " != 1" +
					" AND subscription_id_subscription IN " + 
					"(SELECT rowid " + 
					"FROM subscription " +					
					"WHERE rowid = " + ID_SUBSCRIPTION + ");";
		
		
		
		if(!onlyUnread)
			buildSQL = buildSQL.replace("read_temp != 1 AND", "");
		
		if(!execludeStarredItems)
			buildSQL = buildSQL.replace(RSS_ITEM_STARRED + " != 1 AND", RSS_ITEM_STARRED_TEMP + " = 1 AND");
		
		String result = "0";
		Cursor cursor = database.rawQuery(buildSQL, null);
        if (cursor != null)
        {
        	if(cursor.moveToFirst())
        		result = cursor.getString(0);
        }
        cursor.close();
        
        return result;
    }
	
	public Boolean isFeedUnreadStarred(String FEED_ID, Boolean checkUnread) {
		String buildSQL;
		/*
		if(checkUnread)
			buildSQL = "SELECT read "; 
		else//Wenn nicht checkRead auf true steht, soll geprueft werden ob das Feed Markiert ist.
			buildSQL = "SELECT starred ";
			*/

		if(checkUnread)
			buildSQL = "SELECT read_temp "; 
		else//Wenn nicht checkRead auf true steht, soll geprueft werden ob das Feed Markiert ist.
			buildSQL = "SELECT starred_temp ";

		buildSQL += " FROM " + RSS_ITEM_TABLE +  
					" WHERE rowid = " + FEED_ID;
		
		return checkSqlForBoolean(buildSQL);
    }
	
	private Boolean checkSqlForBoolean(String buildSQL)
	{
		Boolean result = false;
		Cursor cursor = database.rawQuery(buildSQL, null);
        if (cursor != null)
        {
        	if(cursor.moveToFirst())
        	{
        		String val = cursor.getString(0);
        		if(val != null)
        			if(val.equals("1"))
        				result = true;
        	}
        }
        cursor.close();

        return result;
	}

	public void updateIsReadOfFeed(String FEED_ID, Boolean isRead) {
		ContentValues args = new ContentValues();
		//args.put(RSS_ITEM_READ, isRead);
		args.put(RSS_ITEM_READ_TEMP, isRead);
		int result = database.update(RSS_ITEM_TABLE, args, "rowid=?", new String[] { FEED_ID });
		
		if(DATABASE_DEBUG_MODE)
			Log.d("RESULT UPDATE DATABASE", "RESULT: " + result);
    }
	
	public void updateIsStarredOfFeed(String FEED_ID, Boolean isStarred) {
		
		if(isStarred)//Wenn ein Feed markiert ist muss es auch als gelesen markiert werden.
			updateIsReadOfFeed(FEED_ID, true);
		
		
		ContentValues args = new ContentValues();
		//args.put(RSS_ITEM_STARRED, isStarred);
		args.put(RSS_ITEM_STARRED_TEMP, isStarred);
		int result = database.update(RSS_ITEM_TABLE, args, "rowid=?", new String[] { FEED_ID });
		
		if(DATABASE_DEBUG_MODE)
			Log.d("RESULT UPDATE DATABASE", "RESULT: " + result);
    }
	
	private String getAllFeedsSelectStatement()
	{
		return "SELECT rowid as _id, " + RSS_ITEM_TITLE + ", " + RSS_ITEM_RSSITEM_ID + ", " + RSS_ITEM_LINK + ", " + RSS_ITEM_BODY + ", " + RSS_ITEM_READ + ", " + RSS_ITEM_SUBSCRIPTION_ID + ", "
					+ RSS_ITEM_PUBDATE + ", " + RSS_ITEM_STARRED + ", " + RSS_ITEM_GUIDHASH + ", " + RSS_ITEM_GUID + ", " + RSS_ITEM_STARRED_TEMP + ", " + RSS_ITEM_READ_TEMP;
	}
	
	public Cursor getAllItemsForFeed(String ID_SUBSCRIPTION, boolean onlyUnread, boolean onlyStarredItems) {
		
		String buildSQL =  getAllFeedsSelectStatement() +
	 			" FROM " + RSS_ITEM_TABLE +  
				" WHERE subscription_id_subscription IN " + 
					"(SELECT rowid " + 
					"FROM subscription " +					
					"WHERE rowid = " + ID_SUBSCRIPTION + ")";
		
		if(onlyUnread && !onlyStarredItems)
			buildSQL += " AND " + RSS_ITEM_READ_TEMP + " != 1";
		else if(onlyStarredItems)
			buildSQL += " AND " + RSS_ITEM_STARRED_TEMP + " = 1";
		
		if(DATABASE_DEBUG_MODE)
			Log.d("DB_HELPER", "getAllItemsForFeed SQL: " + buildSQL); 
        return database.rawQuery(buildSQL, null);
    }
	
	public Cursor getFeedByID(String ID_FEED) {				
		String buildSQL = getAllFeedsSelectStatement() + 
	 			" FROM " + RSS_ITEM_TABLE +  
				" WHERE rowid = " + ID_FEED;
		
		if(DATABASE_DEBUG_MODE)
			Log.d("DB_HELPER", "getFeedByID SQL: " + buildSQL); 
        return database.rawQuery(buildSQL, null);
    }

    public String getRowIdOfFeedByItemID(String ID_FEED)
    {
        String buildSQL = "SELECT rowid " +
                " FROM " + RSS_ITEM_TABLE +
                " WHERE " + RSS_ITEM_RSSITEM_ID + " = " + ID_FEED;

        String result = null;
        Cursor cursor = database.rawQuery(buildSQL, null);
        if (cursor != null)
        {
            if(cursor.moveToFirst())
                result = cursor.getString(0);
        }
        cursor.close();

        return result;
    }
	
    /*
	public String getCountUnreadFeedsForFolder(String ID_FOLDER, boolean onlyUnread) {		//TODO optimize this here !!!!
		String buildSQL = "SELECT COUNT(*) " +  
	 			" FROM " + RSS_ITEM_TABLE + 
	 			" WHERE " + RSS_ITEM_READ_TEMP + " != 1 ";
		if(!ID_FOLDER.equals(SubscriptionExpandableListAdapter.ALL_UNREAD_ITEMS))
				buildSQL += " AND subscription_id_subscription IN " + 
					"(SELECT sc.rowid " + 
					"FROM subscription sc " +
					"JOIN folder f ON sc." + SUBSCRIPTION_FOLDER_ID + " = f.rowid " +
					"WHERE f.rowid = " + ID_FOLDER + ")";
		
		if(onlyUnread)
			buildSQL += " AND ";
		String result = "0";		
		Cursor cursor = database.rawQuery(buildSQL, null);
        if (cursor != null)
        {
        	if(cursor.moveToFirst())
        		result = cursor.getString(0);
        }
        cursor.close();
        
        return result;
    }*/
	
	public int getCountFeedsForFolder(String ID_FOLDER, boolean onlyUnread) {
		
		Cursor cursor = getAllItemsForFolder(ID_FOLDER, onlyUnread);
		int count = cursor.getCount();
		cursor.close();
		
		return count;
		
		
	}
	/*
	public String getCountFeedsForFolder(String ID_FOLDER, boolean onlyUnread) {		
		String buildSQL = "SELECT COUNT(*) " +  
	 			" FROM " + RSS_ITEM_TABLE;
		
		if(!(ID_FOLDER.equals(SubscriptionExpandableListAdapter.ALL_UNREAD_ITEMS) || ID_FOLDER.equals(SubscriptionExpandableListAdapter.ALL_STARRED_ITEMS)))  
		{
				buildSQL += " WHERE subscription_id_subscription IN " + 
					"(SELECT sc.rowid " + 
					"FROM subscription sc " +
					"JOIN folder f ON sc." + SUBSCRIPTION_FOLDER_ID + " = f.rowid " +
					"WHERE f.rowid = " + ID_FOLDER + ") ";
				
				if(onlyUnread)
					buildSQL += " AND read_temp != 1";
		}
		else if(ID_FOLDER.equals(SubscriptionExpandableListAdapter.ALL_UNREAD_ITEMS))//UNREAD
			buildSQL += " WHERE starred != 1 and read_temp != 1";
		else if(ID_FOLDER.equals(SubscriptionExpandableListAdapter.ALL_STARRED_ITEMS))//STARRED
			buildSQL += " WHERE starred_temp = 1";
		//	buildSQL += " WHERE starred = 1";
		
			
		
		String result = "0";		
		Cursor cursor = database.rawQuery(buildSQL, null);
        if (cursor != null)
        {
        	if(cursor.moveToFirst())
        		result = cursor.getString(0);
        }
        cursor.close();
        
        return result;
    }*/
	
	public Cursor getAllItemsForFolder(String ID_FOLDER, boolean onlyUnread) {
		String buildSQL = getAllFeedsSelectStatement() + 
	 			" FROM " + RSS_ITEM_TABLE;
	 	
	 	if(!(ID_FOLDER.equals(SubscriptionExpandableListAdapter.ALL_UNREAD_ITEMS) || ID_FOLDER.equals(SubscriptionExpandableListAdapter.ALL_STARRED_ITEMS)))//Wenn nicht Alle Artikel ausgewaehlt wurde (-10) oder (-11) fuer Starred Feeds
	 	{
			buildSQL += " WHERE subscription_id_subscription IN " + 
					"(SELECT sc.rowid " + 
					"FROM subscription sc " +
					"JOIN folder f ON sc." + SUBSCRIPTION_FOLDER_ID + " = f.rowid " +
					"WHERE f.rowid = " + ID_FOLDER + ")";
			
			if(onlyUnread)
				buildSQL += " AND " + RSS_ITEM_READ_TEMP + " != 1";
	 	}
	 	else if(ID_FOLDER.equals(SubscriptionExpandableListAdapter.ALL_UNREAD_ITEMS) && onlyUnread)//only unRead should only be null when testing the size of items
	 		buildSQL += " WHERE " + RSS_ITEM_STARRED_TEMP + " != 1 AND " + RSS_ITEM_READ_TEMP + " != 1";
	 	else if(ID_FOLDER.equals(SubscriptionExpandableListAdapter.ALL_UNREAD_ITEMS))
	 		buildSQL += " WHERE " + RSS_ITEM_STARRED + " != 1";
	 	else if(ID_FOLDER.equals(SubscriptionExpandableListAdapter.ALL_STARRED_ITEMS) && onlyUnread)
	 		buildSQL += " WHERE " + RSS_ITEM_STARRED_TEMP + " = 1 AND " + RSS_ITEM_READ_TEMP + " != 1";
	 	else if(ID_FOLDER.equals(SubscriptionExpandableListAdapter.ALL_STARRED_ITEMS))
	 		buildSQL += " WHERE " + RSS_ITEM_STARRED_TEMP + " = 1";
	 			
	 	
	 	buildSQL += " ORDER BY " + RSS_ITEM_PUBDATE + " desc";

	 	//	buildSQL += " WHERE starred = 1";
	 	
	 	if(DATABASE_DEBUG_MODE)
	 		Log.d("DB_HELPER", "getAllFeedData SQL: " + buildSQL); 
        return database.rawQuery(buildSQL, null);
    }	
	
	public Cursor getAllSubscriptionForFolder(String ID_FOLDER, boolean onlyUnread) {
        //String buildSQL = "SELECT rowid as _id, * FROM " + SUBSCRIPTION_TABLE + " WHERE " + SUBSCRIPTION_SUBSCRIPTION_ID + "=" + ID_SUBSCRIPTION; 
		String buildSQL = "SELECT sc.rowid as _id, sc.* " + 
							"FROM " + SUBSCRIPTION_TABLE + " sc " +
							"LEFT OUTER JOIN folder f ON sc.folder_idfolder = f.rowid ";
		
		if(ID_FOLDER.equals("-11"))//Starred
        {
        	buildSQL += " JOIN " + RSS_ITEM_TABLE + " rss ON sc.rowid = rss." + RSS_ITEM_SUBSCRIPTION_ID +
                    " WHERE rss." + RSS_ITEM_STARRED_TEMP + " = 1" +
                    " GROUP BY sc.rowid " +
                    " HAVING COUNT(*) > 0";
        }
		else if(onlyUnread || ID_FOLDER.equals(SubscriptionExpandableListAdapter.ALL_UNREAD_ITEMS) /*ID_SUBSCRIPTION.matches("-10|-11")*/)
        {
            buildSQL += " JOIN " + RSS_ITEM_TABLE + " rss ON sc.rowid = rss." + RSS_ITEM_SUBSCRIPTION_ID +
                        " WHERE f.rowid = " + ID_FOLDER  + " AND rss." + RSS_ITEM_READ_TEMP + " != 1" +
                        " GROUP BY sc.rowid " +
                        " HAVING COUNT(*) > 0";
            
            if(ID_FOLDER.equals(SubscriptionExpandableListAdapter.ALL_UNREAD_ITEMS))
            	buildSQL = buildSQL.replace("f.rowid = " + ID_FOLDER + " AND", "");//Remove to ID stuff because i want the result of all feeds where are unread items in
        } 
        else
            buildSQL += "WHERE f.rowid = " + ID_FOLDER;
        
		if(DATABASE_DEBUG_MODE)
			Log.d("DB_HELPER", "getAllSub_SubscriptionForSubscription SQL: " + buildSQL);
        return database.rawQuery(buildSQL, null);
    }	
	
	public void insertNewFolder (String label, String label_path) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(FOLDER_LABEL, label);  
        contentValues.put(FOLDER_LABEL_ID, label_path);  
        database.insert(FOLDER_TABLE, null, contentValues);     
    }
	
	public void insertNewSub_Subscription (String headerText, String ID_FOLDER, String subscription_id, String FAVICON_URL) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(SUBSCRIPTION_HEADERTEXT, headerText);
        contentValues.put(SUBSCRIPTION_FOLDER_ID, ID_FOLDER);
        contentValues.put(SUBSCRIPTION_ID , subscription_id);
        contentValues.put(SUBSCRIPTION_FAVICON_URL, FAVICON_URL);
        database.insert(SUBSCRIPTION_TABLE, null, contentValues);        
    }
	
	public void insertNewFeed (String Titel, String link, String description, Boolean isRead, String ID_SUBSCRIPTION, String ID_RSSITEM, Date timestamp, Boolean isStarred, String guid, String guidHash) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(RSS_ITEM_TITLE, Titel);
        contentValues.put(RSS_ITEM_LINK, link);
        contentValues.put(RSS_ITEM_BODY, description);
        contentValues.put(RSS_ITEM_READ, isRead);
        contentValues.put(RSS_ITEM_SUBSCRIPTION_ID, ID_SUBSCRIPTION);
        contentValues.put(RSS_ITEM_RSSITEM_ID, ID_RSSITEM);
        contentValues.put(RSS_ITEM_PUBDATE, timestamp.getTime());
        contentValues.put(RSS_ITEM_STARRED, isStarred);
        contentValues.put(RSS_ITEM_GUID, guid);
        contentValues.put(RSS_ITEM_GUIDHASH, guidHash);

        contentValues.put(RSS_ITEM_READ_TEMP, isRead);
		contentValues.put(RSS_ITEM_STARRED_TEMP, isStarred);

        database.insert(RSS_ITEM_TABLE, null, contentValues);
    }
	
	public String getIdOfFolder (String FolderName) {
		//String buildSQL = "SELECT rowid as _id FROM " + SUBSCRIPTION_TABLE + " WHERE " + SUBSCRIPTION_HEADERTEXT  + " = '" + SubscriptionName + "'";
		String buildSQL = "SELECT rowid as _id FROM " + FOLDER_TABLE + " WHERE " + FOLDER_LABEL  + " = '" + FolderName + "'";
		
		String result = null;
        Cursor cursor = database.rawQuery(buildSQL, null);
        if (cursor != null)
        {
        	if(cursor.moveToFirst())
        		result = cursor.getString(0);
        }
        cursor.close();
        
        return result;
    }
	
	public String getIdOfFolderByLabelPath (String FolderLabelPath) {
		//String buildSQL = "SELECT rowid as _id FROM " + SUBSCRIPTION_TABLE + " WHERE " + SUBSCRIPTION_HEADERTEXT  + " = '" + SubscriptionName + "'";
		String buildSQL = "SELECT rowid as _id FROM " + FOLDER_TABLE + " WHERE " + FOLDER_LABEL_ID  + " = '" + FolderLabelPath + "'";
		
		String result = null;
        Cursor cursor = database.rawQuery(buildSQL, null);
        if (cursor != null)
        {
        	if(cursor.moveToFirst())
        		result = cursor.getString(0);
        }
        cursor.close();
        
        return result;
    }
	
	public String getRowIdBySubscriptionID (String StreamID) {
		//String buildSQL = "SELECT rowid as _id FROM " + SUBSCRIPTION_TABLE + " WHERE " + SUBSCRIPTION_HEADERTEXT  + " = '" + SubscriptionName + "'";
		String buildSQL = "SELECT rowid as _id FROM " + SUBSCRIPTION_TABLE + " WHERE " + SUBSCRIPTION_ID  + " = '" + StreamID + "'";
		
		String result = null;
        Cursor cursor = database.rawQuery(buildSQL, null);
        if (cursor != null)
        {
        	if(cursor.moveToFirst())
        		result = cursor.getString(0);
        }
        cursor.close();
        
        return result;
    }
	
	public String getSubscriptionIdByRowID (String ID) {
		
		String buildSQL = "SELECT " + SUBSCRIPTION_ID + " FROM " + SUBSCRIPTION_TABLE + " WHERE rowid = '" + ID + "'";
        Cursor cursor = database.rawQuery(buildSQL, null);
        try
        {
	        if (cursor != null)
	        {
	        	if(cursor.moveToFirst())
	        		return cursor.getString(0);
	        }
        } finally {
        	if (cursor != null)	        
        		cursor.close();
        }
        
        return null;
    }
	
	public String getTitleOfFolderByID (String FolderID) {
		//String buildSQL = "SELECT " + SUBSCRIPTION_HEADERTEXT + " FROM " + SUBSCRIPTION_TABLE + " WHERE rowid = '" + SubscriptionID + "'";
		String buildSQL = "SELECT " + FOLDER_LABEL + " FROM " + FOLDER_TABLE + " WHERE rowid = '" + FolderID + "'";
		
		String result = null;
        Cursor cursor = database.rawQuery(buildSQL, null);
        if (cursor != null)
        {
            if(cursor.moveToFirst())
            	result = cursor.getString(0);
        }
        cursor.close();
        
        return result;
    }
	
	public String getTitleOfSubscriptionByRowID (String SubscriptionID) {
		String buildSQL = "SELECT " + SUBSCRIPTION_HEADERTEXT + " FROM " + SUBSCRIPTION_TABLE + " WHERE rowid = '" + SubscriptionID + "'";
		
		String result = null;
        Cursor cursor = database.rawQuery(buildSQL, null);
        if (cursor != null)
        {
            if(cursor.moveToFirst())
            	result = cursor.getString(0);
        }
        cursor.close();
        
        return result;
    }
	
	public String getTitleOfSubscriptionByFeedItemID (String SubscriptionID) {
		String buildSQL = "SELECT " + SUBSCRIPTION_HEADERTEXT + " FROM " + SUBSCRIPTION_TABLE + " sc " +
							"JOIN " + RSS_ITEM_TABLE + " rss ON sc.rowid = rss." + RSS_ITEM_SUBSCRIPTION_ID + " " +
							"WHERE rss.rowid = '" + SubscriptionID + "'";
		
		String result = null;
        Cursor cursor = database.rawQuery(buildSQL, null);
        if (cursor != null)
        {
            if(cursor.moveToFirst())
            	result = cursor.getString(0);
        }
        cursor.close();
        
        return result;
    }
	
	public int removeTopSubscriptionItemByTag(String TAG)
	{
		return database.delete(SUBSCRIPTION_TABLE, SUBSCRIPTION_HEADERTEXT + " = ?", new String[] { TAG });	
	}
	
	public int removeFolderByFolderLabel(String FolderLabel)
	{
		return database.delete(FOLDER_TABLE, FOLDER_LABEL + " = ?", new String[] { FolderLabel });	
	}
	
	public /*String[]*/ List<String> convertCursorToStringArray(Cursor cursor, int column_id)
	{
	    List<String> items = new ArrayList<String>();	    		
	    if(cursor.getCount() > 0)
	    {
		    cursor.moveToFirst();
		    do {	      
		    	items.add(cursor.getString(column_id));
		    } while(cursor.moveToNext());
	    }
	    // Make sure to close the cursor
	    cursor.close();
	    //return (String[]) items.toArray();
	    return items;
		  
	}
	
	public Boolean doesRssItemAlreadyExsists(String idRssItem)//idRssItem = id aus dem Google Reader Stream
	{
		String buildSQL = "SELECT " + RSS_ITEM_TITLE + " FROM " + RSS_ITEM_TABLE + " WHERE rssitem_id = '" + idRssItem + "'";		
        Cursor cursor = database.rawQuery(buildSQL, null);
        int count = cursor.getCount();
        cursor.close();
        if(count > 0)
        	return true;
        else
        	return false;
	}
	
	public int resetRssItemsDatabase()
	{			
        int result = database.delete(RSS_ITEM_TABLE, null, null);
        return result;
	}
	
	public void resetDatabase()
	{
		openHelper.resetDatabase(database);
	}
	
	public void openDatabase()
    {
    	database = openHelper.getWritableDatabase();
    }
	
	public void closeDatabase()
	{
		database.close();
	}
}
