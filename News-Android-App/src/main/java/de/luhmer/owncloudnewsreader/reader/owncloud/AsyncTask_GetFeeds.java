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

package de.luhmer.owncloudnewsreader.reader.owncloud;

import android.content.Context;

import de.luhmer.owncloudnewsreader.reader.AsyncTask_Reader;
import de.luhmer.owncloudnewsreader.reader.OnAsyncTaskCompletedListener;

public class AsyncTask_GetFeeds extends AsyncTask_Reader {

	private API api;
	
    public AsyncTask_GetFeeds(final int task_id, final Context context, final OnAsyncTaskCompletedListener[] listener, API api) {
    	super(task_id, context, listener);
    	this.api = api;
    }

    @Override
    protected Exception doInBackground(Object... params) {
    	
        try {
        	api.GetFeeds(context, api);
        } catch (Exception ex) {
            return ex;
        }
        return null;
    }
}
