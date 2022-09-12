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

package de.luhmer.owncloudnewsreader.async_tasks;

import android.graphics.Bitmap;
import android.util.Log;

import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.net.URL;
import java.util.concurrent.ExecutionException;

import de.luhmer.owncloudnewsreader.helper.ImageDownloadFinished;

public class DownloadImageHandler {
	private static final String TAG = DownloadImageHandler.class.getCanonicalName();

	private URL mImageUrl;
	private ImageDownloadFinished imageDownloadFinished;

	public DownloadImageHandler(String imageUrl) {
		try {
			this.mImageUrl = new URL(imageUrl);
		} catch(Exception ex) {
            Log.d(TAG, "Invalid URL: " + imageUrl, ex);
		}
	}

	public void preloadSync(RequestManager glide) {
		try {
			Bitmap bm = glide
					.asBitmap()
					.load(mImageUrl.toString())
					.diskCacheStrategy(DiskCacheStrategy.DATA)
					.submit()
					.get();
			NotifyDownloadFinished(bm);
		} catch (ExecutionException | InterruptedException e) {
			e.printStackTrace();
		}
		NotifyDownloadFinished(null);
	}

	private void NotifyDownloadFinished(Bitmap bitmap) {
		if(imageDownloadFinished != null) {
            imageDownloadFinished.DownloadFinished(bitmap);
        }
	}
}
