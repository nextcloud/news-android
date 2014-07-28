package de.luhmer.owncloudnewsreader.reader.owncloud.apiv2;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.List;

import de.luhmer.owncloudnewsreader.SettingsActivity;
import de.luhmer.owncloudnewsreader.database.DatabaseConnectionOrm;
import de.luhmer.owncloudnewsreader.database.model.Feed;
import de.luhmer.owncloudnewsreader.reader.AsyncTask_Reader;
import de.luhmer.owncloudnewsreader.reader.OnAsyncTaskCompletedListener;
import de.luhmer.owncloudnewsreader.reader.owncloud.OwnCloudConstants;

/**
 * Created by david on 13.01.14.
 */
public class AsyncTask_TriggerOcUpdate extends AsyncTask_Reader {

    private String username;
    private String password;

    public AsyncTask_TriggerOcUpdate(final int task_id, final Context context, String username, String password, final OnAsyncTaskCompletedListener[] listener) {
        super(task_id, context, listener);

        this.username = username;
        this.password = password;
    }

    @Override
    protected Object doInBackground(Object... params) {
        try {
            SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
            String oc_path = mPrefs.getString(SettingsActivity.EDT_OWNCLOUDROOTPATH_STRING, "") + OwnCloudConstants.ROOT_PATH_APIv2 + "feeds/update" + OwnCloudConstants.JSON_FORMAT;

            DatabaseConnectionOrm dbConn = new DatabaseConnectionOrm(context);
            List<Feed> feedList = dbConn.getListOfFeeds();


            /*
            if(cursor.getCount() > 0) {
                cursor.moveToFirst();

                do {
                    String feedId = cursor.getString(cursor.getColumnIndex(DatabaseConnection.SUBSCRIPTION_ID));

                    List<NameValuePair> nValuePairs = new ArrayList<NameValuePair>();
                    nValuePairs.add(new BasicNameValuePair("userId", username));
                    nValuePairs.add(new BasicNameValuePair("feedId", feedId));

                    HttpJsonRequest.PerformJsonRequest(oc_path, nValuePairs, username, password, context);
                } while(cursor.moveToNext());
            }
            */

            return true;
        } catch (Exception ex) {
            return ex;
        }
    }
}
