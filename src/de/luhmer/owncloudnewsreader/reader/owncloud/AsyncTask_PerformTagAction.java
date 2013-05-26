package de.luhmer.owncloudnewsreader.reader.owncloud;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import de.luhmer.owncloudnewsreader.reader.AsyncTask_Reader;
import de.luhmer.owncloudnewsreader.reader.FeedItemTags.TAGS;
import de.luhmer.owncloudnewsreader.reader.OnAsyncTaskCompletedListener;

public class AsyncTask_PerformTagAction extends AsyncTask<Object, Void, Boolean> implements AsyncTask_Reader {
	
	private Context context;
	private int task_id;
	private OnAsyncTaskCompletedListener[] listener;
	
	public AsyncTask_PerformTagAction(final int task_id, final Context context, final OnAsyncTaskCompletedListener[] listener) {
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
	protected Boolean doInBackground(Object... params) {
		String itemId = (String) params[0];
		TAGS tag = (TAGS) params[1];
		
		try {
			//String authKey = AuthenticationManager.getGoogleAuthKey(username, password);
			return OwnCloudReaderMethods.PerformTagExecution(itemId, tag, context);				
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
    @Override
    protected void onPostExecute(Boolean values) {    	
    	for (OnAsyncTaskCompletedListener listenerInstance : listener) {
    		if(listenerInstance != null)
    			listenerInstance.onAsyncTaskCompleted(task_id, values);	
		}
    	
		detach();
    }
}
