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

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;

import java.util.concurrent.Future;

import de.luhmer.owncloudnewsreader.reader.owncloud.API;

public abstract class AsyncTask_Reader extends AsyncTask<Object, Void, Exception> {
	protected Context context;
	protected OnAsyncTaskCompletedListener[] listener;
    protected Future<API> apiFuture;
	
	public AsyncTask_Reader(final Context context, final OnAsyncTaskCompletedListener... listener) {
		this.context = context;
		this.listener = listener;
	}
	
	//public abstract void attach(final Activity context, final OnAsyncTaskCompletedListener[] listener);
	
	//Activity meldet sich zurueck nach OrientationChange
    public void attach(final Activity context, final OnAsyncTaskCompletedListener[] listener) {
        this.context = context;
        this.listener = listener;
    }

    //Activity meldet sich ab
    public void detach() {
        if (context != null) {
            context = null;
        }

        if (listener != null) {
            listener = null;
        }
    }
       
    @Override
    protected void onPostExecute(Exception ex) {
        for (OnAsyncTaskCompletedListener listenerInstance : listener) {
            if(listenerInstance != null)
                listenerInstance.onAsyncTaskCompleted(ex);
        }

        detach();
    }

    public void setAPIFuture(Future<API> apiFuture) {
        this.apiFuture = apiFuture;
    }
}
