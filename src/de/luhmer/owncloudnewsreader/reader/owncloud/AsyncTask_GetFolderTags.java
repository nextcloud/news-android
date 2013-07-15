package de.luhmer.owncloudnewsreader.reader.owncloud;

import android.app.Activity;
import de.luhmer.owncloudnewsreader.reader.AsyncTask_Reader;
import de.luhmer.owncloudnewsreader.reader.OnAsyncTaskCompletedListener;
import de.luhmer.owncloudnewsreader.reader.owncloud.apiv2.APIv2;

public class AsyncTask_GetFolderTags extends AsyncTask_Reader {

    public AsyncTask_GetFolderTags(final int task_id, final Activity context, final OnAsyncTaskCompletedListener[] listener) {
    	super(task_id, context, listener);
    }
		
	@Override
	protected Exception doInBackground(Object... params) {
		API api = new APIv2(context);
		
        try {
		    //OwnCloudReaderMethods.GetFolderTags(context, api);
        	api.GetFolderTags(context, api);
        } catch(Exception ex) {
            return ex;
        }
        return null;
	}
}
