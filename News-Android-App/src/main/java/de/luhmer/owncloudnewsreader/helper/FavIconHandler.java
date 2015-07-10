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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.graphics.Palette;
import android.util.Log;
import android.util.SparseArray;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.lang.ref.WeakReference;

import de.luhmer.owncloudnewsreader.R;
import de.luhmer.owncloudnewsreader.async_tasks.GetImageThreaded;
import de.luhmer.owncloudnewsreader.database.DatabaseConnectionOrm;
import de.luhmer.owncloudnewsreader.database.model.Feed;

public class FavIconHandler {
    private static final String TAG = "FavIconHandler";

    private Context context;
    private final String favIconPath;

    public FavIconHandler(Context context) {
        this.context = context;
        favIconPath = FileUtils.getPathFavIcons(context);
    }

    public void loadFavIconForFeed(String favIconUrl, ImageView imgView) {
        File cacheFile = ImageHandler.getFullPathOfCacheFileSafe(favIconUrl, favIconPath);
        if(cacheFile != null && cacheFile.exists()) {
            Picasso.with(context)
                    .load(cacheFile)
                    .placeholder(FavIconHandler.getResourceIdForRightDefaultFeedIcon(context))
                    .into(imgView, null);
        } else {
            Picasso.with(context)
                    .load(favIconUrl)
                    .placeholder(FavIconHandler.getResourceIdForRightDefaultFeedIcon(context))
                    .into(imgView, null);
        }
    }

    public static int getResourceIdForRightDefaultFeedIcon(Context context)
	{
		if(ThemeChooser.isDarkTheme(context))
			return R.drawable.default_feed_icon_light;
		else
			return R.drawable.default_feed_icon_dark;

	}

	public void PreCacheFavIcon(Feed feed) {
        if(feed.getFaviconUrl() == null) {
            Log.v(TAG, "No favicon for "+feed.getFeedTitle());
            return;
        }

        GetImageThreaded giAsync = new GetImageThreaded(feed.getFaviconUrl(), favIconDownloadFinished, feed.getId(), favIconPath, context);
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
                Palette palette = Palette.from(bitmap).generate();
                String avg = String.valueOf(
                        palette.getVibrantColor(R.color.material_blue_grey_800)
                );
                feed.setAvgColour(avg);
                dbConn.updateFeed(feed);

                Log.v(TAG, "Updating AVG color of feed: " + feed.getFeedTitle() + " - Color: " + avg);
            } else {
                Log.v(TAG, "Failed to update AVG color of feed: " + AsynkTaskId);
            }
        }
    };
}
