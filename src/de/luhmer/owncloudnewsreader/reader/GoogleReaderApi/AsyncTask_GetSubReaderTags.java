package de.luhmer.owncloudnewsreader.reader.GoogleReaderApi;

import java.util.ArrayList;

import android.app.Activity;
import android.os.AsyncTask;
import de.luhmer.owncloudnewsreader.data.FolderSubscribtionItem;
import de.luhmer.owncloudnewsreader.reader.OnAsyncTaskCompletedListener;

public class AsyncTask_GetSubReaderTags extends AsyncTask<Object, Void, ArrayList<FolderSubscribtionItem>> {
		
		private Activity context;	
		private int task_id;
		private OnAsyncTaskCompletedListener listener;
		
		public AsyncTask_GetSubReaderTags(final int task_id, final Activity context, final OnAsyncTaskCompletedListener listener) {
			  super();
			  
			  this.context = context;
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
		
		
		@Override
		protected ArrayList<FolderSubscribtionItem> doInBackground(Object... params) {
			String username = (String) params[0];
			String password = (String) params[1];
			
			try {
				//String authKey = AuthenticationManager.getGoogleAuthKey(username, password);
				return GoogleReaderMethods.getSubList(username, password);				
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}
		
	    @Override
	    protected void onPostExecute(ArrayList<FolderSubscribtionItem> values) {
	    	
	    	listener.onAsyncTaskCompleted(task_id, values);
	    	
			detach();
	    }
}
