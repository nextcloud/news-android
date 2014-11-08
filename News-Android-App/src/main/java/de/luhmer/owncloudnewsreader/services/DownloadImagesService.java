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
import android.graphics.Bitmap;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.SparseArray;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import de.luhmer.owncloudnewsreader.NewsReaderListActivity;
import de.luhmer.owncloudnewsreader.R;
import de.luhmer.owncloudnewsreader.SettingsActivity;
import de.luhmer.owncloudnewsreader.async_tasks.GetImageThreaded;
import de.luhmer.owncloudnewsreader.database.DatabaseConnectionOrm;
import de.luhmer.owncloudnewsreader.database.model.Feed;
import de.luhmer.owncloudnewsreader.database.model.RssItem;
import de.luhmer.owncloudnewsreader.helper.BitmapDrawableLruCache;
import de.luhmer.owncloudnewsreader.helper.FavIconHandler;
import de.luhmer.owncloudnewsreader.helper.FileUtils;
import de.luhmer.owncloudnewsreader.helper.ImageDownloadFinished;
import de.luhmer.owncloudnewsreader.helper.ImageHandler;

public class DownloadImagesService extends IntentService {

	public static final String LAST_ITEM_ID = "LAST_ITEM_ID";
    public static final String DOWNLOAD_FAVICONS_EXCLUSIVE = "DOWNLOAD_FAVICONS_EXCLUSIVE";
	private static Random random;

	private int NOTIFICATION_ID = 1;
	private NotificationManager notificationManager;
	private NotificationCompat.Builder NotificationDownloadImages;

	private int maxCount;
	//private int total_size = 0;

    private String pathToImageCache;
    List<String> linksToImages = new LinkedList<String>();

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
        pathToImageCache = FileUtils.getPathImageCache(this);

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
		}
		super.onDestroy();
	}

	@Override
	protected void onHandleIntent(Intent intent) {
        boolean downloadFavIconsExclusive = intent.getBooleanExtra(DOWNLOAD_FAVICONS_EXCLUSIVE, false);

        DatabaseConnectionOrm dbConn = new DatabaseConnectionOrm(this);
        Notification notify = BuildNotification();

        //if(linksFavIcons.size() > 0)
            //notificationManager.notify(NOTIFICATION_ID, notify);

        List<Feed> feedList = dbConn.getListOfFeeds();
        FavIconHandler favIconHandler = new FavIconHandler(this);
        for(Feed feed : feedList) {
            favIconHandler.PreCacheFavIcon(feed);
        }


        if(!downloadFavIconsExclusive) {
            long lastId = intent.getLongExtra(LAST_ITEM_ID, 0);
            List<RssItem> rssItemList = dbConn.getAllItemsWithIdHigher(lastId);
            List<String> links = new ArrayList<String>();
            for(RssItem rssItem : rssItemList) {
                String body = rssItem.getBody();
                links.addAll(ImageHandler.getImageLinksFromText(body));
            }

            maxCount = links.size();

            if (maxCount > 0)
                notificationManager.notify(NOTIFICATION_ID, notify);

            linksToImages.addAll(links);

            StartNextDownloadInQueue();
        }
	}

    private synchronized void StartNextDownloadInQueue() {
        try {
            if(linksToImages.size() > 0)
                new GetImageThreaded(linksToImages.get(0), imgDownloadFinished, 999, pathToImageCache, this).start();
            linksToImages.remove(0);
        } catch (Exception ex) {
            ex.printStackTrace();
            Toast.makeText(this, "Error while downloading images.", Toast.LENGTH_LONG).show();
        }
    }

    private Notification BuildNotification() {
        Intent intentNewsReader = new Intent(this, NewsReaderListActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intentNewsReader, 0);
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationDownloadImages = new NotificationCompat.Builder(this)
                .setContentTitle("ownCloud News Reader")
                .setContentText("Downloading Images for offline usage")
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentIntent(pIntent);

        Notification notify = NotificationDownloadImages.build();

        //Hide the notification after its selected
        notify.flags |= Notification.FLAG_AUTO_CANCEL;
        //notify.flags |= Notification.FLAG_NO_CLEAR;

        return notify;
    }

    private void RemoveOldImages(Context context) {
        HashMap<File, Long> files;
        long size = ImageHandler.getFolderSize(new File(FileUtils.getPath(context)));
        size = (long) (size / 1024d / 1024d);

        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        int max_allowed_size = Integer.parseInt(mPrefs.getString(SettingsActivity.SP_MAX_CACHE_SIZE, "1000"));//Default is 1Gb --> 1000mb
        if(size > max_allowed_size)
        {
            files = new HashMap<File, Long>();
            for(File file : ImageHandler.getFilesFromDir(new File(FileUtils.getPathImageCache(context))))
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
                    sortedMap.put(key, val);
                    break;
                }
            }
        }
        return sortedMap;
    }

	ImageDownloadFinished imgDownloadFinished = new ImageDownloadFinished() {

		@Override
		public void DownloadFinished(long ThreadId, Bitmap bitmap) {

            int count = maxCount - linksToImages.size();


            if(maxCount == count) {
            	notificationManager.cancel(NOTIFICATION_ID);
                if(DownloadImagesService.this != null)
                    RemoveOldImages(DownloadImagesService.this);
            } else {
                NotificationDownloadImages.setProgress(maxCount, count+1, false);
                NotificationDownloadImages.setContentText("Downloading Images for offline usage - " + (count+1) + "/" + maxCount);
                notificationManager.notify(NOTIFICATION_ID, NotificationDownloadImages.build());

                StartNextDownloadInQueue();
            }
		}
	};
}
