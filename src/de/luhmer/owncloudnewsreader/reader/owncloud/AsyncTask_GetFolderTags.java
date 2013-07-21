package de.luhmer.owncloudnewsreader.reader.owncloud;

import android.app.Activity;
import de.luhmer.owncloudnewsreader.reader.AsyncTask_Reader;
import de.luhmer.owncloudnewsreader.reader.OnAsyncTaskCompletedListener;

public class AsyncTask_GetFolderTags extends AsyncTask_Reader {

	private API api;
	
    public AsyncTask_GetFolderTags(final int task_id, final Activity context, final OnAsyncTaskCompletedListener[] listener, API api) {
    	super(task_id, context, listener);
    	this.api = api;
    }
		
	@Override
	protected Exception doInBackground(Object... params) {
		
        try {
		    //OwnCloudReaderMethods.GetFolderTags(context, api);
        	api.GetFolderTags(context, api);
        } catch(Exception ex) {
            return ex;
        }
        return null;
	}
}
