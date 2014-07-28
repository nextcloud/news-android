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
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.support.v4.util.LruCache;



public class BitmapDrawableLruCache extends LruCache<Long, BitmapDrawable> {
    public static int getDefaultLruCacheSize() {
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = maxMemory / 8;
        return cacheSize;
    }

    public BitmapDrawableLruCache() {
        this(getDefaultLruCacheSize());
    }

    public BitmapDrawableLruCache(int sizeInKiloBytes) {
        super(sizeInKiloBytes);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
	@Override
    protected int sizeOf(Long key, BitmapDrawable bitmap) {
		if(Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB_MR1)
			return bitmap.getBitmap().getByteCount() / 1024;
		else
			return bitmap.getBitmap().getRowBytes() * bitmap.getBitmap().getHeight() / 1024;
    }
}
