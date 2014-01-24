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
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import de.luhmer.owncloudnewsreader.Constants;
import de.luhmer.owncloudnewsreader.Constants.SYNC_TYPES;
import de.luhmer.owncloudnewsreader.R;
import de.luhmer.owncloudnewsreader.SettingsActivity;
import de.luhmer.owncloudnewsreader.helper.AidlException;
import de.luhmer.owncloudnewsreader.helper.NotificationManagerNewsReader;
import de.luhmer.owncloudnewsreader.reader.FeedItemTags.TAGS;
import de.luhmer.owncloudnewsreader.reader.IReader;
import de.luhmer.owncloudnewsreader.reader.OnAsyncTaskCompletedListener;
import de.luhmer.owncloudnewsreader.reader.owncloud.API;
import de.luhmer.owncloudnewsreader.reader.owncloud.OwnCloud_Reader;
import de.luhmer.owncloudnewsreader.reader.owncloud.apiv2.APIv2;
import de.luhmer.owncloudnewsreader.reader.owncloud.apiv2.AsyncTask_TriggerOcUpdate;
import de.luhmer.owncloudnewsreader.services.IOwnCloudSyncService.Stub;
import de.luhmer.owncloudnewsreader.widget.WidgetProvider;

public class OwnCloudSyncService extends Service {
	
	protected static final String TAG = "OwnCloudSyncService";	
	
	private RemoteCallbackList<IOwnCloudSyncServiceCallback> callbacks = new RemoteCallbackList<IOwnCloudSyncServiceCallback>();

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
				startedSync(SYNC_TYPES.SYNC_TYPE__GET_API);
				OwnCloud_Reader ocReader = (OwnCloud_Reader) _Reader;
				SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(OwnCloudSyncService.this);
				String username = mPrefs.getString(SettingsActivity.EDT_USERNAME_STRING, "");
				String password = mPrefs.getString(SettingsActivity.EDT_PASSWORD_STRING, "");
				ocReader.Start_AsyncTask_GetVersion(Constants.TaskID_GetVersion, OwnCloudSyncService.this, onAsyncTask_GetVersionFinished, username, password);								
			}
		}

		@Override
		public boolean isSyncRunning() throws RemoteException {
			return _Reader.isSyncRunning();			
		}
	};
	
	
	static IReader _Reader;
	
	@Override
	public void onCreate() {
		super.onCreate();
		if(_Reader == null)
			_Reader = new OwnCloud_Reader();
		Log.d(TAG, "onCreate() called");
	}


	OnAsyncTaskCompletedListener onAsyncTask_GetVersionFinished = new OnAsyncTaskCompletedListener() {
		
		@Override
		public void onAsyncTaskCompleted(int task_id, Object task_result) {
			
			finishedSync(SYNC_TYPES.SYNC_TYPE__GET_API);
			
			if(!(task_result instanceof Exception))
			{	
				String appVersion = task_result.toString();
				API api = API.GetRightApiForVersion(appVersion, OwnCloudSyncService.this);
				((OwnCloud_Reader) _Reader).setApi(api);
				
				_Reader.Start_AsyncTask_PerformItemStateChange(Constants.TaskID_PerformStateChange,  OwnCloudSyncService.this, onAsyncTask_PerformTagExecute);
			
				startedSync(SYNC_TYPES.SYNC_TYPE__ITEM_STATES);
			}
			else 				
				ThrowException((Exception) task_result);
		}
	};
	
	//Sync state of items e.g. read/unread/starred/unstarred
    OnAsyncTaskCompletedListener onAsyncTask_PerformTagExecute = new OnAsyncTaskCompletedListener() {
        @Override
        public void onAsyncTaskCompleted(int task_id, Object task_result) {
        	
        	finishedSync(SYNC_TYPES.SYNC_TYPE__ITEM_STATES);
        	
            if(task_result != null)//task result is null if there was an error
            {	
            	if((Boolean) task_result)
            	{	
            		if(task_id == Constants.TaskID_PerformStateChange) {
            			_Reader.Start_AsyncTask_GetFolder(Constants.TaskID_GetFolder,  OwnCloudSyncService.this, onAsyncTask_GetFolder);
            			
            			
            			startedSync(SYNC_TYPES.SYNC_TYPE__FOLDER);
            		}
            		else
            			_Reader.setSyncRunning(true);
            	}
            }
        }
    };
	
    
	OnAsyncTaskCompletedListener onAsyncTask_GetFolder = new OnAsyncTaskCompletedListener() {
		@Override
		public void onAsyncTaskCompleted(int task_id, Object task_result) {
			
			finishedSync(SYNC_TYPES.SYNC_TYPE__FOLDER);
			
			if(task_result != null)
				ThrowException((Exception) task_result);
			else {
                _Reader.Start_AsyncTask_GetFeeds(Constants.TaskID_GetFeeds, OwnCloudSyncService.this, onAsyncTask_GetFeed);
                
                startedSync(SYNC_TYPES.SYNC_TYPE__FEEDS);
            }

            Log.d(TAG, "onAsyncTask_GetFolder Finished");
		
		}
	};
	
	OnAsyncTaskCompletedListener onAsyncTask_GetFeed = new OnAsyncTaskCompletedListener() {
		
		@Override
		public void onAsyncTaskCompleted(int task_id, Object task_result) {
			
			finishedSync(SYNC_TYPES.SYNC_TYPE__FEEDS);
			
			if(task_result != null)
				ThrowException((Exception) task_result);
			else {
                _Reader.Start_AsyncTask_GetItems(Constants.TaskID_GetItems, OwnCloudSyncService.this, onAsyncTask_GetItems, TAGS.ALL);//Recieve all unread Items
                
                startedSync(SYNC_TYPES.SYNC_TYPE__ITEMS);
            }

            Log.d(TAG, "onAsyncTask_GetFeed Finished");
		}
	};
	
	OnAsyncTaskCompletedListener onAsyncTask_GetItems = new OnAsyncTaskCompletedListener() {
		
		@Override
		public void onAsyncTaskCompleted(int task_id, Object task_result) {
			finishedSync(SYNC_TYPES.SYNC_TYPE__ITEMS);
			
			if(task_result != null)
            	ThrowException((Exception) task_result);
            else
            {
                SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(OwnCloudSyncService.this);
                int newItemsCount = mPrefs.getInt(Constants.LAST_UPDATE_NEW_ITEMS_COUNT_STRING, 0);
                if(newItemsCount > 0) {
                    ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
                    List<ActivityManager.RunningTaskInfo> runningTaskInfo = am.getRunningTasks(1);

                    ComponentName componentInfo = runningTaskInfo.get(0).topActivity;
                    if(!componentInfo.getPackageName().equals("de.luhmer.owncloudnewsreader")) {
                        String tickerText = getString(R.string.notification_new_items_ticker).replace("X", String.valueOf(newItemsCount));
                        String contentText = getString(R.string.notification_new_items_text).replace("X", String.valueOf(newItemsCount));
                        String title = getString(R.string.app_name);

                        if(mPrefs.getBoolean(SettingsActivity.CB_SHOW_NOTIFICATION_NEW_ARTICLES_STRING, true))//Default is true
                            NotificationManagerNewsReader.getInstance(OwnCloudSyncService.this).ShowMessage(title, tickerText, contentText);
                    }
                    UpdateWidget();
                }

                if(_Reader instanceof OwnCloud_Reader) {
                    API api = ((OwnCloud_Reader)_Reader).getApi();
                    if(api instanceof APIv2) {
                        new AsyncTask_TriggerOcUpdate(0,
                                OwnCloudSyncService.this,
                                api.getUsername(),
                                api.getPassword(),
                                new OnAsyncTaskCompletedListener[] { onAsyncTask_UpdateOcFeeds })
                                .execute((Void) null);
                    }
                }
            }

            Log.d(TAG, "onAsyncTask_GetItems Finished");
			//fireUpdateFinishedClicked();
			
		}
	};

    OnAsyncTaskCompletedListener onAsyncTask_UpdateOcFeeds = new OnAsyncTaskCompletedListener() {
        @Override
        public void onAsyncTaskCompleted(int task_id, Object task_result) {

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
	
	private void startedSync(SYNC_TYPES sync_type) {
		List<IOwnCloudSyncServiceCallback> callbackList = getCallBackItemsAndBeginBroadcast();
		for(IOwnCloudSyncServiceCallback icb : callbackList) {
			try {
				icb.startedSync(sync_type.toString());
				//icb.finishedSyncOfItems();
			} catch (RemoteException e) {						
				e.printStackTrace();
			}
		}
		callbacks.finishBroadcast();
	}
	
	private void finishedSync(SYNC_TYPES sync_type) {
		List<IOwnCloudSyncServiceCallback> callbackList = getCallBackItemsAndBeginBroadcast();
		for(IOwnCloudSyncServiceCallback icb : callbackList) {
			try {
				icb.finishedSync(sync_type.toString());
				//icb.finishedSyncOfItems();
			} catch (RemoteException e) {						
				e.printStackTrace();
			}
		}
		callbacks.finishBroadcast();
	}
	
	private List<IOwnCloudSyncServiceCallback> getCallBackItemsAndBeginBroadcast() {
		// Broadcast to all clients the new value.
		List<IOwnCloudSyncServiceCallback> callbackList = new ArrayList<IOwnCloudSyncServiceCallback>();
        final int N = callbacks.beginBroadcast();
        for (int i=0; i < N; i++) {
            callbackList.add((IOwnCloudSyncServiceCallback) callbacks.getBroadcastItem(i));
        }
        return callbackList;
	}	
	

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}
}
