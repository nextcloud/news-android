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

import java.util.ArrayList;
import java.util.List;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;
import de.luhmer.owncloudnewsreader.Constants;
import de.luhmer.owncloudnewsreader.SettingsActivity;
import de.luhmer.owncloudnewsreader.authentication.OwnCloudSyncAdapter;
import de.luhmer.owncloudnewsreader.helper.AidlException;
import de.luhmer.owncloudnewsreader.reader.FeedItemTags.TAGS;
import de.luhmer.owncloudnewsreader.reader.IReader;
import de.luhmer.owncloudnewsreader.reader.OnAsyncTaskCompletedListener;
import de.luhmer.owncloudnewsreader.reader.owncloud.API;
import de.luhmer.owncloudnewsreader.reader.owncloud.OwnCloud_Reader;
import de.luhmer.owncloudnewsreader.services.IOwnCloudSyncService.Stub;

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
			OwnCloud_Reader ocReader = (OwnCloud_Reader) _Reader;
			SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(OwnCloudSyncService.this);
			String username = mPrefs.getString(SettingsActivity.EDT_USERNAME_STRING, "");
			String password = mPrefs.getString(SettingsActivity.EDT_PASSWORD_STRING, "");
			ocReader.Start_AsyncTask_GetVersion(Constants.TaskID_GetVersion, OwnCloudSyncService.this, onAsyncTask_GetVersionFinished, username, password);
		}

		@Override
		public boolean isSyncRunning() throws RemoteException {
			return _Reader.isSyncRunning();			
		}
	};
	
	
	IReader _Reader = new OwnCloud_Reader();
	
	// Storage for an instance of the sync adapter
    private static OwnCloudSyncAdapter sSyncAdapter = null;
    // Object to use as a thread-safe lock
    private static final Object sSyncAdapterLock = new Object();
	
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		/*
         * Create the sync adapter as a singleton.
         * Set the sync adapter as syncable
         * Disallow parallel syncs
         */
        synchronized (sSyncAdapterLock) {
            if (sSyncAdapter == null) {
                sSyncAdapter = new OwnCloudSyncAdapter(getApplicationContext(), true);
            }
        }
		
		Log.d(TAG, "onCreate() called");
	}


	OnAsyncTaskCompletedListener onAsyncTask_GetVersionFinished = new OnAsyncTaskCompletedListener() {
		
		@Override
		public void onAsyncTaskCompleted(int task_id, Object task_result) {
			
			if(!(task_result instanceof Exception))
			{						
				String appVersion = task_result.toString();					
				API api = API.GetRightApiForVersion(appVersion, OwnCloudSyncService.this);
				((OwnCloud_Reader) _Reader).setApi(api);
				
				_Reader.Start_AsyncTask_PerformItemStateChange(Constants.TaskID_PerformStateChange,  OwnCloudSyncService.this, onAsyncTask_PerformTagExecute);
			
				List<IOwnCloudSyncServiceCallback> callbackList = getCallBackItemsAndBeginBroadcast();
				for(IOwnCloudSyncServiceCallback icb : callbackList) {
					try {
						icb.startedSyncOfItemStates();
					} catch (RemoteException e) {						
						e.printStackTrace();
					}
				}
				callbacks.finishBroadcast();
			}
			else 				
				ThrowException((Exception) task_result);
		}
	};
	
	//Sync state of items e.g. read/unread/starred/unstarred
    OnAsyncTaskCompletedListener onAsyncTask_PerformTagExecute = new OnAsyncTaskCompletedListener() {
        @Override
        public void onAsyncTaskCompleted(int task_id, Object task_result) {
        	
            if(task_result != null)//task result is null if there was an error
            {	
            	List<IOwnCloudSyncServiceCallback> callbackList = getCallBackItemsAndBeginBroadcast();
            	
            	//Started
				for(IOwnCloudSyncServiceCallback icb : callbackList) {
					try {
						icb.finishedSyncOfItemStates();
					} catch (RemoteException e) {						
						e.printStackTrace();
					}
				}
            	
            	if((Boolean) task_result)
            	{	
            		if(task_id == Constants.TaskID_PerformStateChange) {
            			_Reader.Start_AsyncTask_GetFolder(Constants.TaskID_GetFolder,  OwnCloudSyncService.this, onAsyncTask_GetFolder);
            			
            			
            			//Started
        				for(IOwnCloudSyncServiceCallback icb : callbackList) {
        					try {
        						icb.startedSyncOfFolders();
        					} catch (RemoteException e) {						
        						e.printStackTrace();
        					}
        				}
            		}
            		else
            			_Reader.setSyncRunning(true);
            	}
            	callbacks.finishBroadcast();
            }
        }
    };
	
    
	OnAsyncTaskCompletedListener onAsyncTask_GetFolder = new OnAsyncTaskCompletedListener() {
		@Override
		public void onAsyncTaskCompleted(int task_id, Object task_result) {
			
			List<IOwnCloudSyncServiceCallback> callbackList = getCallBackItemsAndBeginBroadcast();
			for(IOwnCloudSyncServiceCallback icb : callbackList) {
				try {
					icb.finishedSyncOfFolders();
				} catch (RemoteException e) {						
					e.printStackTrace();
				}
			}
			callbacks.finishBroadcast();
			
			
			if(task_result != null)
				ThrowException((Exception) task_result);
			else {
                _Reader.Start_AsyncTask_GetFeeds(Constants.TaskID_GetFeeds, OwnCloudSyncService.this, onAsyncTask_GetFeed);
                  
                callbackList = getCallBackItemsAndBeginBroadcast();
				for(IOwnCloudSyncServiceCallback icb : callbackList) {
					try {
						icb.startedSyncOfFeeds();
					} catch (RemoteException e) {						
						e.printStackTrace();
					}
				}
				callbacks.finishBroadcast();
            }

            Log.d(TAG, "onAsyncTask_GetFolder Finished");
		
		}
	};
	
	OnAsyncTaskCompletedListener onAsyncTask_GetFeed = new OnAsyncTaskCompletedListener() {
		
		@Override
		public void onAsyncTaskCompleted(int task_id, Object task_result) {
			
			List<IOwnCloudSyncServiceCallback> callbackList = getCallBackItemsAndBeginBroadcast();
			for(IOwnCloudSyncServiceCallback icb : callbackList) {
				try {
					icb.finishedSyncOfFeeds();
				} catch (RemoteException e) {						
					e.printStackTrace();
				}
			}
			callbacks.finishBroadcast();
			
			if(task_result != null)
				ThrowException((Exception) task_result);
			else {
                _Reader.Start_AsyncTask_GetItems(Constants.TaskID_GetItems, OwnCloudSyncService.this, onAsyncTask_GetItems, TAGS.ALL);//Recieve all unread Items

                
                callbackList = getCallBackItemsAndBeginBroadcast();
    			for(IOwnCloudSyncServiceCallback icb : callbackList) {
    				try {
    					icb.startedSyncOfItems();
    				} catch (RemoteException e) {						
    					e.printStackTrace();
    				}
    			}
    			callbacks.finishBroadcast();
            }

            Log.d(TAG, "onAsyncTask_GetFeed Finished");
		}
	};
	
	OnAsyncTaskCompletedListener onAsyncTask_GetItems = new OnAsyncTaskCompletedListener() {
		
		@Override
		public void onAsyncTaskCompleted(int task_id, Object task_result) {
			List<IOwnCloudSyncServiceCallback> callbackList = getCallBackItemsAndBeginBroadcast();
			for(IOwnCloudSyncServiceCallback icb : callbackList) {
				try {
					icb.finishedSyncOfItems();
				} catch (RemoteException e) {						
					e.printStackTrace();
				}
			}
			callbacks.finishBroadcast();
			
			if(task_result != null)
            	ThrowException((Exception) task_result);
                        
            Log.d(TAG, "onAsyncTask_GetItems Finished");
			//fireUpdateFinishedClicked();
			
		}
	};
	
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
