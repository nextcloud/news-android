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

import com.squareup.okhttp.HttpUrl;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import de.luhmer.owncloudnewsreader.reader.AsyncTask_Reader;
import de.luhmer.owncloudnewsreader.reader.FeedItemTags;
import de.luhmer.owncloudnewsreader.reader.HttpJsonRequest;
import de.luhmer.owncloudnewsreader.reader.OnAsyncTaskCompletedListener;

public class OwnCloud_Reader {
	private static OwnCloud_Reader instance;

	public static void init(Context context) {
		if(instance != null)
			throw new IllegalStateException("Already initialized");
		instance = new OwnCloud_Reader();
	}

	public static OwnCloud_Reader getInstance() {
		if(instance == null)
			throw new IllegalStateException("Must be initialized first");
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

	private final ExecutorService executor = (ExecutorService) AsyncTask.THREAD_POOL_EXECUTOR;

	private boolean isSyncRunning = false;
	private Future<API> apiFuture;

	private OwnCloud_Reader() {
	}
	
	public void Start_AsyncTask_GetItems(Context context, OnAsyncTaskCompletedListener listener, FeedItemTags tag) {
		Start_AsyncTask(new AsyncTask_GetItems(context, AsyncTask_finished, listener), tag);
	}

	public void Start_AsyncTask_GetOldItems(Context context, OnAsyncTaskCompletedListener listener, Long feed_id, Long folder_id) {
		Start_AsyncTask(new AsyncTask_GetOldItems(context, feed_id, folder_id, AsyncTask_finished, listener));
	}
	
	public void Start_AsyncTask_GetFolder(Context context, OnAsyncTaskCompletedListener listener) {
		Start_AsyncTask(new AsyncTask_GetFolderTags(context, AsyncTask_finished, listener));
	}
	
	public void Start_AsyncTask_GetFeeds(Context context, OnAsyncTaskCompletedListener listener) {
		Start_AsyncTask(new AsyncTask_GetFeeds(context, AsyncTask_finished, listener));
	}

	public void Start_AsyncTask_PerformItemStateChange(Context context, OnAsyncTaskCompletedListener listener) {
		Start_AsyncTask(new AsyncTask_PerformItemStateChange(context, AsyncTask_finished, listener));
	}

	@SafeVarargs
	private final <Params> void Start_AsyncTask(final AsyncTask_Reader asyncTask, final Params... params) {
		setSyncRunning(true);
		if(apiFuture == null)
			apiFuture = executor.submit(apiCallable);
		asyncTask.setAPIFuture(apiFuture);
		asyncTask.executeOnExecutor(executor, params);
	}

	public boolean isSyncRunning() {
		return isSyncRunning;
	}
	
	public void setSyncRunning(boolean isSyncRunning) {
		this.isSyncRunning = isSyncRunning;
	}

	OnAsyncTaskCompletedListener AsyncTask_finished = new OnAsyncTaskCompletedListener() {
		
		@Override
		public void onAsyncTaskCompleted(Exception task_result) {
			setSyncRunning(false);
		}
	};
}
