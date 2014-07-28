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

package de.luhmer.owncloudnewsreader.helper;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.util.SparseArray;
import android.widget.ImageView;

import java.io.File;
import java.lang.ref.WeakReference;

import de.luhmer.owncloudnewsreader.R;
import de.luhmer.owncloudnewsreader.async_tasks.GetImageAsyncTask;
import de.luhmer.owncloudnewsreader.database.DatabaseConnectionOrm;
import de.luhmer.owncloudnewsreader.database.model.Feed;

public class FavIconHandler {
    private static final String TAG = "FavIconHandler";

    public static Drawable GetFavIconFromCache(String URL_TO_PAGE, Context context, Long feedID)
	{
		try
		{
			File favIconFile = ImageHandler.getFullPathOfCacheFile(URL_TO_PAGE, FileUtils.getPathFavIcons(context));
			if(favIconFile.isFile() && favIconFile.length() > 0)
			{
				if(feedID != null) {
                	DatabaseConnectionOrm dbConn = new DatabaseConnectionOrm(context);
                    Feed feed = dbConn.getFeedById(feedID);
                    Bitmap bitmap = BitmapFactory.decodeFile(favIconFile.getAbsolutePath());
                    String avg = ColourCalculator.ColourHexFromBitmap(bitmap);

                    feed.setAvgColour(avg);
                    dbConn.updateFeed(feed);
                    //dbConn.setAvgColourOfFeedByDbId(feedID, avg);
                }

				return Drawable.createFromPath(favIconFile.getPath());
			}
		}
		catch(Exception ex)
		{
            //Log.d(TAG, ex.getMessage());
			ex.printStackTrace();
		}
		return null;
	}

    public static int getResourceIdForRightDefaultFeedIcon(Context context)
	{
		if(ThemeChooser.isDarkTheme(context))
			return R.drawable.default_feed_icon_light;
		else
			return R.drawable.default_feed_icon_dark;

	}

	static SparseArray<FavIconCache> imageViewReferences = new SparseArray<FavIconCache>();
	Long feedID;

    static SparseArray<FavIconCache> favIconToFeedId = new SparseArray<FavIconCache>();
    public static void PreCacheFavIcon(String WEB_URL_TO_FILE, Context context, Long feedID) {

        FavIconCache favIconCache = new FavIconCache();
        favIconCache.context = context;
        favIconCache.WEB_URL_TO_FILE = WEB_URL_TO_FILE;

        int key = feedID.intValue();
        favIconToFeedId.put(key, favIconCache);
        GetImageAsyncTask giAsync = new GetImageAsyncTask(WEB_URL_TO_FILE, favIconDownloadFinished, key, FileUtils.getPathFavIcons(context), context, null);
        giAsync.scaleImage = true;
        giAsync.dstHeight = 2*32;
        giAsync.dstWidth = 2*32;
        giAsync.feedID = feedID;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            // Execute in parallel
            giAsync.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, ((Void)null));
        else
            giAsync.execute((Void)null);
    }

    static ImageDownloadFinished favIconDownloadFinished = new ImageDownloadFinished() {

        @Override
        public void DownloadFinished(int AsynkTaskId, String fileCachePath, BitmapDrawableLruCache lruCache) {
            FavIconCache favIconCache = favIconToFeedId.get(AsynkTaskId);
            FavIconHandler.GetFavIconFromCache(favIconCache.WEB_URL_TO_FILE, favIconCache.context, (long) AsynkTaskId);
            imageViewReferences.remove(AsynkTaskId);
        }
    };


	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public void GetImageAsync(ImageView imageView, String WEB_URL_TO_FILE, Context context, Long feedID, BitmapDrawableLruCache lruCache)
	{
		this.feedID = feedID;

		boolean setImageAlready = false;
		if(lruCache != null) {
			if(lruCache.get(feedID) != null) {
				if (imageView != null) {
	                imageView.setImageDrawable(lruCache.get(feedID));
	                setImageAlready = true;
	            }
			}
		}
		if(!setImageAlready) {
			WeakReference<ImageView> imageViewReference = new WeakReference<ImageView>(imageView);
			FavIconCache favIconCache = new FavIconCache();
			favIconCache.context = context;
			favIconCache.WEB_URL_TO_FILE = WEB_URL_TO_FILE;
			favIconCache.imageViewReference = imageViewReference;

			int key = 0;
			if(imageViewReferences.size() > 0)
				key = imageViewReferences.keyAt(imageViewReferences.size() -1) + 1;
			imageViewReferences.append(key, favIconCache);


			imageView.setImageDrawable(null);
			GetImageAsyncTask giAsync = new GetImageAsyncTask(WEB_URL_TO_FILE, imgDownloadFinished, key, FileUtils.getPathFavIcons(context), context/*, imageView*/, lruCache);
			giAsync.scaleImage = true;
			giAsync.dstHeight = 2*32;
			giAsync.dstWidth = 2*32;
			giAsync.feedID = feedID;

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
				giAsync.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, ((Void)null));
			else
				giAsync.execute((Void)null);
		}
	}

	ImageDownloadFinished imgDownloadFinished = new ImageDownloadFinished() {

		@Override
		public void DownloadFinished(int AsynkTaskId, String fileCachePath, BitmapDrawableLruCache lruCache) {
			//WeakReference<ImageView> imageViewRef = imageViewReferences.get(AsynkTaskId);
			FavIconCache favIconCache = imageViewReferences.get(AsynkTaskId);
			WeakReference<ImageView> imageViewRef = favIconCache.imageViewReference;

			if(imageViewRef != null)
			{
	            ImageView imageView = imageViewRef.get();
	            if (imageView != null) {
	            	BitmapDrawable bd = (BitmapDrawable) FavIconHandler.GetFavIconFromCache(favIconCache.WEB_URL_TO_FILE, favIconCache.context, feedID);
	            	if(lruCache != null && feedID != null && bd != null)
	            		lruCache.put(feedID, bd);
	                imageView.setImageDrawable(bd);
	            }
			}

			imageViewReferences.remove(AsynkTaskId);
		}
	};

	static class FavIconCache
	{
		public WeakReference<ImageView> imageViewReference;
		public String WEB_URL_TO_FILE;
		public Context context;
	}
}
