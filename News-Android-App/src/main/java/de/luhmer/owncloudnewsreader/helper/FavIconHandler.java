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
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.core.content.ContextCompat;
import androidx.palette.graphics.Palette;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import de.luhmer.owncloudnewsreader.R;
import de.luhmer.owncloudnewsreader.database.DatabaseConnectionOrm;
import de.luhmer.owncloudnewsreader.database.model.Feed;

public class FavIconHandler {
    private static final String TAG = FavIconHandler.class.getCanonicalName();
    private final DisplayImageOptions displayImageOptions;

    public FavIconHandler(Context context) {
        int placeHolder = FavIconHandler.getResourceIdForRightDefaultFeedIcon();
        displayImageOptions = new DisplayImageOptions.Builder()
                .showImageOnLoading(placeHolder)
                .showImageForEmptyUri(placeHolder)
                .showImageOnFail(placeHolder)
                .cacheOnDisk(true)
                .cacheInMemory(true)
                .build();
    }

    public void loadFavIconForFeed(String favIconUrl, ImageView imgView) {
        ImageLoader.getInstance().displayImage(favIconUrl, imgView, displayImageOptions);
    }

    /**
     * Version of loadFacIconForFeed that applies a vertical offset to the icon ImageView,
     * to compensate for font size scaling alignment issue
     *
     * @param favIconUrl    URL of icon to load/display
     * @param imgView       ImageView object to use for icon display
     * @param offset        Y translation to apply to ImageView
     */
    public void loadFavIconForFeed(String favIconUrl, ImageView imgView, int offset) {
        loadFavIconForFeed(favIconUrl, imgView);
        imgView.setTranslationY(offset);
    }

    private static int getResourceIdForRightDefaultFeedIcon() {
        if (ThemeChooser.getSelectedTheme().equals(ThemeChooser.THEME.LIGHT)) {
            return R.drawable.default_feed_icon_dark;
        } else {
            return R.drawable.default_feed_icon_light;
        }

    }

    public void preCacheFavIcon(final Feed feed, Context context) throws IllegalStateException {
        if (feed.getFaviconUrl() == null) {
            Log.v(TAG, "No favicon for " + feed.getFeedTitle());
            return;
        }

        Log.v(TAG, "Loading image: " + feed.getFaviconUrl());
        ImageLoader.getInstance().loadImage(feed.getFaviconUrl(), displayImageOptions, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {

            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {

            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                DownloadFinished(feed.getId(), loadedImage, context);
            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {

            }
        });
    }

    private void DownloadFinished(long feedId, Bitmap bitmap, Context context) {
        if (bitmap != null) {
            DatabaseConnectionOrm dbConn = new DatabaseConnectionOrm(context);
            Feed feed = dbConn.getFeedById(feedId);
            Palette palette = Palette.from(bitmap).generate();
            String avg = String.valueOf(
                    palette.getVibrantColor(ContextCompat.getColor(context, R.color.material_blue_grey_800))
            );
            feed.setAvgColour(avg);
            dbConn.updateFeed(feed);

            Log.v(TAG, "Updating AVG color of feed: " + feed.getFeedTitle() + " - Color: " + avg);
        } else {
            Log.v(TAG, "Failed to update AVG color of feed: " + feedId);
        }
    }
}
