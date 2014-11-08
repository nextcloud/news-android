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
import de.luhmer.owncloudnewsreader.async_tasks.GetImageThreaded;
import de.luhmer.owncloudnewsreader.database.DatabaseConnectionOrm;
import de.luhmer.owncloudnewsreader.database.model.Feed;

public class FavIconHandler {
    private static final String TAG = "FavIconHandler";

    Context context;

    public FavIconHandler(Context context) {
        this.context = context;
    }

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

	public void PreCacheFavIcon(Feed feed) {
        GetImageThreaded giAsync = new GetImageThreaded(feed.getFaviconUrl(), favIconDownloadFinished, feed.getId(), FileUtils.getPathFavIcons(context), context);
        giAsync.scaleImage = true;
        giAsync.dstHeight = 2*32;
        giAsync.dstWidth = 2*32;

        giAsync.start();
    }

    ImageDownloadFinished favIconDownloadFinished = new ImageDownloadFinished() {

        @Override
        public void DownloadFinished(long AsynkTaskId, Bitmap bitmap) {
            if(bitmap != null) {
                DatabaseConnectionOrm dbConn = new DatabaseConnectionOrm(context);
                Feed feed = dbConn.getFeedById(AsynkTaskId);
                String avg = ColourCalculator.ColourHexFromBitmap(bitmap);
                feed.setAvgColour(avg);
                dbConn.updateFeed(feed);
            }
        }
    };
}
