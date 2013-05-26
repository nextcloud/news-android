package de.luhmer.owncloudnewsreader.reader.GoogleReaderApi;

import java.util.List;

import org.apache.http.NameValuePair;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import de.luhmer.owncloudnewsreader.reader.OnAsyncTaskCompletedListener;

public class AsyncTask_PerformTagAction extends AsyncTask<Object, Void, Boolean> {
	
	private Context context;	
	private int task_id;
	private OnAsyncTaskCompletedListener listener;
	
	public AsyncTask_PerformTagAction(final int task_id, final Context context2, final OnAsyncTaskCompletedListener listener) {
		  super();
		  
		  this.context = context2;
		  this.task_id = task_id;
		  this.listener = listener;
	}
		  
	//Activity meldet sich zurueck nach OrientationChange
	public void attach(final Activity context, final OnAsyncTaskCompletedListener listener) {
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
	
	
	@SuppressWarnings("unchecked")
	@Override
	protected Boolean doInBackground(Object... params) {
		String username = (String) params[0];
		String password = (String) params[1];
		List<NameValuePair> valuepairs = (List<NameValuePair>) params[2]; 
		
		try {
			//String authKey = AuthenticationManager.getGoogleAuthKey(username, password);
			return GoogleReaderMethods.performTagExecute(username, password, valuepairs);				
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
    @Override
    protected void onPostExecute(Boolean values) {    	
    	listener.onAsyncTaskCompleted(task_id, values);    	
		detach();
    }
}
