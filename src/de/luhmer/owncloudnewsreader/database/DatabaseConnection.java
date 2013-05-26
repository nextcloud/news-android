package de.luhmer.owncloudnewsreader.database;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
    
    public DatabaseConnection(Context aContext) {         
        openHelper = new DatabaseHelper(aContext);
        openDatabase();   
    }
	
	public Cursor getAllData(String TABLE_NAME) {		 
        String buildSQL = "SELECT rowid as _id, * FROM " + TABLE_NAME; 
        Log.d("DB_HELPER", "getAllData SQL: " + buildSQL); 
        return database.rawQuery(buildSQL, null);
    }
	
	public Cursor getAllTopSubscriptions() {
		//String buildSQL = "SELECT rowid as _id, * FROM " + SUBSCRIPTION_TABLE + " WHERE subscription_id_subscription IS NULL"; 
		String buildSQL = "SELECT rowid as _id, * FROM " + FOLDER_TABLE;
        Log.d("DB_HELPER", "getAllTopData SQL: " + buildSQL); 
        return database.rawQuery(buildSQL, null);
	}
	
	public Cursor getAllTopSubscriptionsWithUnreadFeeds() {
		//String buildSQL = "SELECT rowid as _id, * FROM " + SUBSCRIPTION_TABLE + " WHERE subscription_id_subscription IS NULL"; 
		String buildSQL = "SELECT f.rowid as _id, * FROM " + FOLDER_TABLE + " f "+
							" JOIN " + SUBSCRIPTION_TABLE + " sc ON f.rowid = sc." + SUBSCRIPTION_FOLDER_ID +
							" JOIN " + RSS_ITEM_TABLE + " rss ON sc.rowid = rss." + RSS_ITEM_SUBSCRIPTION_ID +
							" GROUP BY f.rowid " +
							" HAVING COUNT(*) > 0";
        Log.d("DB_HELPER", "getAllTopData SQL: " + buildSQL); 
        return database.rawQuery(buildSQL, null);
	}
	
	public Cursor getAllSubSubscriptions() {
		//String buildSQL = "SELECT rowid as _id, * FROM " + SUBSCRIPTION_TABLE + " WHERE subscription_id_subscription IS NOT NULL"; 
		String buildSQL = "SELECT rowid as _id, * FROM " + SUBSCRIPTION_TABLE;
        Log.d("DB_HELPER", "getAllSubData SQL: " + buildSQL); 
        return database.rawQuery(buildSQL, null);
	}
	
	public String getCountUnreadFeedsForSubscription(String ID_SUBSCRIPTION) {
		
		String buildSQL = "SELECT COUNT(*) " + 
	 			" FROM " + RSS_ITEM_TABLE +  
				" WHERE read != 1" +
					" AND subscription_id_subscription IN " + 
					"(SELECT rowid " + 
					"FROM subscription " +					
					"WHERE rowid = " + ID_SUBSCRIPTION + ");";
		
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
		if(checkUnread)
			buildSQL = "SELECT read "; 
		else//Wenn nicht checkRead auf true steht, soll geprueft werden ob das Feed Markiert ist.
			buildSQL = "SELECT starred ";
		buildSQL += " FROM " + RSS_ITEM_TABLE +  
					" WHERE rowid = " + FEED_ID;
		
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
		args.put(RSS_ITEM_READ, isRead);
		int result = database.update(RSS_ITEM_TABLE, args, "rowid=?", new String[] { FEED_ID });
		Log.d("RESULT UPDATE DATABASE", "RESULT: " + result);
    }
	
	public void updateIsStarredOfFeed(String FEED_ID, Boolean isStarred) {
		ContentValues args = new ContentValues();
	    //args.put(RSS_ITEM_READ, isRead);	    
		args.put(RSS_ITEM_STARRED, isStarred);
		int result = database.update(RSS_ITEM_TABLE, args, "rowid=?", new String[] { FEED_ID });
		Log.d("RESULT UPDATE DATABASE", "RESULT: " + result);
    }
	
	private String getAllFeedsSelectStatement()
	{
		return "SELECT rowid as _id, " + RSS_ITEM_TITLE + ", " + RSS_ITEM_RSSITEM_ID + ", " + RSS_ITEM_LINK + ", " + RSS_ITEM_BODY + ", " + RSS_ITEM_READ + ", " + RSS_ITEM_SUBSCRIPTION_ID + ", "
					+ RSS_ITEM_PUBDATE + ", " + RSS_ITEM_STARRED + ", " + RSS_ITEM_GUIDHASH + ", " + RSS_ITEM_GUID;
	}
	
	public Cursor getAllFeedsForSubscription(String ID_SUBSCRIPTION) {
		
		String buildSQL =  getAllFeedsSelectStatement() +
	 			" FROM " + RSS_ITEM_TABLE +  
				" WHERE subscription_id_subscription IN " + 
					"(SELECT rowid " + 
					"FROM subscription " +					
					"WHERE rowid = " + ID_SUBSCRIPTION + ");";
		
        Log.d("DB_HELPER", "getAllFeedData SQL: " + buildSQL); 
        return database.rawQuery(buildSQL, null);
    }
	
	public Cursor getFeedByID(String ID_FEED) {				
		String buildSQL = getAllFeedsSelectStatement() + 
	 			" FROM " + RSS_ITEM_TABLE +  
				" WHERE rowid = " + ID_FEED;
							
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
	
	public String getCountUnreadFeedsForFolder(String ID_FOLDER) {		
		String buildSQL = "SELECT COUNT(*) " +  
	 			" FROM " + RSS_ITEM_TABLE + 
	 			" WHERE read != 1 ";
		if(!ID_FOLDER.equals("-10"))
				buildSQL += " AND subscription_id_subscription IN " + 
					"(SELECT sc.rowid " + 
					"FROM subscription sc " +
					"JOIN folder f ON sc." + SUBSCRIPTION_FOLDER_ID + " = f.rowid " +
					"WHERE f.rowid = " + ID_FOLDER + ");";
		
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
	
	public String getCountFeedsForFolder(String ID_FOLDER) {		
		String buildSQL = "SELECT COUNT(*) " +  
	 			" FROM " + RSS_ITEM_TABLE;
		if(!(ID_FOLDER.equals("-10") || ID_FOLDER.equals("-11")))  
				buildSQL += " WHERE subscription_id_subscription IN " + 
					"(SELECT sc.rowid " + 
					"FROM subscription sc " +
					"JOIN folder f ON sc." + SUBSCRIPTION_FOLDER_ID + " = f.rowid " +
					"WHERE f.rowid = " + ID_FOLDER + ");";
		
		//if(ID_FOLDER.equals("-10"))//UNREAD
		//	buildSQL += " WHERE starred != 1 and read != 1";
		else if(ID_FOLDER.equals("-11"))//STARRED
			buildSQL += " WHERE starred = 1";
			
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
	
	public Cursor getAllFeedsForFolder(String ID_FOLDER) {
		String buildSQL = getAllFeedsSelectStatement() + 
	 			" FROM " + RSS_ITEM_TABLE;
	 	
	 	if(!(ID_FOLDER.equals("-10") || ID_FOLDER.equals("-11")))//Wenn nicht Alle Artikel ausgewaehlt wurde (-10) oder (-11) fuer Starred Feeds
			buildSQL += " WHERE subscription_id_subscription IN " + 
					"(SELECT sc.rowid " + 
					"FROM subscription sc " +
					"JOIN folder f ON sc." + SUBSCRIPTION_FOLDER_ID + " = f.rowid " +
					"WHERE f.rowid = " + ID_FOLDER + ");";
		
	 	//else if(ID_FOLDER.equals("-10"))
	 	//	buildSQL += " WHERE starred != 1 and read != 1";
	 	else if(ID_FOLDER.equals("-11"))
	 		buildSQL += " WHERE starred = 1";
	 	
        Log.d("DB_HELPER", "getAllFeedData SQL: " + buildSQL); 
        return database.rawQuery(buildSQL, null);
    }	
	
	public Cursor getAllSub_SubscriptionForSubscription(String ID_SUBSCRIPTION) {		 
        //String buildSQL = "SELECT rowid as _id, * FROM " + SUBSCRIPTION_TABLE + " WHERE " + SUBSCRIPTION_SUBSCRIPTION_ID + "=" + ID_SUBSCRIPTION; 
		String buildSQL = "SELECT sc.rowid as _id, * " + 
							"FROM " + SUBSCRIPTION_TABLE + " sc " +
							"JOIN folder f ON sc.folder_idfolder = f.rowid " +
							"WHERE f.rowid = " + ID_SUBSCRIPTION;
        Log.d("DB_HELPER", "getAllData SQL: " + buildSQL); 
        return database.rawQuery(buildSQL, null);
    }	
	
	public void insertNewFolder (String label, String label_path) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(FOLDER_LABEL, label);  
        contentValues.put(FOLDER_LABEL_ID, label_path);  
        database.insert(FOLDER_TABLE, null, contentValues);     
    }
	
	/*
	public void insertNewSubscription (String headerText) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(SUBSCRIPTION_HEADERTEXT, headerText);        
        database.insert(SUBSCRIPTION_TABLE, null, contentValues);     
    }*/
	
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
        if (cursor != null)
        {
        	if(cursor.moveToFirst())
        		return cursor.getString(0);
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
	
	public String getTitleOfSubscriptionByID (String SubscriptionID) {
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
	
	public void resetRssItemsDatabase()
	{			
        database.delete(RSS_ITEM_TABLE, null, null);
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
