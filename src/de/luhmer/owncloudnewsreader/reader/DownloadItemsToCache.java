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

package de.luhmer.owncloudnewsreader.reader;

import android.content.Context;
import android.util.SparseArray;
import de.luhmer.owncloudnewsreader.async_tasks.GetImageAsyncTask;
import de.luhmer.owncloudnewsreader.helper.BitmapDrawableLruCache;
import de.luhmer.owncloudnewsreader.helper.ImageDownloadFinished;
import de.luhmer.owncloudnewsreader.helper.ImageHandler;

public class DownloadItemsToCache {
	SparseArray<String> URLs;
	Context context;
	
	public DownloadItemsToCache(Context context) {
		URLs = new SparseArray<String>();
		this.context = context;
	}
	
	public void StartDownloadOfImage(String URL_TO_IMAGE)
	{
		int key = 0;
		if(URLs.size() > 0)
			key = URLs.keyAt(URLs.size() -1) + 1;
		URLs.append(key, URL_TO_IMAGE);
		
		 GetImageAsyncTask getImageAsync = new GetImageAsyncTask(URL_TO_IMAGE, imgDownloadFinished, key, ImageHandler.getPathImageCache(context), context, null);
		 getImageAsync.execute((Void)null);
	}	
	
	ImageDownloadFinished imgDownloadFinished = new ImageDownloadFinished() {

		@Override
		public void DownloadFinished(int AsynkTaskId, String fileCachePath, BitmapDrawableLruCache lruCache) {
						
		}		
	};
}
