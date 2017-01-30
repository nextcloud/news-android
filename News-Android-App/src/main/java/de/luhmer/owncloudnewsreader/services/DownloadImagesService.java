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
import android.graphics.Bitmap;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import de.greenrobot.dao.query.LazyList;
import de.luhmer.owncloudnewsreader.NewsReaderListActivity;
import de.luhmer.owncloudnewsreader.R;
import de.luhmer.owncloudnewsreader.async_tasks.GetImageThreaded;
import de.luhmer.owncloudnewsreader.database.DatabaseConnectionOrm;
import de.luhmer.owncloudnewsreader.database.model.Feed;
import de.luhmer.owncloudnewsreader.database.model.RssItem;
import de.luhmer.owncloudnewsreader.helper.FavIconHandler;
import de.luhmer.owncloudnewsreader.helper.ImageDownloadFinished;
import de.luhmer.owncloudnewsreader.helper.ImageHandler;

public class DownloadImagesService extends IntentService {

	public static final String LAST_ITEM_ID = "LAST_ITEM_ID";
    public enum DownloadMode { FAVICONS_ONLY, PICTURES_ONLY, FAVICONS_AND_PICTURES }
    public static final String DOWNLOAD_MODE_STRING = "DOWNLOAD_MODE";
	private static Random random;

	private int NOTIFICATION_ID = 1;
	private NotificationManager notificationManager;
	private NotificationCompat.Builder mNotificationDownloadImages;

	private int maxCount;
	//private int total_size = 0;

    List<String> linksToImages = new LinkedList<>();

	public DownloadImagesService() {
		super(null);
	}

	public DownloadImagesService(String name) {
		super(name);
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
    }

    @Override
	public void onDestroy() {
		if(mNotificationDownloadImages != null)
		{
			if(maxCount == 0)
				notificationManager.cancel(NOTIFICATION_ID);
		}
		super.onDestroy();
	}

	@Override
	protected void onHandleIntent(Intent intent) {
        DownloadMode downloadMode = (DownloadMode) intent.getSerializableExtra(DOWNLOAD_MODE_STRING);


        DatabaseConnectionOrm dbConn = new DatabaseConnectionOrm(this);
        Notification notify = BuildNotification();

        if(downloadMode.equals(DownloadMode.FAVICONS_ONLY)) {
            List<Feed> feedList = dbConn.getListOfFeeds();
            FavIconHandler favIconHandler = new FavIconHandler(this);
            for(Feed feed : feedList) {
                favIconHandler.PreCacheFavIcon(feed);
            }
        } else if(downloadMode.equals(DownloadMode.FAVICONS_AND_PICTURES) || downloadMode.equals(DownloadMode.PICTURES_ONLY)) {
            long lastId = intent.getLongExtra(LAST_ITEM_ID, 0);
            List<RssItem> rssItemList = dbConn.getAllItemsWithIdHigher(lastId);
            List<String> links = new ArrayList<>();
            for(RssItem rssItem : rssItemList) {
                String body = rssItem.getBody();
                links.addAll(ImageHandler.getImageLinksFromText(body));

                if(links.size() > 10000) {
                    notificationManager.notify(123, GetNotificationLimitImagesReached(10000));
                    break;
                }
            }
            ((LazyList)rssItemList).close();

            maxCount = links.size();

            if (maxCount > 0) {
                notificationManager.notify(NOTIFICATION_ID, notify);
            }

            linksToImages.addAll(links);

            StartNextDownloadInQueue();
        }
	}

    private synchronized void StartNextDownloadInQueue() {
        try {
            if(linksToImages.size() > 0) {
                String link = linksToImages.remove(0);
                new GetImageThreaded(link, imgDownloadFinished, 999).start();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            Toast.makeText(this, "Error while downloading images.", Toast.LENGTH_LONG).show();
        }
    }

    private Notification GetNotificationLimitImagesReached(int limit) {
        Intent intentNewsReader = new Intent(this, NewsReaderListActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intentNewsReader, 0);
        NotificationCompat.Builder notifyBuilder = new NotificationCompat.Builder(this)
                .setContentTitle("Nextcloud News")
                .setContentText("Only " + limit + " images can be cached at once")
                .setSmallIcon(R.drawable.ic_notification)
                .setContentIntent(pIntent);

        Notification notify = notifyBuilder.build();

        //Hide the notification after its selected
        notify.flags |= Notification.FLAG_AUTO_CANCEL;
        return notify;
    }

    private Notification BuildNotification() {
        Intent intentNewsReader = new Intent(this, NewsReaderListActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intentNewsReader, 0);
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationDownloadImages = new NotificationCompat.Builder(this)
                .setContentTitle(getResources().getString(R.string.app_name))
                .setContentText("Downloading images for offline usage")
                .setSmallIcon(R.drawable.ic_notification)
                .setContentIntent(pIntent)
                .setOngoing(true);

        Notification notify = mNotificationDownloadImages.build();

        //Hide the notification after its selected
        notify.flags |= Notification.FLAG_AUTO_CANCEL;
        notify.flags |= Notification.FLAG_NO_CLEAR;

        return notify;
    }

    private void RemoveOldImages() {
        ImageLoader.getInstance().clearDiskCache();
    }

	ImageDownloadFinished imgDownloadFinished = new ImageDownloadFinished() {

        @Override
        public void DownloadFinished(long AsynkTaskId, Bitmap bitmap) {
            int count = maxCount - linksToImages.size();

            if(maxCount == count) {
                notificationManager.cancel(NOTIFICATION_ID);
                //RemoveOldImages();
            } else {
                mNotificationDownloadImages
                        .setContentText("Downloading Images for offline usage - " + (count + 1) + "/" + maxCount)
                        .setProgress(maxCount, count + 1, false);

                notificationManager.notify(NOTIFICATION_ID, mNotificationDownloadImages.build());

                StartNextDownloadInQueue();
            }
        }
    };
}
