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

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import de.greenrobot.dao.query.LazyList;
import de.luhmer.owncloudnewsreader.NewsReaderListActivity;
import de.luhmer.owncloudnewsreader.R;
import de.luhmer.owncloudnewsreader.async_tasks.DownloadImageHandler;
import de.luhmer.owncloudnewsreader.database.DatabaseConnectionOrm;
import de.luhmer.owncloudnewsreader.database.model.Feed;
import de.luhmer.owncloudnewsreader.database.model.RssItem;
import de.luhmer.owncloudnewsreader.helper.FavIconHandler;
import de.luhmer.owncloudnewsreader.helper.ImageHandler;

public class DownloadImagesService extends JobIntentService {

	public static final String LAST_ITEM_ID = "LAST_ITEM_ID";
    private static final String TAG = DownloadImagesService.class.getCanonicalName();


    public enum DownloadMode { FAVICONS_ONLY, PICTURES_ONLY, FAVICONS_AND_PICTURES }
    public static final String DOWNLOAD_MODE_STRING = "DOWNLOAD_MODE";
	private static Random random;

	private int NOTIFICATION_ID = 1;
	private NotificationManager mNotificationManager;
	private NotificationCompat.Builder mNotificationDownloadImages;
    NotificationChannel mChannel;

    private int maxCount;
	//private int total_size = 0;

    List<String> linksToImages = new LinkedList<>();


    /**
     * Unique job/channel ID for this service.
     */
    static final int JOB_ID = 1000;
    static final String CHANNEL_ID = "1000";

    /**
     * Convenience method for enqueuing work in to this service.
     */
    public static void enqueueWork(Context context, Intent work) {
        enqueueWork(context, DownloadImagesService.class, JOB_ID, work);
    }



    @Override
    public void onCreate() {
        super.onCreate();
        try {
            maxCount = 0;
            if (random == null)
                random = new Random();
            NOTIFICATION_ID = random.nextInt();
        } catch (Exception ex) {
            ex.printStackTrace();
        }


        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_LOW;
            mChannel = new NotificationChannel(CHANNEL_ID, CHANNEL_ID, importance);
            //mChannel.enableLights(true);
            mNotificationManager.createNotificationChannel(mChannel);
        }
    }

    @Override
	public void onDestroy() {
        Log.d(TAG, "onDestroy");
		if(mNotificationDownloadImages != null)
		{
			if(maxCount == 0)
				mNotificationManager.cancel(NOTIFICATION_ID);
		}
		super.onDestroy();
	}

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        DownloadMode downloadMode = (DownloadMode) intent.getSerializableExtra(DOWNLOAD_MODE_STRING);

        DatabaseConnectionOrm dbConn = new DatabaseConnectionOrm(this);
        Notification notify = BuildNotification();

        if(downloadMode.equals(DownloadMode.FAVICONS_ONLY)) {
            List<Feed> feedList = dbConn.getListOfFeeds();
            FavIconHandler favIconHandler = new FavIconHandler(this);
            for(Feed feed : feedList) {
                try {
                    favIconHandler.PreCacheFavIcon(feed);
                } catch(IllegalStateException ex) {
                    Log.e(TAG, ex.getMessage());
                }
            }
        } else if(downloadMode.equals(DownloadMode.FAVICONS_AND_PICTURES) || downloadMode.equals(DownloadMode.PICTURES_ONLY)) {
            long lastId = intent.getLongExtra(LAST_ITEM_ID, 0);
            List<RssItem> rssItemList = dbConn.getAllItemsWithIdHigher(lastId);
            List<String> links = new ArrayList<>();
            for(RssItem rssItem : rssItemList) {
                String body = rssItem.getBody();
                links.addAll(ImageHandler.getImageLinksFromText(body));

                if(links.size() > 10000) {
                    mNotificationManager.notify(123, GetNotificationLimitImagesReached(10000));
                    break;
                }
            }
            ((LazyList)rssItemList).close();

            maxCount = links.size();

            if (maxCount > 0) {
                mNotificationManager.notify(NOTIFICATION_ID, notify);
            }

            linksToImages.addAll(links);

            downloadImages();
        }
	}

    private void downloadImages() {
        try {
            while(linksToImages.size() > 0) {
                String link = linksToImages.remove(0);
                new DownloadImageHandler(link).downloadSync();

                updateNotificationProgress();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            Toast.makeText(this, "Error while downloading images.", Toast.LENGTH_LONG).show();
        }
    }

    private void updateNotificationProgress() {
        int count = maxCount - linksToImages.size();
        if(maxCount == count) {
            mNotificationManager.cancel(NOTIFICATION_ID);
            //RemoveOldImages();
        } else {
            mNotificationDownloadImages
                    .setContentText("Downloading Images for offline usage - " + (count + 1) + "/" + maxCount)
                    .setProgress(maxCount, count + 1, false);

            mNotificationManager.notify(NOTIFICATION_ID, mNotificationDownloadImages.build());
        }
    }

    private Notification GetNotificationLimitImagesReached(int limit) {
        Intent intentNewsReader = new Intent(this, NewsReaderListActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intentNewsReader, 0);
        NotificationCompat.Builder notifyBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Nextcloud News")
                .setContentText("Only " + limit + " images can be cached at once")
                .setSmallIcon(R.drawable.ic_notification)
                .setContentIntent(pIntent);


        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notifyBuilder.setChannelId(CHANNEL_ID);
        }

        Notification notify = notifyBuilder.build();

        //Hide the notification after its selected
        notify.flags |= Notification.FLAG_AUTO_CANCEL;
        return notify;
    }

    private Notification BuildNotification() {
        Intent intentNewsReader = new Intent(this, NewsReaderListActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intentNewsReader, 0);
        mNotificationDownloadImages = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(getResources().getString(R.string.app_name))
                .setContentText("Downloading images for offline usage")
                .setSmallIcon(R.drawable.ic_notification)
                .setContentIntent(pIntent)
                .setOngoing(true);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            mNotificationDownloadImages.setChannelId(CHANNEL_ID);
        }

        Notification notify = mNotificationDownloadImages.build();

        //Hide the notification after its selected
        notify.flags |= Notification.FLAG_AUTO_CANCEL;
        notify.flags |= Notification.FLAG_NO_CLEAR;

        return notify;
    }

    private void RemoveOldImages() {
        ImageLoader.getInstance().clearDiskCache();
    }

}
