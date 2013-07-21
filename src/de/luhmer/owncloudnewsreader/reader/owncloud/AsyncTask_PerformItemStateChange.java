package de.luhmer.owncloudnewsreader.reader.owncloud;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import de.luhmer.owncloudnewsreader.database.DatabaseConnection;
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
			DatabaseConnection dbConn = new DatabaseConnection(context);
			try {
				//Mark as READ
				List<String> itemIds = dbConn.getAllNewReadItems();
				boolean result = api.PerformTagExecution(itemIds, TAGS.MARK_ITEM_AS_READ, context, api);
				if(result)
					dbConn.change_readUnreadStateOfItem(itemIds, true);
				succeeded.add(result);
				
				//Mark as UNREAD
				itemIds = dbConn.getAllNewUnreadItems();
				result = api.PerformTagExecution(itemIds, TAGS.MARK_ITEM_AS_UNREAD, context, api);
				if(result)
					dbConn.change_readUnreadStateOfItem(itemIds, false);
				succeeded.add(result);
				
				//Mark as STARRED
				itemIds = dbConn.getAllNewStarredItems();
				result = api.PerformTagExecution(itemIds, TAGS.MARK_ITEM_AS_STARRED, context, api);
				if(result)
					dbConn.change_starrUnstarrStateOfItem(itemIds, true);
				succeeded.add(result);
				
				//Mark as UNSTARRED
				itemIds = dbConn.getAllNewUnstarredItems();
				result = api.PerformTagExecution(itemIds, TAGS.MARK_ITEM_AS_UNSTARRED, context, api);
				if(result)
					dbConn.change_starrUnstarrStateOfItem(itemIds, false);
				succeeded.add(result);
			} finally {
				dbConn.closeDatabase();
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
