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

package de.luhmer.owncloudnewsreader.services;

import android.app.ActivityManager;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;

import org.apache.commons.lang3.time.StopWatch;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import de.luhmer.owncloudnewsreader.Constants;
import de.luhmer.owncloudnewsreader.R;
import de.luhmer.owncloudnewsreader.SettingsActivity;
import de.luhmer.owncloudnewsreader.helper.AidlException;
import de.luhmer.owncloudnewsreader.helper.NotificationManagerNewsReader;
import de.luhmer.owncloudnewsreader.reader.FeedItemTags;
import de.luhmer.owncloudnewsreader.reader.OnAsyncTaskCompletedListener;
import de.luhmer.owncloudnewsreader.reader.owncloud.OwnCloud_Reader;
import de.luhmer.owncloudnewsreader.services.IOwnCloudSyncService.Stub;
import de.luhmer.owncloudnewsreader.widget.WidgetProvider;

public class OwnCloudSyncService extends Service {
	
	protected static final String TAG = "OwnCloudSyncService";	
	
	private RemoteCallbackList<IOwnCloudSyncServiceCallback> callbacks = new RemoteCallbackList<>();

	private CountDownLatch syncCompletedLatch;
	private StopWatch syncStopWatch;
	private boolean syncRunning;

	private Stub mBinder = new IOwnCloudSyncService.Stub() {

		public void registerCallback(IOwnCloudSyncServiceCallback callback) {
			callbacks.register(callback);
		}

		public void unregisterCallback(IOwnCloudSyncServiceCallback callback) {
			callbacks.unregister(callback);
		}

		@Override
		public void startSync() throws RemoteException {
			if(!isSyncRunning()) {
				startedSync();
				OwnCloud_Reader.getInstance().Start_AsyncTask_PerformItemStateChange(OwnCloudSyncService.this, onAsyncTask_PerformTagExecute);
			}
		}

		@Override
		public boolean isSyncRunning() throws RemoteException {
			return syncRunning;
		}
	};

	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(TAG, "onCreate() called");
	}

    @Override
    public boolean onUnbind(Intent intent) {
        //Destroy service if no sync is running
        if(!syncRunning) {
            Log.v(TAG, "Stopping service because of inactivity");
            stopSelf();
        }

        return super.onUnbind(intent);
    }

	//Sync state of items e.g. read/unread/starred/unstarred
    private final OnAsyncTaskCompletedListener onAsyncTask_PerformTagExecute = new OnAsyncTaskCompletedListener() {
        @Override
        public void onAsyncTaskCompleted(Exception task_result) {
			syncCompletedLatch = new CountDownLatch(3);
			syncStopWatch = new StopWatch();
			syncStopWatch.start();

			OwnCloud_Reader.getInstance().Start_AsyncTask_GetFolder(OwnCloudSyncService.this, onAsyncTaskFinished);
			OwnCloud_Reader.getInstance().Start_AsyncTask_GetFeeds(OwnCloudSyncService.this, onAsyncTaskFinished);
			OwnCloud_Reader.getInstance().Start_AsyncTask_GetItems(OwnCloudSyncService.this, onAsyncTaskFinished, FeedItemTags.ALL); //Receive all unread Items
			AsyncTask.execute(syncCompletionRunnable);
		}
    };

	private final Runnable syncCompletionRunnable = new Runnable() {
		@Override
		public void run() {
			try {
				syncCompletedLatch.await();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} finally {
				new Handler(Looper.getMainLooper()).post(new Runnable() {
					public void run() {
						finishedSync();
					}
				});
			}
		}
	};

	private final OnAsyncTaskCompletedListener onAsyncTaskFinished = new OnAsyncTaskCompletedListener() {

		@Override
		public void onAsyncTaskCompleted(Exception task_result) {
			if(task_result != null)
				ThrowException(task_result);
			syncCompletedLatch.countDown();
		}
	};

    private void UpdateWidget()
    {
        Intent intent = new Intent(this, WidgetProvider.class);
        intent.setAction("android.appwidget.action.APPWIDGET_UPDATE");
        // Use an array and EXTRA_APPWIDGET_IDS instead of AppWidgetManager.EXTRA_APPWIDGET_ID,
        // since it seems the onUpdate() is only fired on that:

        int ids[] = AppWidgetManager.getInstance(getApplication()).getAppWidgetIds(new ComponentName(getApplication(), WidgetProvider.class));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS,ids);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,ids);
        sendBroadcast(intent);
    }

	private void ThrowException(Exception ex) {
		List<IOwnCloudSyncServiceCallback> callbackList = getCallBackItemsAndBeginBroadcast();
		for (IOwnCloudSyncServiceCallback icb : callbackList) {
			try {
				icb.throwException(new AidlException(ex));
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		callbacks.finishBroadcast();
	}
	
	private void startedSync() {
		syncRunning = true;
		Log.v(TAG, "Synchronization started");

		List<IOwnCloudSyncServiceCallback> callbackList = getCallBackItemsAndBeginBroadcast();
		for(IOwnCloudSyncServiceCallback icb : callbackList) {
			try {
				icb.startedSync();
				//icb.finishedSyncOfItems();
			} catch (RemoteException e) {						
				e.printStackTrace();
			}
		}
		callbacks.finishBroadcast();
	}
	
	private void finishedSync() {
		syncRunning = false;
		syncStopWatch.stop();
        Log.v(TAG, "Time needed (synchronization): " + syncStopWatch.toString());

		SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(OwnCloudSyncService.this);
		int newItemsCount = mPrefs.getInt(Constants.LAST_UPDATE_NEW_ITEMS_COUNT_STRING, 0);
		if(newItemsCount > 0) {
			ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
			List<ActivityManager.RunningTaskInfo> runningTaskInfo = am.getRunningTasks(1);

			ComponentName componentInfo = runningTaskInfo.get(0).topActivity;
			if(!componentInfo.getPackageName().equals("de.luhmer.owncloudnewsreader")) {
				Resources res = getResources();
				String tickerText = res.getQuantityString(R.plurals.notification_new_items_ticker, newItemsCount, newItemsCount);
				String contentText = res.getQuantityString(R.plurals.notification_new_items_text, newItemsCount, newItemsCount);
				String title = getString(R.string.app_name);

				if(mPrefs.getBoolean(SettingsActivity.CB_SHOW_NOTIFICATION_NEW_ARTICLES_STRING, true))//Default is true
					NotificationManagerNewsReader.getInstance(OwnCloudSyncService.this).ShowMessage(title, tickerText, contentText);
			}
			UpdateWidget();
		}

		List<IOwnCloudSyncServiceCallback> callbackList = getCallBackItemsAndBeginBroadcast();
		for(IOwnCloudSyncServiceCallback icb : callbackList) {
			try {
				icb.finishedSync();
				//icb.finishedSyncOfItems();
			} catch (RemoteException e) {						
				e.printStackTrace();
			}
		}
		callbacks.finishBroadcast();
	}
	
	private List<IOwnCloudSyncServiceCallback> getCallBackItemsAndBeginBroadcast() {
		// Broadcast to all clients the new value.
		List<IOwnCloudSyncServiceCallback> callbackList = new ArrayList<>();
        final int N = callbacks.beginBroadcast();
        for (int i=0; i < N; i++) {
            callbackList.add(callbacks.getBroadcastItem(i));
        }
        return callbackList;
	}	
	

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}
}
