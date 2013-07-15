package de.luhmer.owncloudnewsreader.reader.owncloud;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import de.luhmer.owncloudnewsreader.Constants;
import de.luhmer.owncloudnewsreader.R;
import de.luhmer.owncloudnewsreader.SettingsActivity;
import de.luhmer.owncloudnewsreader.database.DatabaseConnection;
import de.luhmer.owncloudnewsreader.helper.NetworkConnection;
import de.luhmer.owncloudnewsreader.reader.AsyncTask_Reader;
import de.luhmer.owncloudnewsreader.reader.FeedItemTags.TAGS;
import de.luhmer.owncloudnewsreader.reader.OnAsyncTaskCompletedListener;
import de.luhmer.owncloudnewsreader.reader.owncloud.apiv2.APIv2;
import de.luhmer.owncloudnewsreader.services.DownloadImagesService;

public class AsyncTask_GetItems extends AsyncTask_Reader {
    private long highestItemIdBeforeSync; 
    
    public AsyncTask_GetItems(final int task_id, final Activity context, final OnAsyncTaskCompletedListener[] listener) {
    	super(task_id, context, listener);
    }
	
	@Override
	protected Exception doInBackground(Object... params) {
		DatabaseConnection dbConn = new DatabaseConnection(context);
        try {
		    //String authKey = AuthenticationManager.getGoogleAuthKey(username, password);
        	//SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        	//int maxItemsInDatabase = Integer.parseInt(mPrefs.getString(SettingsActivity.SP_MAX_ITEMS_SYNC, "200"));
        	        	
        	long lastModified = dbConn.getLastModfied();
        	
        	//List<RssFile> files;
        	long offset = dbConn.getLowestItemId(false);
        	int totalCount = 0;
        	int requestCount = 0;
        	int maxSyncSize = Integer.parseInt(OwnCloudReaderMethods.maxSizePerSync);
        	int maxItemsInDatabase = Constants.maxItemsCount;
        	
        	highestItemIdBeforeSync = dbConn.getHighestItemId();
        	
        	API api = new APIv2(context);
        	
        	if(lastModified == 0)
        	{	
	        	do {    
	        		requestCount = api.GetItems(TAGS.ALL, context, String.valueOf(offset), false, "0", "3", api);
	        		if(requestCount > 0)
	        			offset = dbConn.getLowestItemId(false);
	        		totalCount += requestCount;	        		
	        	} while(requestCount == maxSyncSize /* && totalCount < maxItemsInDatabase */);
	        	
	        	do {  
	        		offset = dbConn.getLowestItemId(true);
	        		requestCount = api.GetItems(TAGS.ALL_STARRED, context, String.valueOf(offset), true, "0", "2", api);
	        		if(requestCount > 0)
	        			offset = dbConn.getLowestItemId(true);
	        		totalCount += requestCount;
	        	} while(requestCount == maxSyncSize && totalCount < maxItemsInDatabase);
        	}
        	else
        	{	
        		api.GetUpdatedItems(TAGS.ALL, context, lastModified, api);
        		//OwnCloudReaderMethods.GetUpdatedItems(TAGS.ALL, context, lastModified, api);
        		
        	}
        } catch (Exception ex) {
            return ex;
        } finally {
        	dbConn.closeDatabase();
        }
        return null;
	}
	
    @Override
    protected void onPostExecute(Object ex) {
    	for (OnAsyncTaskCompletedListener listenerInstance : listener) {
    		if(listenerInstance != null)
    			listenerInstance.onAsyncTaskCompleted(task_id, ex);
		}
    	
    	SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
    	if(mPrefs.getBoolean(SettingsActivity.CB_CACHE_IMAGES_OFFLINE_STRING, false))
    	{        		
    		if(!NetworkConnection.isWLANConnected(context) && NetworkConnection.isNetworkAvailable(context))
    			ShowDownloadImageWithoutWifiQuestion();
    		else if(NetworkConnection.isNetworkAvailable(context)) 		
    			StartDownloadingImages();
    	}
    	
    	
		detach();
    }
    
    private void StartDownloadingImages()
    {
    	DatabaseConnection dbConn = new DatabaseConnection(context);
    	try {
    		Intent service = new Intent(context, DownloadImagesService.class);
        	service.putExtra(DownloadImagesService.LAST_ITEM_ID, highestItemIdBeforeSync);
    		context.startService(service);
    	} finally {
    		dbConn.closeDatabase();
    	}
    }
    
    
    private void ShowDownloadImageWithoutWifiQuestion()
    {
    	AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
 
		// set title
		alertDialogBuilder.setTitle(context.getString(R.string.no_wifi_available));
 
			// set dialog message
		alertDialogBuilder
			.setMessage(context.getString(R.string.do_you_want_to_download_without_wifi))
			.setCancelable(true)
			.setPositiveButton("Yes",new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,int id) {
					StartDownloadingImages();
				}
			}); 
						
		AlertDialog alertDialog = alertDialogBuilder.create();
 
		alertDialog.show();		
    }
}
