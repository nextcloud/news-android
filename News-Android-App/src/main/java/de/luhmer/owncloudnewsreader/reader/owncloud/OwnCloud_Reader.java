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

import android.app.Activity;
import android.content.Context;
import android.util.SparseArray;

import de.luhmer.owncloudnewsreader.reader.AsyncTask_Reader;
import de.luhmer.owncloudnewsreader.reader.FeedItemTags;
import de.luhmer.owncloudnewsreader.reader.IReader;
import de.luhmer.owncloudnewsreader.reader.OnAsyncTaskCompletedListener;

public class OwnCloud_Reader implements IReader {
	boolean isSyncRunning = false;
	private API api = null;
	
	SparseArray<AsyncTask_Reader> AsyncTasksRunning;
	
	public OwnCloud_Reader() {
		AsyncTasksRunning = new SparseArray<AsyncTask_Reader>();
	}
	
	@Override
	public void Start_AsyncTask_GetItems(int task_id,
			Context context, OnAsyncTaskCompletedListener listener, FeedItemTags.TAGS tag) {
		setSyncRunning(true);
		AsyncTasksRunning.append(task_id, (AsyncTask_Reader) new AsyncTask_GetItems(task_id, context, new OnAsyncTaskCompletedListener[] { AsyncTask_finished, listener }, api).execute(tag));
	}

	@Override
	public void Start_AsyncTask_GetOldItems(int task_id,
			Context context, OnAsyncTaskCompletedListener listener, Long feed_id, Long folder_id) {
		setSyncRunning(true);
		AsyncTasksRunning.append(task_id, (AsyncTask_Reader) new AsyncTask_GetOldItems(task_id, context, new OnAsyncTaskCompletedListener[] { AsyncTask_finished, listener }, feed_id, folder_id, api).execute());
	}
	
	@Override
	public void Start_AsyncTask_GetFolder(int task_id,
			Context context, OnAsyncTaskCompletedListener listener) {
		setSyncRunning(true);
		AsyncTasksRunning.append(task_id, (AsyncTask_Reader) new AsyncTask_GetFolderTags(task_id, context, new OnAsyncTaskCompletedListener[] { AsyncTask_finished, listener }, api).execute());
	}
	
	@Override
	public void Start_AsyncTask_GetFeeds(int task_id,
			Context context, OnAsyncTaskCompletedListener listener) {
		setSyncRunning(true);
		AsyncTasksRunning.append(task_id, (AsyncTask_Reader) new AsyncTask_GetFeeds(task_id, context, new OnAsyncTaskCompletedListener[] { AsyncTask_finished, listener }, api).execute());
	}

	@Override
	public void Start_AsyncTask_PerformItemStateChange(int task_id,
			Context context, OnAsyncTaskCompletedListener listener) {
		setSyncRunning(true);
		AsyncTasksRunning.append(task_id, (AsyncTask_Reader) new AsyncTask_PerformItemStateChange(task_id, context, new OnAsyncTaskCompletedListener[] { AsyncTask_finished, listener }, api).execute());
	}
	
	
	public void Start_AsyncTask_GetVersion(int task_id,
			Context context, OnAsyncTaskCompletedListener listener, String username, String password) {
		setSyncRunning(true);
		AsyncTasksRunning.append(task_id, (AsyncTask_Reader) new AsyncTask_GetApiVersion(task_id, context, username, password, new OnAsyncTaskCompletedListener[] { AsyncTask_finished, listener }).execute());
	}
	
	@Override
	public void Start_AsyncTask_Authenticate(int task_id, Activity context,
			OnAsyncTaskCompletedListener listener) {

	}

	
	@Override
	public void onAsyncTaskCompleted(int task_id, Object task_result) {
		
	}

	@Override
	public boolean isSyncRunning() {
		return isSyncRunning;
	}
	
	@Override
	public void setSyncRunning(boolean isSyncRunning) {
		this.isSyncRunning = isSyncRunning;
	}

	OnAsyncTaskCompletedListener AsyncTask_finished = new OnAsyncTaskCompletedListener() {
		
		@Override
		public void onAsyncTaskCompleted(int task_id, Object task_result) {
			setSyncRunning(false);
			AsyncTasksRunning.remove(task_id);
		}
	};

	@Override
	public SparseArray<AsyncTask_Reader> getRunningAsyncTasks() {
		return AsyncTasksRunning;
	}

	@Override
	public void attachToRunningTask(int task_id, Activity activity, OnAsyncTaskCompletedListener listener) {
		if(AsyncTasksRunning.get(task_id) != null)
			AsyncTasksRunning.get(task_id).attach(activity, new OnAsyncTaskCompletedListener[] { listener, AsyncTask_finished });
	}

	public API getApi() {
		return api;
	}

	public void setApi(API api) {
		this.api = api;
	}	
}
