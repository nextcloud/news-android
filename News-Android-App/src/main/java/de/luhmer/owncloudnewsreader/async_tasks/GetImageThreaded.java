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

package de.luhmer.owncloudnewsreader.async_tasks;

import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.net.URL;

import de.luhmer.owncloudnewsreader.helper.ImageDownloadFinished;

public class GetImageThreaded implements ImageLoadingListener
{
	private static final String TAG = "GetImageAsyncTask";

	private URL WEB_URL_TO_FILE;
	private ImageDownloadFinished imageDownloadFinished;
	private long ThreadId;

	private static final DisplayImageOptions displayImageOptions = new DisplayImageOptions.Builder()
			.cacheOnDisk(true)
			.build();

	public GetImageThreaded(String WEB_URL_TO_FILE, ImageDownloadFinished imgDownloadFinished, long ThreadId) {
		try
		{
			this.WEB_URL_TO_FILE = new URL(WEB_URL_TO_FILE);
		}
		catch(Exception ex)
		{
            Log.d(TAG, "Invalid URL: " + WEB_URL_TO_FILE, ex);
		}

		imageDownloadFinished = imgDownloadFinished;
		this.ThreadId = ThreadId;
		//this.imageViewReference = new WeakReference<ImageView>(imageView);
	}

    public void start() {
        ImageLoader.getInstance().loadImage(WEB_URL_TO_FILE.toString(), displayImageOptions, this);
    }


	@Override
	public void onLoadingStarted(String imageUri, View view) {

	}

	@Override
	public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
		NotifyDownloadFinished(null);
		Log.d(TAG, "Failed to load file: " + imageUri);
	}

	@Override
	public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
		NotifyDownloadFinished(loadedImage);
	}

	@Override
	public void onLoadingCancelled(String imageUri, View view) {
		NotifyDownloadFinished(null);
		Log.d(TAG, "Cancelled: " + imageUri);
	}

	private void NotifyDownloadFinished(Bitmap bitmap) {
		if(imageDownloadFinished != null)
			imageDownloadFinished.DownloadFinished(ThreadId, bitmap);
	}
}
