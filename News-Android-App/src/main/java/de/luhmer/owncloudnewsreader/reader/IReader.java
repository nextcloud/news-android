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
import android.util.SparseArray;

public interface IReader {
	public boolean isSyncRunning();	
	public void setSyncRunning(boolean status);
	
	public SparseArray<AsyncTask_Reader> getRunningAsyncTasks();
	public void attachToRunningTask(int task_id, Activity activity, OnAsyncTaskCompletedListener listener);
	
	public void Start_AsyncTask_GetItems(final int task_id, final Context context, final OnAsyncTaskCompletedListener listener, FeedItemTags.TAGS tag);
	public void Start_AsyncTask_GetOldItems(final int task_id, final Context context, final OnAsyncTaskCompletedListener listener, Long feed_id, Long folder_id);
	
	public void Start_AsyncTask_GetFolder(final int task_id, final Context context, final OnAsyncTaskCompletedListener listener);
	public void Start_AsyncTask_GetFeeds(final int task_id, final Context context, final OnAsyncTaskCompletedListener listener);	
	public void Start_AsyncTask_PerformItemStateChange(final int task_id, final Context context, final OnAsyncTaskCompletedListener listener);
	public void Start_AsyncTask_Authenticate(final int task_id, final Activity context, final OnAsyncTaskCompletedListener listener);
	
	public abstract void onAsyncTaskCompleted(final int task_id, final Object task_result);
}
