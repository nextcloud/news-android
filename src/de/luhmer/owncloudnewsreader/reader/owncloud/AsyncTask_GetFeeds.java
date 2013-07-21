package de.luhmer.owncloudnewsreader.reader.owncloud;

import android.content.Context;
import de.luhmer.owncloudnewsreader.reader.AsyncTask_Reader;
import de.luhmer.owncloudnewsreader.reader.OnAsyncTaskCompletedListener;

public class AsyncTask_GetFeeds extends AsyncTask_Reader {

	private API api;
	
    public AsyncTask_GetFeeds(final int task_id, final Context context, final OnAsyncTaskCompletedListener[] listener, API api) {
    	super(task_id, context, listener);
    	this.api = api;
    }

    @Override
    protected Exception doInBackground(Object... params) {
    	
        try {
        	api.GetFeeds(context, api);
        } catch (Exception ex) {
            return ex;
        }
        return null;
    }
}
