package de.luhmer.owncloudnewsreader.reader.owncloud;

import android.app.Activity;
import android.os.AsyncTask;
import android.widget.Toast;
import de.luhmer.owncloudnewsreader.R;
import de.luhmer.owncloudnewsreader.database.DatabaseConnection;
import de.luhmer.owncloudnewsreader.reader.AsyncTask_Reader;
import de.luhmer.owncloudnewsreader.reader.FeedItemTags.TAGS;
import de.luhmer.owncloudnewsreader.reader.OnAsyncTaskCompletedListener;

public class AsyncTask_GetOldItems extends AsyncTask<Object, Void, Exception> implements AsyncTask_Reader {
		
    private Activity context;
    private int task_id;
    private OnAsyncTaskCompletedListener[] listener;
    public String feed_id;
    public String folder_id;
    private int downloadedItemsCount = 0;
    
    public AsyncTask_GetOldItems(final int task_id, final Activity context, final OnAsyncTaskCompletedListener[] listener, String feed_id, String folder_id) {
          super();

          this.context = context;
          this.task_id = task_id;
          this.listener = listener;
          this.feed_id = feed_id;
          this.folder_id = folder_id;
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
		DatabaseConnection dbConn = new DatabaseConnection(context);
        try {
        	long offset = 0;        	
        	//int requestCount = 0;
        	//int maxSyncSize = Integer.parseInt(OwnCloudReaderMethods.maxSizePerSync);
        	String id = null;
        	String type = null;
        	
        	if(feed_id != null)
        	{
        		offset = dbConn.getLowestItemIdByFeed(feed_id);
        		id = dbConn.getSubscriptionIdByRowID(feed_id);
        		type = "0";
        	}
        	else if(folder_id != null)
        	{
        		offset = dbConn.getLowestItemIdByFolder(folder_id);
        		id = dbConn.getIdOfFolderByLabelPath(folder_id);
        		type = "1";
        	}
        	
        	
        	downloadedItemsCount = OwnCloudReaderMethods.GetItems(TAGS.ALL, context, String.valueOf(offset), true, id, type);
        	
        	
        	//do {    
        	//requestCount = OwnCloudReaderMethods.GetItems(TAGS.ALL, context, String.valueOf(offset), true, feed_id);
        	//	if(requestCount > 0)
        	//		offset = dbConn.getLowestItemIdByFeed(feed_id);
        	//} while(requestCount == maxSyncSize);
        	
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
    	
    	if(downloadedItemsCount == 0)
    		Toast.makeText(context, context.getString(R.string.toast_no_more_downloads_available), Toast.LENGTH_LONG).show();
    	else
    	{
    		String text = context.getString(R.string.toast_downloaded_x_items).replace("X", String.valueOf(downloadedItemsCount));
    		Toast.makeText(context, text, Toast.LENGTH_LONG).show();
    	}
    	
    	/*
    	DatabaseConnection dbConn = new DatabaseConnection(context);
    	try {
    		Intent service = new Intent(context, DownloadImagesService.class);
        	service.putExtra(DownloadImagesService.LAST_ITEM_ID, highestItemIdBeforeSync);
    		context.startService(service);
    	} finally {
    		dbConn.closeDatabase();
    	}
    	*/
    	
		detach();
    }
}
