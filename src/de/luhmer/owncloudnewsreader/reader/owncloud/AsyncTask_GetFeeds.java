package de.luhmer.owncloudnewsreader.reader.owncloud;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import de.luhmer.owncloudnewsreader.database.DatabaseConnection;
import de.luhmer.owncloudnewsreader.reader.AsyncTask_Reader;
import de.luhmer.owncloudnewsreader.reader.FeedItemTags.TAGS;
import de.luhmer.owncloudnewsreader.reader.OnAsyncTaskCompletedListener;
import de.luhmer.owncloudnewsreader.services.DownloadImagesService;

public class AsyncTask_GetFeeds extends AsyncTask<Object, Void, Exception> implements AsyncTask_Reader {
		
    private Activity context;
    private int task_id;
    private OnAsyncTaskCompletedListener[] listener;
    private long highestItemIdBeforeSync; 
    
    public AsyncTask_GetFeeds(final int task_id, final Activity context, final OnAsyncTaskCompletedListener[] listener) {
          super();

          this.context = context;
          this.task_id = task_id;
          this.listener = listener;
    }

    //Activity meldet sich zurueck nach OrientationChange
	public void attach(final Activity context, final OnAsyncTaskCompletedListener[] listener) {
		this.context = context;
		this.listener = listener;	
	}
		  
	//Activity meldet sich ab
	public void detach() {		
		if (context != null) {
			context = null;
		}
		 
		if (listener != null) {
			listener = null;
		}
	}
	
	@Override
	protected Exception doInBackground(Object... params) {
		//FeedItemTags.TAGS tag = (TAGS) params[0];
		/*
		String username = (String) params[0];
		String password = (String) params[1];
		String _TAG_LABEL = (String) params[2];
		*/

		DatabaseConnection dbConn = new DatabaseConnection(context);
        try {
		    //String authKey = AuthenticationManager.getGoogleAuthKey(username, password);
        	//SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        	//int maxItemsInDatabase = Integer.parseInt(mPrefs.getString(SettingsActivity.SP_MAX_ITEMS_SYNC, "200"));
        	        	
        	long lastModified = dbConn.getLastModfied();
        	
        	//List<RssFile> files;
        	long offset = dbConn.getLowestItemId(false);
        	//int totalCount = 0;
        	int requestCount = 0;
        	int maxSyncSize = Integer.parseInt(OwnCloudReaderMethods.maxSizePerSync);
        	
        	highestItemIdBeforeSync = dbConn.getHighestItemId();
        	
        	if(lastModified == 0)
        	{
        		//init startup
	        	do {    
	        		requestCount = OwnCloudReaderMethods.GetItems(TAGS.ALL_UNREAD, context, String.valueOf(offset), false);
	        		if(requestCount > 0)
	        			offset = dbConn.getLowestItemId(false);
	        		//totalCount += requestCount;
	        	} while(requestCount == maxSyncSize /*&& totalCount < maxItemsInDatabase*/);
	        	
	        	do {  
	        		offset = dbConn.getLowestItemId(true);
	        		requestCount = OwnCloudReaderMethods.GetItems(TAGS.ALL_STARRED, context, String.valueOf(offset), true);
	        		if(requestCount > 0)
	        			offset = dbConn.getLowestItemId(true);
	        	} while(requestCount == maxSyncSize);
        	}
        	else
        	{	
        		//OwnCloudReaderMethods.GetUpdatedItems(tag, context, lastModified, true);
        		OwnCloudReaderMethods.GetUpdatedItems(TAGS.ALL_UNREAD, context, lastModified);
        		//OwnCloudReaderMethods.GetUpdatedItems(TAGS.ALL_STARRED, context, lastModified);
        	}
        	
        	/*
        	//Get all unread items which are older then the latest item id in db            	
        	do {    
        		requestCount = OwnCloudReaderMethods.GetItems(tag, context, String.valueOf(offset), false);
        		//InsertIntoDatabase.InsertFeedItemsIntoDatabase(files, context);
        		
        		if(requestCount > 0)
        			offset = dbConn.getLowestItemId();
        		
        		totalCount += requestCount;
        	} while(requestCount == maxSyncSize && totalCount < maxItemsInDatabase);
        	
        	
        	if(lastModified == 0 && totalCount < maxItemsInDatabase)//If the app should sync all the items in past.
        	{	
            	do {
            		requestCount = OwnCloudReaderMethods.GetItems(tag, context, String.valueOf(offset), true);
            		//InsertIntoDatabase.InsertFeedItemsIntoDatabase(files, context);
            		
            		if(requestCount > 0)
            			offset = dbConn.getLowestItemId();
            		
            		totalCount += requestCount;      		
            	} while(requestCount == maxSyncSize && totalCount < maxItemsInDatabase);
        	}
        	else if(lastModified != 0)
        	{
        		OwnCloudReaderMethods.GetUpdatedItems(tag, context, lastModified, true);
        		//InsertIntoDatabase.InsertFeedItemsIntoDatabase(files, context);
        	}
        	
        	if(dbConn.getCountOfAllItems(true) > maxItemsInDatabase && !tag.equals(FeedItemTags.TAGS.ALL_STARRED))//Remove all old items which are over the limit of maxItemsInDatabase
        	{
        		String id_db = String.valueOf(dbConn.getItemDbIdAtPosition(maxItemsInDatabase));
        		dbConn.removeAllItemsWithIdLowerThan(id_db);
        	}*/
        } catch (Exception ex) {
            return ex;
        } finally {
        	dbConn.closeDatabase();
        }
        return null;
	}
	
    @Override
    protected void onPostExecute(Exception ex) {
    	for (OnAsyncTaskCompletedListener listenerInstance : listener) {
    		if(listenerInstance != null)
    			listenerInstance.onAsyncTaskCompleted(task_id, ex);
		}
    	/*
    	if(task_id == 3)//All Starred Item request was performed
    	{
    		Intent service = new Intent(context, DownloadImagesService.class);
    		context.startService(service);
    	}*/
    	
    	DatabaseConnection dbConn = new DatabaseConnection(context);
    	try {
    		Intent service = new Intent(context, DownloadImagesService.class);
        	service.putExtra(DownloadImagesService.LAST_ITEM_ID, highestItemIdBeforeSync);
    		context.startService(service);
    	} finally {
    		dbConn.closeDatabase();
    	}
    	
    	
		detach();
    }
}
