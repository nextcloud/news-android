package de.luhmer.owncloudnewsreader.reader.owncloud;

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import de.luhmer.owncloudnewsreader.SettingsActivity;
import de.luhmer.owncloudnewsreader.reader.AsyncTask_Reader;
import de.luhmer.owncloudnewsreader.reader.OnAsyncTaskCompletedListener;

public class AsyncTask_GetApiVersion extends AsyncTask_Reader {
	    
    private String username;
    private String password;
    
    public AsyncTask_GetApiVersion(final int task_id, final Activity context, String username, String password, final OnAsyncTaskCompletedListener[] listener) {
          super(task_id, context, listener);

          this.username = username;
          this.password = password;
    }
    
    @Override
    protected Object doInBackground(Object... params) {
        try {
        	SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
    		String oc_root_path = mPrefs.getString(SettingsActivity.EDT_OWNCLOUDROOTPATH_STRING, "");
        	return OwnCloudReaderMethods.GetVersionNumber(context, username, password, oc_root_path);
        } catch (Exception ex) {
            return ex;
        }
    }
}

