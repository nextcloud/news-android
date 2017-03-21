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
import android.os.AsyncTask;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import de.luhmer.owncloudnewsreader.helper.AsyncTaskHelper;
import de.luhmer.owncloudnewsreader.reader.AsyncTask_Reader;
import de.luhmer.owncloudnewsreader.reader.FeedItemTags;
import de.luhmer.owncloudnewsreader.reader.HttpJsonRequest;
import de.luhmer.owncloudnewsreader.reader.OnAsyncTaskCompletedListener;
import okhttp3.HttpUrl;

public class OwnCloud_Reader {

	@SuppressWarnings("unused")
	private static final String TAG = OwnCloud_Reader.class.getCanonicalName();

	private Future<API> apiFuture;
	private static OwnCloud_Reader instance;

	public static synchronized OwnCloud_Reader getInstance() {
		if(instance == null)
			instance = new OwnCloud_Reader();
		return instance;
	}

	private final Callable<API> apiCallable = new Callable<API>() {
		@Override
		public API call() throws Exception {
			HttpUrl oc_root_url = HttpJsonRequest.getInstance().getRootUrl();
			String version = OwnCloudReaderMethods.GetVersionNumber(oc_root_url);
			return API.GetRightApiForVersion(version, oc_root_url);
		}
	};

	private OwnCloud_Reader() {
	}
	
	public void Start_AsyncTask_GetItems(Context context, OnAsyncTaskCompletedListener listener, FeedItemTags tag) {
		Start_AsyncTask(new AsyncTask_GetItems(context, listener), tag);
	}

	public void Start_AsyncTask_GetOldItems(Context context, OnAsyncTaskCompletedListener listener, Long feed_id, Long folder_id) {
		Start_AsyncTask(new AsyncTask_GetOldItems(context, feed_id, folder_id, listener));
	}
	
	public void Start_AsyncTask_GetFolder(Context context, OnAsyncTaskCompletedListener listener) {
		Start_AsyncTask(new AsyncTask_GetFolderTags(context, listener));
	}
	
	public void Start_AsyncTask_GetFeeds(Context context, OnAsyncTaskCompletedListener listener) {
		Start_AsyncTask(new AsyncTask_GetFeeds(context, listener));
	}

	public void Start_AsyncTask_PerformItemStateChange(Context context, OnAsyncTaskCompletedListener listener) {
		Start_AsyncTask(new AsyncTask_PerformItemStateChange(context, listener));
	}

	@SafeVarargs
	private final <Params> void Start_AsyncTask(final AsyncTask_Reader asyncTask, final Params... params) {
		if (apiFuture == null) {
			apiFuture = ((ExecutorService) AsyncTask.THREAD_POOL_EXECUTOR).submit(apiCallable);
		}

		asyncTask.setAPIFuture(apiFuture);

		AsyncTaskHelper.StartAsyncTask(asyncTask, params);
	}

	public void resetApi() {
		apiFuture = null;
	}
}
