package de.luhmer.owncloudnewsreader.reader;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.util.SparseArray;

public interface IReader {
	public boolean isSyncRunning();	
	
	public SparseArray<AsyncTask_Reader> getRunningAsyncTasks();
	public void attachToRunningTask(int task_id, Activity activity, OnAsyncTaskCompletedListener listener);
	
	public void Start_AsyncTask_GetFeeds(final int task_id, final Activity context, final OnAsyncTaskCompletedListener listener, FeedItemTags.TAGS tag);
	public void Start_AsyncTask_GetFolder(final int task_id, final Activity context, final OnAsyncTaskCompletedListener listener);
	public void Start_AsyncTask_GetSubFolder(final int task_id, final Activity context, final OnAsyncTaskCompletedListener listener);	
	public void Start_AsyncTask_PerformTagActionForSingleItem(final int task_id, final Context context, final OnAsyncTaskCompletedListener listener, String itemId, FeedItemTags.TAGS tag);
	public void Start_AsyncTask_Authenticate(final int task_id, final Activity context, final OnAsyncTaskCompletedListener listener);
	
	public abstract void onAsyncTaskCompleted(final int task_id, final Object task_result);

}
