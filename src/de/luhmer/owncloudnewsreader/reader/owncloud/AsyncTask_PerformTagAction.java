package de.luhmer.owncloudnewsreader.reader.owncloud;

import java.util.List;

import android.app.Activity;
import android.os.AsyncTask;
import de.luhmer.owncloudnewsreader.reader.AsyncTask_Reader;
import de.luhmer.owncloudnewsreader.reader.FeedItemTags.TAGS;
import de.luhmer.owncloudnewsreader.reader.OnAsyncTaskCompletedListener;
import de.luhmer.owncloudnewsreader.reader.owncloud.apiv2.APIv2;

public class AsyncTask_PerformTagAction extends AsyncTask_Reader {
	
	public AsyncTask_PerformTagAction(final int task_id, final Activity context, final OnAsyncTaskCompletedListener[] listener) {
		super(task_id, context, listener);
	}	
	
	@SuppressWarnings("unchecked")
	@Override
	protected Boolean doInBackground(Object... params) {
		List<String> itemIds = (List<String>) params[0];
		TAGS tag = (TAGS) params[1];
		
		try {
			//String authKey = AuthenticationManager.getGoogleAuthKey(username, password);
			API api = new APIv2(context);
			
			if(itemIds.size() > 0)
				return api.PerformTagExecution(itemIds, tag, context, api);
			else
				return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
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
