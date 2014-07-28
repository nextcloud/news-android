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

import java.util.ArrayList;
import java.util.List;

import de.luhmer.owncloudnewsreader.database.DatabaseConnectionOrm;
import de.luhmer.owncloudnewsreader.reader.AsyncTask_Reader;
import de.luhmer.owncloudnewsreader.reader.FeedItemTags.TAGS;
import de.luhmer.owncloudnewsreader.reader.OnAsyncTaskCompletedListener;

public class AsyncTask_PerformItemStateChange extends AsyncTask_Reader
{
	private API api;
	
	public AsyncTask_PerformItemStateChange(final int task_id, final Context context, final OnAsyncTaskCompletedListener[] listener, API api) {
		super(task_id, context, listener);		 
		this.api = api;
	}	
	
	@Override
	protected Boolean doInBackground(Object... params) {

		List<Boolean> succeeded = new ArrayList<Boolean>();
		
		try {
			DatabaseConnectionOrm dbConn = new DatabaseConnectionOrm(context);
			try {
				//Mark as READ
				List<String> itemIds = dbConn.getRssItemsIdsFromList(dbConn.getAllNewReadRssItems());
				boolean result = api.PerformTagExecution(itemIds, TAGS.MARK_ITEM_AS_READ, context, api);
				if(result)
					dbConn.change_readUnreadStateOfItem(itemIds, true);
				succeeded.add(result);
				
				//Mark as UNREAD
				itemIds = dbConn.getRssItemsIdsFromList(dbConn.getAllNewUnreadRssItems());
				result = api.PerformTagExecution(itemIds, TAGS.MARK_ITEM_AS_UNREAD, context, api);
				if(result)
					dbConn.change_readUnreadStateOfItem(itemIds, false);
				succeeded.add(result);
				
				//Mark as STARRED
				itemIds = dbConn.getRssItemsIdsFromList(dbConn.getAllNewStarredRssItems());
				result = api.PerformTagExecution(itemIds, TAGS.MARK_ITEM_AS_STARRED, context, api);
				if(result)
					dbConn.change_starrUnstarrStateOfItem(itemIds, true);
				succeeded.add(result);
				
				//Mark as UNSTARRED
				itemIds = dbConn.getRssItemsIdsFromList(dbConn.getAllNewUnstarredRssItems());
				result = api.PerformTagExecution(itemIds, TAGS.MARK_ITEM_AS_UNSTARRED, context, api);
				if(result)
					dbConn.change_starrUnstarrStateOfItem(itemIds, false);
				succeeded.add(result);
			} finally {
				//dbConn.closeDatabase();
			}
		} catch (Exception e) {
			e.printStackTrace();
			succeeded.add(false);
		}
		
		if(succeeded.contains(false))
			return false;
		else
			return true;
	}
	
    @Override
    protected void onPostExecute(Object values) {    	
    	for (OnAsyncTaskCompletedListener listenerInstance : listener) {
    		if(listenerInstance != null)
    			listenerInstance.onAsyncTaskCompleted(task_id, values);	
		}
    	
		detach();
    }
}
