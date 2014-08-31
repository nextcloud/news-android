/**
* Android ownCloud News
*
* @author David Luhmer
* @copyright 2013 David Luhmer david-dev@live.de
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU AFFERO GENERAL PUBLIC LICENSE
* License as published by the Free Software Foundation; either
* version 3 of the License, or any later version.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU AFFERO GENERAL PUBLIC LICENSE for more details.
*
* You should have received a copy of the GNU Affero General Public
* License along with this library.  If not, see <http://www.gnu.org/licenses/>.
*
*/

package de.luhmer.owncloudnewsreader.reader.owncloud;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import de.luhmer.owncloudnewsreader.Constants;
import de.luhmer.owncloudnewsreader.DownloadImagesActivity;
import de.luhmer.owncloudnewsreader.R;
import de.luhmer.owncloudnewsreader.SettingsActivity;
import de.luhmer.owncloudnewsreader.database.DatabaseConnectionOrm;
import de.luhmer.owncloudnewsreader.helper.NetworkConnection;
import de.luhmer.owncloudnewsreader.reader.AsyncTask_Reader;
import de.luhmer.owncloudnewsreader.reader.FeedItemTags.TAGS;
import de.luhmer.owncloudnewsreader.reader.OnAsyncTaskCompletedListener;
import de.luhmer.owncloudnewsreader.services.DownloadImagesService;

public class AsyncTask_GetItems extends AsyncTask_Reader {
    private long highestItemIdBeforeSync;
    private API api;
    int totalCount;

    public AsyncTask_GetItems(final int task_id, final Context context, final OnAsyncTaskCompletedListener[] listener, API api) {
    	super(task_id, context, listener);
    	this.api = api;

        totalCount = 0;
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        Toast.makeText(context, "Fetched " + totalCount + " items so far..", Toast.LENGTH_SHORT).show();

        super.onProgressUpdate(values);
    }

    @Override
	protected Exception doInBackground(Object... params) {
		DatabaseConnectionOrm dbConn = new DatabaseConnectionOrm(context);
        try {
            dbConn.clearDatabaseOverSize();

		    //String authKey = AuthenticationManager.getGoogleAuthKey(username, password);
        	//SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        	//int maxItemsInDatabase = Integer.parseInt(mPrefs.getString(SettingsActivity.SP_MAX_ITEMS_SYNC, "200"));

        	long lastModified = dbConn.getLastModified();
            //dbConn.clearDatabaseOverSize();

        	//List<RssFile> files;
        	long offset = dbConn.getLowestItemId(false);

        	int requestCount;
        	int maxSyncSize = Integer.parseInt(OwnCloudReaderMethods.maxSizePerSync);

        	highestItemIdBeforeSync = dbConn.getHighestItemId();


            SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(context);

        	if(lastModified == 0)//Only on first sync
        	{
                int maxItemsInDatabase = Constants.maxItemsCount;

                do {
	        		requestCount = api.GetItems(TAGS.ALL, context, String.valueOf(offset), false, 0, "3", api);
	        		if(requestCount > 0)
	        			offset = dbConn.getLowestItemId(false);
	        		totalCount += requestCount;

                    publishProgress((Void) null);
                } while(requestCount == maxSyncSize);

                mPrefs.edit().putInt(Constants.LAST_UPDATE_NEW_ITEMS_COUNT_STRING, totalCount).commit();

                do {
	        		offset = dbConn.getLowestItemId(true);
	        		requestCount = api.GetItems(TAGS.ALL_STARRED, context, String.valueOf(offset), true, 0, "2", api);
	        		//if(requestCount > 0)
	        		//	offset = dbConn.getLowestItemId(true);
	        		totalCount += requestCount;
	        	} while(requestCount == maxSyncSize && totalCount < maxItemsInDatabase);
        	}
        	else
        	{
                //First reset the count of last updated items
                mPrefs.edit().putInt(Constants.LAST_UPDATE_NEW_ITEMS_COUNT_STRING, 0).commit();
                //Get all updated items
                int[] result = api.GetUpdatedItems(TAGS.ALL, context, lastModified + 1, api);
                //If no exception occurs, set the number of updated items
                mPrefs.edit().putInt(Constants.LAST_UPDATE_NEW_ITEMS_COUNT_STRING, result[1]).commit();
        	}
        } catch (Exception ex) {
            ex.printStackTrace();
            return ex;
        } finally {
        	//dbConn.closeDatabase();
        }
        return null;
	}

    @Override
    protected void onPostExecute(Object ex) {
    	for (OnAsyncTaskCompletedListener listenerInstance : listener) {
    		if(listenerInstance != null)
    			listenerInstance.onAsyncTaskCompleted(task_id, ex);
		}

        if(ex == null && NetworkConnection.isNetworkAvailable(context)) {
            SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(context);

            int syncStrategy = Integer.parseInt(mPrefs.getString(SettingsActivity.LV_CACHE_IMAGES_OFFLINE_STRING, "0"));

            boolean downloadImages = false;

            switch(syncStrategy) {
                case 0:
                    break;
                case 1://Download via WiFi only
                    if (NetworkConnection.isWLANConnected(context))
                        downloadImages = true;
                    break;
                case 2: //Download via WiFi and Mobile
                    downloadImages = true;
                    break;
                case 3://Download via WiFi and ask for mobile
                    if (!NetworkConnection.isWLANConnected(context))
                        ShowDownloadImageWithoutWifiQuestion();
                    else
                        downloadImages = true;
                    break;
            }

            if(downloadImages) //If images should be cached
                StartDownloadingImages(context, highestItemIdBeforeSync, false);
            else //Download favIcons
                StartDownloadingImages(context, highestItemIdBeforeSync, true);
        }

		detach();
    }

    /**
     *
     * @param context
     * @param highestItemIdBeforeSync
     * @param favIconsExclusive indicates if only favIcons should be downloaded
     */
    public static void StartDownloadingImages(Context context, long highestItemIdBeforeSync, boolean favIconsExclusive)
    {
        Intent service = new Intent(context, DownloadImagesService.class);
        service.putExtra(DownloadImagesService.LAST_ITEM_ID, highestItemIdBeforeSync);

        if(favIconsExclusive)
            service.putExtra(DownloadImagesService.DOWNLOAD_FAVICONS_EXCLUSIVE, true);

        context.startService(service);
    }


    private void ShowDownloadImageWithoutWifiQuestion()
    {
        Bitmap bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher);

        Intent intent = new Intent(context, DownloadImagesActivity.class);
        intent.putExtra("highestItemIdBeforeSync", highestItemIdBeforeSync);
        PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent, 0);

        Notification notification = new NotificationCompat.Builder(context)
                .setContentTitle(context.getString(R.string.no_wifi_available))
                .setContentText(context.getString(R.string.do_you_want_to_download_without_wifi))
                .setSmallIcon(R.drawable.ic_launcher)
                .setLargeIcon(bm)
                .setContentIntent(pIntent)
                .setAutoCancel(true)
                .build();


        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        // hide the notification after its selected
        notification.flags |= Notification.FLAG_AUTO_CANCEL;

        notificationManager.notify(0, notification);
    }
}
