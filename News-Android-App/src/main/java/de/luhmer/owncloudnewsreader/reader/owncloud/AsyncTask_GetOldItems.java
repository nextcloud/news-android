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

import android.content.Context;
import android.widget.Toast;

import de.luhmer.owncloudnewsreader.ListView.SubscriptionExpandableListAdapter;
import de.luhmer.owncloudnewsreader.NewsReaderDetailFragment;
import de.luhmer.owncloudnewsreader.NewsReaderListActivity;
import de.luhmer.owncloudnewsreader.R;
import de.luhmer.owncloudnewsreader.database.DatabaseConnectionOrm;
import de.luhmer.owncloudnewsreader.database.model.RssItem;
import de.luhmer.owncloudnewsreader.reader.AsyncTask_Reader;
import de.luhmer.owncloudnewsreader.reader.FeedItemTags.TAGS;
import de.luhmer.owncloudnewsreader.reader.OnAsyncTaskCompletedListener;

public class AsyncTask_GetOldItems extends AsyncTask_Reader {

    private static final String TAG = "AsyncTask_GetOldItems";
    public Long feed_id;
    public Long folder_id;
    private int downloadedItemsCount = 0;
    private API api;

    public AsyncTask_GetOldItems(final int task_id, final Context context, final OnAsyncTaskCompletedListener[] listener, Long feed_id, Long folder_id, API api) {
    	super(task_id, context, listener);

        this.feed_id = feed_id;
        this.folder_id = folder_id;
        this.api = api;
    }

	@Override
	protected Exception doInBackground(Object... params) {
		DatabaseConnectionOrm dbConn = new DatabaseConnectionOrm(context);
        try {
        	long offset = 0;
        	//int requestCount = 0;
        	//int maxSyncSize = Integer.parseInt(OwnCloudReaderMethods.maxSizePerSync);
        	Long id = null;
        	String type = null;

        	if(feed_id != null)
        	{
                RssItem rssItem = dbConn.getLowestRssItemIdByFeed(feed_id);
        		offset = rssItem.getId();//TODO needs testing!
        		id = feed_id;
        		type = "0";
        	}
        	else if(folder_id != null)
        	{
        		if(folder_id == SubscriptionExpandableListAdapter.SPECIAL_FOLDERS.ALL_STARRED_ITEMS.getValue())//TODO needs testing!
        		{
        			offset = dbConn.getLowestItemId(true);
        			id = 0L;
        			type = "2";
        		} else {
                    offset = dbConn.getLowestItemIdByFolder(folder_id);
        			id = folder_id;//dbConn.getIdOfFolderByLabelPath(folder_id);
        			type = "1";
        		}
        	}


        	downloadedItemsCount = api.GetItems(TAGS.ALL, context, String.valueOf(offset), true, id.intValue(), type, api);

            /*
            int totalCount = dbConn.getCountOfAllItems(false);

            //If the number of items in the database is bigger than the maximum allowed number of items
            if(totalCount > Constants.maxItemsCount) {
                String feedIdDb = dbConn.getRowIdBySubscriptionID(feed_id);
                dbConn.removeXLatestItems(totalCount - Constants.maxItemsCount, feedIdDb);
                Log.d(TAG, "Deleted starred-items in order to free up enough space for the read items");
            }
            */

        } catch (Exception ex) {
            return ex;
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



        if(context instanceof  NewsReaderListActivity) {
            NewsReaderListActivity activity = (NewsReaderListActivity) context;
            NewsReaderDetailFragment nrD = (NewsReaderDetailFragment) activity.getSupportFragmentManager().findFragmentById(R.id.content_frame);
            if(nrD != null)
                nrD.UpdateCurrentRssView(context, true);
        }

		detach();
    }
}
