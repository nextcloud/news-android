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

import java.util.List;

import de.luhmer.owncloudnewsreader.database.DatabaseConnectionOrm;
import de.luhmer.owncloudnewsreader.reader.AsyncTask_Reader;
import de.luhmer.owncloudnewsreader.reader.FeedItemTags;
import de.luhmer.owncloudnewsreader.reader.OnAsyncTaskCompletedListener;

public class AsyncTask_PerformItemStateChange extends AsyncTask_Reader
{
	public AsyncTask_PerformItemStateChange(final Context context, final OnAsyncTaskCompletedListener... listener) {
		super(context, listener);
	}

	@Override
	protected Exception doInBackground(Object... params) {
		boolean succeeded;

		try {
			DatabaseConnectionOrm dbConn = new DatabaseConnectionOrm(context);

			//Mark as READ
			List<String> itemIds = dbConn.getRssItemsIdsFromList(dbConn.getAllNewReadRssItems());
			boolean result = apiFuture.get().PerformTagExecution(itemIds, FeedItemTags.MARK_ITEM_AS_READ, context);
			if(result)
				dbConn.change_readUnreadStateOfItem(itemIds, true);
			succeeded = result;

			//Mark as UNREAD
			itemIds = dbConn.getRssItemsIdsFromList(dbConn.getAllNewUnreadRssItems());
			result = apiFuture.get().PerformTagExecution(itemIds, FeedItemTags.MARK_ITEM_AS_UNREAD, context);
			if(result)
				dbConn.change_readUnreadStateOfItem(itemIds, false);
			succeeded &= result;

			//Mark as STARRED
			itemIds = dbConn.getRssItemsIdsFromList(dbConn.getAllNewStarredRssItems());
			result = apiFuture.get().PerformTagExecution(itemIds, FeedItemTags.MARK_ITEM_AS_STARRED, context);
			if(result)
				dbConn.change_starrUnstarrStateOfItem(itemIds, true);
			succeeded &= result;

			//Mark as UNSTARRED
			itemIds = dbConn.getRssItemsIdsFromList(dbConn.getAllNewUnstarredRssItems());
			result = apiFuture.get().PerformTagExecution(itemIds, FeedItemTags.MARK_ITEM_AS_UNSTARRED, context);
			if(result)
				dbConn.change_starrUnstarrStateOfItem(itemIds, false);
			succeeded &= result;
		} catch (Exception e) {
			return e;
		}

		Exception e = null;
		if(!succeeded)
			e = new Exception("Performing item state change failed");
        return e;
	}

    @Override
    protected void onPostExecute(Exception ex) {
     	for (OnAsyncTaskCompletedListener listenerInstance : listener) {
    		if(listenerInstance != null)
    			listenerInstance.onAsyncTaskCompleted(ex);
		}

		detach();
    }
}
