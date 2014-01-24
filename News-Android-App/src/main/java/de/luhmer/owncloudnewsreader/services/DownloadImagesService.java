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

package de.luhmer.owncloudnewsreader.services;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;

import de.luhmer.owncloudnewsreader.NewsReaderListActivity;
import de.luhmer.owncloudnewsreader.R;
import de.luhmer.owncloudnewsreader.SettingsActivity;
import de.luhmer.owncloudnewsreader.async_tasks.GetImageAsyncTask;
import de.luhmer.owncloudnewsreader.database.DatabaseConnection;
import de.luhmer.owncloudnewsreader.helper.BitmapDrawableLruCache;
import de.luhmer.owncloudnewsreader.helper.ImageDownloadFinished;
import de.luhmer.owncloudnewsreader.helper.ImageHandler;

public class DownloadImagesService extends IntentService {

	public static final String LAST_ITEM_ID = "LAST_ITEM_ID";
	private static Random random;
	
	private int NOTIFICATION_ID = 1;
	private NotificationManager notificationManager;
	private NotificationCompat.Builder NotificationDownloadImages;
	private int count;
	private int maxCount;
	//private int total_size = 0;
	
	public DownloadImagesService() {
		super(null);
		initService();
	}
	
	public DownloadImagesService(String name) {
		super(name);
		initService();
	}
	
	private void initService()
	{
		count = 0;
		maxCount = 0;
		if(random == null)
			random = new Random();
		NOTIFICATION_ID = random.nextInt();
	}

	@Override
	public void onDestroy() {
		if(NotificationDownloadImages != null)
		{
			if(maxCount == 0)
				notificationManager.cancel(NOTIFICATION_ID);
			/*
			else if(maxCount != count)
			{
				NotificationDownloadImages.setProgress(maxCount, count, false);
				NotificationDownloadImages.setContentText("Stopped downloading images. Application was closed.");
	        	notificationManager.notify(NOTIFICATION_ID, NotificationDownloadImages.build());
			}*/
		}
		super.onDestroy();
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		String lastId = String.valueOf(intent.getLongExtra(LAST_ITEM_ID, 0));
		DatabaseConnection dbConn = new DatabaseConnection(this);
		Cursor cursor = dbConn.getAllItemsWithIdHigher(lastId);
		List<String> links = new ArrayList<String>();
		try
		{
			if(cursor != null)
			{
				while(cursor.moveToNext())
				{
					String body = cursor.getString(cursor.getColumnIndex(DatabaseConnection.RSS_ITEM_BODY));
					links.addAll(ImageHandler.getImageLinksFromText(body));
				}
			}
		} catch(Exception ex) {
			ex.printStackTrace();
		} finally {
			cursor.close();
			dbConn.closeDatabase();
		}
		maxCount = links.size();
						
		Intent intentNewsReader = new Intent(this, NewsReaderListActivity.class);
		PendingIntent pIntent = PendingIntent.getActivity(this, 0, intentNewsReader, 0);
		notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
		NotificationDownloadImages = new NotificationCompat.Builder(this)
	        .setContentTitle("ownCloud News Reader")
	        .setContentText("Downloading Images for offline usage")
	        .setSmallIcon(R.drawable.ic_launcher)
	        .setContentIntent(pIntent);
   
		Notification notify = NotificationDownloadImages.build();
		
		//Hide the notification after its selected
		notify.flags |= Notification.FLAG_AUTO_CANCEL;
		//notify.flags |= Notification.FLAG_NO_CLEAR;
		
		if(maxCount > 0)
			notificationManager.notify(NOTIFICATION_ID, notify); 
		
		for(String link : links)
	    	new GetImageAsyncTask(link, imgDownloadFinished, 999, ImageHandler.getPathImageCache(this), this, null).execute();
	}

    private void RemoveOldImages(Context context) {
        HashMap<File, Long> files;
        long size = ImageHandler.getFolderSize(new File(ImageHandler.getPath(context)));
        size = (long) (size / 1024d / 1024d);

        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        int max_allowed_size = Integer.parseInt(mPrefs.getString(SettingsActivity.SP_MAX_CACHE_SIZE, "1000"));//Default is 1Gb --> 1000mb
        if(size > max_allowed_size)
        {
            files = new HashMap<File, Long>();
            for(File file : ImageHandler.getFilesFromDir(new File(ImageHandler.getPathImageCache(context))))
            {
                files.put(file, file.lastModified());
            }

            for(Object itemObj : sortHashMapByValuesD(files).entrySet())
            {
                File file = (File) itemObj;
                file.delete();
                size -= file.length();
                if(size < max_allowed_size)
                    break;
            }
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static LinkedHashMap sortHashMapByValuesD(HashMap passedMap) {
        List mapKeys = new ArrayList(passedMap.keySet());
        List mapValues = new ArrayList(passedMap.values());
        Collections.sort(mapValues);
        Collections.sort(mapKeys);

        LinkedHashMap sortedMap = new LinkedHashMap();

        Iterator valueIt = mapValues.iterator();
        while (valueIt.hasNext()) {
            Object val = valueIt.next();
            Iterator keyIt = mapKeys.iterator();

            while (keyIt.hasNext()) {
                Object key = keyIt.next();
                String comp1 = passedMap.get(key).toString();
                String comp2 = val.toString();

                if (comp1.equals(comp2)){
                    passedMap.remove(key);
                    mapKeys.remove(key);
                    sortedMap.put((String)key, (Double)val);
                    break;
                }
            }
        }
        return sortedMap;
    }
	
	ImageDownloadFinished imgDownloadFinished = new ImageDownloadFinished() {
		
		@Override
		public void DownloadFinished(int AsynkTaskId, String fileCachePath, BitmapDrawableLruCache lruCache) {

            if(fileCachePath != null) {
                File file = new File(fileCachePath);
                long size = file.length();
                //total_size += size;
                if(size == 0)
                    file.delete();
            }


			count++;
            // Sets the progress indicator to a max value, the
            // current completion percentage, and "determinate"
            // state
			NotificationDownloadImages.setProgress(maxCount, count, false);
			NotificationDownloadImages.setContentText("Downloading Images for offline usage - " + count + "/" + maxCount);
            // Displays the progress bar for the first time.
            notificationManager.notify(NOTIFICATION_ID, NotificationDownloadImages.build());
            
            if(maxCount == count) {
            	notificationManager.cancel(NOTIFICATION_ID);
                if(DownloadImagesService.this != null)
                    RemoveOldImages(DownloadImagesService.this);
            }
		}
	};
}
