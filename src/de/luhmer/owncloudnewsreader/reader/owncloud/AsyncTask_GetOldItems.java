/**
* Android ownCloud News
*
* @author David Luhmer
* @copyright 2013 David Luhmer david-dev@live.de
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU AFFERO GENERAL PUBLIC LICENSE
* License as published by the Free Software Foundation; either
* version 3 of the License, or any later version.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU AFFERO GENERAL PUBLIC LICENSE for more details.
*
* You should have received a copy of the GNU Affero General Public
* License along with this library.  If not, see <http://www.gnu.org/licenses/>.
*
*/

package de.luhmer.owncloudnewsreader.reader.owncloud;

import android.app.Activity;
import android.widget.Toast;
import de.luhmer.owncloudnewsreader.R;
import de.luhmer.owncloudnewsreader.ListView.SubscriptionExpandableListAdapter;
import de.luhmer.owncloudnewsreader.database.DatabaseConnection;
import de.luhmer.owncloudnewsreader.reader.AsyncTask_Reader;
import de.luhmer.owncloudnewsreader.reader.FeedItemTags.TAGS;
import de.luhmer.owncloudnewsreader.reader.OnAsyncTaskCompletedListener;

public class AsyncTask_GetOldItems extends AsyncTask_Reader {
	
    public String feed_id;
    public String folder_id;
    private int downloadedItemsCount = 0;
    private API api;
    
    public AsyncTask_GetOldItems(final int task_id, final Activity context, final OnAsyncTaskCompletedListener[] listener, String feed_id, String folder_id, API api) {
    	super(task_id, context, listener);
    	
        this.feed_id = feed_id;
        this.folder_id = folder_id;
        this.api = api;
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
        		if(folder_id.equals(SubscriptionExpandableListAdapter.ALL_STARRED_ITEMS))
        		{
        			offset = dbConn.getLowestItemIdStarred();
        			id = "0";
        			type = "2";        			
        		} else {
        			offset = dbConn.getLowestItemIdByFolder(folder_id);
        			id = dbConn.getIdOfFolderByLabelPath(folder_id);
        			type = "1";
        		}
        	}
        	
        	downloadedItemsCount = api.GetItems(TAGS.ALL, context, String.valueOf(offset), true, id, type, api);
        	//downloadedItemsCount = OwnCloudReaderMethods.GetItems(TAGS.ALL, context, String.valueOf(offset), true, id, type, api);
        	
        	
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
    protected void onPostExecute(Object ex) {
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
