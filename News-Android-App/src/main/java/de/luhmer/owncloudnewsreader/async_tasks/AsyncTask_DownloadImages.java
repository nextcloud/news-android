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

import android.content.Context;
import android.os.AsyncTask;

import java.util.List;

import de.luhmer.owncloudnewsreader.helper.ImageHandler;

public class AsyncTask_DownloadImages extends AsyncTask<Void, Void, Void>{
	String text;
	Context context;
	
	public AsyncTask_DownloadImages(String text, Context context) {
		this.text = text;
		this.context = context;
	}
	
	@Override
	protected Void doInBackground(Void... params) {
		if(text != null)
		{
			List<String> links = ImageHandler.getImageLinksFromText(text);
		    
		    for(String link : links)
		    	new GetImageAsyncTask(link, null, 999, ImageHandler.getPathImageCache(context), context, null).execute();
		}
		return null;
	}
}
