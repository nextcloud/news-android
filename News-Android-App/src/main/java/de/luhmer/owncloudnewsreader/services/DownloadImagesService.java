/*
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

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;
import androidx.core.app.NotificationCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import de.greenrobot.dao.query.LazyList;
import de.luhmer.owncloudnewsreader.R;
import de.luhmer.owncloudnewsreader.async_tasks.DownloadImageHandler;
import de.luhmer.owncloudnewsreader.database.DatabaseConnectionOrm;
import de.luhmer.owncloudnewsreader.database.model.Feed;
import de.luhmer.owncloudnewsreader.database.model.RssItem;
import de.luhmer.owncloudnewsreader.helper.FavIconHandler;
import de.luhmer.owncloudnewsreader.helper.ImageHandler;
import de.luhmer.owncloudnewsreader.notification.NextcloudNotificationManager;

public class DownloadImagesService extends JobIntentService {

	public static final String LAST_ITEM_ID = "LAST_ITEM_ID";
    private static final String TAG = DownloadImagesService.class.getCanonicalName();

    public enum DownloadMode { FAVICONS_ONLY, PICTURES_ONLY, FAVICONS_AND_PICTURES }
    public static final String DOWNLOAD_MODE_STRING = "DOWNLOAD_MODE";
	private static Random random;

	private int NOTIFICATION_ID = 1923;
	private NotificationCompat.Builder mNotificationDownloadImages;

    private int maxCount;
    private NotificationManager mNotificationManager;


    /**
     * Unique job/channel ID for this service.
     */
    private static final int JOB_ID = 1000;
    private static final String CHANNEL_ID = "Download Images Service";


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

            mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
	public void onDestroy() {
        Log.d(TAG, "onDestroy");
		if(mNotificationDownloadImages != null)
		{
			if(maxCount == 0) {
                mNotificationManager.cancel(NOTIFICATION_ID);
            }
		}
		super.onDestroy();
	}

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        DownloadMode downloadMode = (DownloadMode) intent.getSerializableExtra(DOWNLOAD_MODE_STRING);

        DatabaseConnectionOrm dbConn = new DatabaseConnectionOrm(this);
        mNotificationDownloadImages = NextcloudNotificationManager.buildNotificationDownloadImageService(this, CHANNEL_ID);

        if(Objects.equals(downloadMode, DownloadMode.FAVICONS_ONLY)) {
            List<Feed> feedList = dbConn.getListOfFeeds();
            FavIconHandler favIconHandler = new FavIconHandler(getApplicationContext());
            for(Feed feed : feedList) {
                try {
                    favIconHandler.preCacheFavIcon(feed);
                } catch(IllegalStateException ex) {
                    Log.e(TAG, ex.getMessage());
                }
            }
        } else if(Objects.equals(downloadMode, DownloadMode.FAVICONS_AND_PICTURES) || Objects.equals(downloadMode, DownloadMode.PICTURES_ONLY)) {
            long lastId = intent.getLongExtra(LAST_ITEM_ID, 0);
            List<RssItem> rssItemList = dbConn.getAllItemsWithIdHigher(lastId);
            List<String> links = new ArrayList<>();
            for(RssItem rssItem : rssItemList) {
                String body = rssItem.getBody();
                links.addAll(ImageHandler.getImageLinksFromText(rssItem.getLink(), body));

                if(links.size() > 10000) {
                    NextcloudNotificationManager.showNotificationImageDownloadLimitReached(this, CHANNEL_ID, 10000);
                    break;
                }
            }
            ((LazyList)rssItemList).close();

            maxCount = links.size();

            if (maxCount > 0) {
                mNotificationManager.notify(NOTIFICATION_ID, mNotificationDownloadImages.build());
            }

            downloadImages(links);
        }
	}

    private void downloadImages(List<String> linksToImages) {
        try {
            RequestManager glide = Glide.with(this.getApplicationContext());

            while(linksToImages.size() > 0) {
                String link = linksToImages.remove(0);
                new DownloadImageHandler(link).preloadSync(glide);

                updateNotificationProgress(linksToImages.size());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.e(TAG, "Error while downloading images.");
            mNotificationDownloadImages
                    .setContentText("Error while downloading images - " + ex.toString())
                    .setProgress(0, 0, false);
            mNotificationManager.notify(NOTIFICATION_ID, mNotificationDownloadImages.build());
        }
    }

    private void updateNotificationProgress(int remainingImagesCount) {
        int count = maxCount - remainingImagesCount;
        if(maxCount == count) {
            mNotificationManager.cancel(NOTIFICATION_ID);
        } else {
            mNotificationDownloadImages
                    .setContentText((count + 1) + "/" + maxCount + " - " + getString(R.string.notification_download_images_offline))
                    .setProgress(maxCount, count + 1, false);

            mNotificationManager.notify(NOTIFICATION_ID, mNotificationDownloadImages.build());
        }
    }
}
