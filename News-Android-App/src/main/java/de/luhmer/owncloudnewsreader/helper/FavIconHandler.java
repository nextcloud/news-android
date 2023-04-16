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

package de.luhmer.owncloudnewsreader.helper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.palette.graphics.Palette;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;

import de.luhmer.owncloudnewsreader.R;
import de.luhmer.owncloudnewsreader.database.DatabaseConnectionOrm;
import de.luhmer.owncloudnewsreader.database.model.Feed;

public class FavIconHandler {
    private static final String TAG = FavIconHandler.class.getCanonicalName();

    private final RequestManager mGlide;
    private final Context mContext;
    private final int mPlaceHolder;

    public FavIconHandler(Context context) {
        mPlaceHolder = FavIconHandler.getResourceIdForRightDefaultFeedIcon();
        mContext = context;
        mGlide = Glide.with(context);
    }

    public <T extends Drawable> void loadFavIconForFeed(@Nullable String favIconUrl, ImageView imgView) {
        RequestOptions requestOptions = new RequestOptions();
        requestOptions = requestOptions.transforms(new RoundedCorners(6));

        if (favIconUrl == null) {
            mGlide
                    .load(mPlaceHolder)
                    .apply(requestOptions)
                    .into(imgView);
        } else {
            mGlide
                    .load(favIconUrl)
                    .diskCacheStrategy(DiskCacheStrategy.DATA)
                    .placeholder(mPlaceHolder)
                    .error(mPlaceHolder)
                    .apply(requestOptions)
                    .onlyRetrieveFromCache(true) // disable loading of favicons from network (usually those favicons are broken)
                    .into(imgView);
        }
    }

    boolean isSVG(String url) {
        return url.contains("svg");
    }

    /**
     * Version of loadFacIconForFeed that applies a vertical offset to the icon ImageView,
     * to compensate for font size scaling alignment issue
     *
     * @param favIconUrl URL of icon to load/display
     * @param imgView    ImageView object to use for icon display
     * @param offset     Y translation to apply to ImageView
     */
    public void loadFavIconForFeed(String favIconUrl, ImageView imgView, int offset) {
        loadFavIconForFeed(favIconUrl, imgView);
        imgView.setTranslationY(offset);
    }

    public static int getResourceIdForRightDefaultFeedIcon() {
        if (ThemeChooser.getSelectedTheme().equals(ThemeChooser.THEME.LIGHT)) {
            return R.drawable.default_feed_icon_dark;
        } else {
            return R.drawable.default_feed_icon_light;
        }
    }

    public void preCacheFavIcon(final Feed feed) throws IllegalStateException {
        if (feed.getFaviconUrl() == null) {
            Log.v(TAG, "No favicon for " + feed.getFeedTitle());
            return;
        }

        String favIconUrl = feed.getFaviconUrl();

        // pre caching doesn't work for SVG icons
        if (isSVG(favIconUrl)) {
            return;
        }

        // Log.v(TAG, "Pre caching favicon: " + favIconUrl);

        mGlide
                .asBitmap()
                .load(favIconUrl)
                .diskCacheStrategy(DiskCacheStrategy.DATA)
                .apply(RequestOptions.overrideOf(Target.SIZE_ORIGINAL))
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        UpdateAvgColorOfFeed(feed.getId(), resource, mContext);
                        Log.d(TAG, "Successfully downloaded image for url: " + favIconUrl);
            }

            @Override
            public void onLoadFailed(@Nullable Drawable errorDrawable) {
                super.onLoadFailed(errorDrawable);
                Log.d(TAG, "Failed to download image for url: " + favIconUrl);
            }
        });
    }

    private void UpdateAvgColorOfFeed(long feedId, Bitmap bitmap, Context context) {
        if (bitmap != null) {
            DatabaseConnectionOrm dbConn = new DatabaseConnectionOrm(context);
            Feed feed = dbConn.getFeedById(feedId);
            Palette palette = Palette.from(bitmap).generate();
            String avg = String.valueOf(
                    palette.getVibrantColor(ContextCompat.getColor(context, androidx.appcompat.R.color.material_blue_grey_800))
            );
            feed.setAvgColour(avg);
            dbConn.updateFeed(feed);

            // Log.v(TAG, "Updating AVG color of feed: " + feed.getFeedTitle() + " - Color: " + avg);
        } else {
            Log.v(TAG, "Failed to update AVG color of feed: " + feedId);
        }
    }
}
