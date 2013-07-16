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
				succeeded.add(api.PerformTagExecution(itemIds, TAGS.MARK_ITEM_AS_READ, context, api));
				
				//Mark as UNREAD
				itemIds = dbConn.getAllNewUnreadItems();
				succeeded.add(api.PerformTagExecution(itemIds, TAGS.MARK_ITEM_AS_UNREAD, context, api));
				
				//Mark as STARRED
				itemIds = dbConn.getAllNewStarredItems();
				succeeded.add(api.PerformTagExecution(itemIds, TAGS.MARK_ITEM_AS_STARRED, context, api));
				
				//Mark as UNSTARRED
				itemIds = dbConn.getAllNewUnstarredItems();
				succeeded.add(api.PerformTagExecution(itemIds, TAGS.MARK_ITEM_AS_UNSTARRED, context, api));
			} finally {
				dbConn.closeDatabase();
			}
			//if(itemIds.size() > 0)
			//	return api.PerformTagExecution(itemIds, tag, context, api);
			//else
			//	return true;
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
