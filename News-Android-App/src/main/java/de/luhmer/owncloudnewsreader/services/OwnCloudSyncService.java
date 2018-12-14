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
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import org.apache.commons.lang3.time.StopWatch;
import org.greenrobot.eventbus.EventBus;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

import java.util.List;

import javax.inject.Inject;

import de.luhmer.owncloudnewsreader.ListView.SubscriptionExpandableListAdapter;
import de.luhmer.owncloudnewsreader.NewsReaderApplication;
import de.luhmer.owncloudnewsreader.R;
import de.luhmer.owncloudnewsreader.SettingsActivity;
import de.luhmer.owncloudnewsreader.database.DatabaseConnectionOrm;
import de.luhmer.owncloudnewsreader.database.model.Feed;
import de.luhmer.owncloudnewsreader.database.model.Folder;
import de.luhmer.owncloudnewsreader.di.ApiProvider;
import de.luhmer.owncloudnewsreader.notification.NextcloudNotificationManager;
import de.luhmer.owncloudnewsreader.reader.InsertIntoDatabase;
import de.luhmer.owncloudnewsreader.reader.nextcloud.ItemStateSync;
import de.luhmer.owncloudnewsreader.reader.nextcloud.RssItemObservable;
import de.luhmer.owncloudnewsreader.services.events.SyncFailedEvent;
import de.luhmer.owncloudnewsreader.services.events.SyncFinishedEvent;
import de.luhmer.owncloudnewsreader.services.events.SyncStartedEvent;
import de.luhmer.owncloudnewsreader.ssl.MemorizingTrustManager;
import de.luhmer.owncloudnewsreader.ssl.OkHttpSSLClient;
import de.luhmer.owncloudnewsreader.widget.WidgetProvider;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function3;
import io.reactivex.schedulers.Schedulers;

public class OwnCloudSyncService extends Service {

    private StopWatch syncStopWatch;
    // This is the object that receives interactions from clients.  See
    // RemoteService for a more complete example.
    private final IBinder mBinder = new OwnCloudSyncServiceBinder();

    protected static final String TAG = "OwnCloudSyncService";

    private boolean syncRunning;

    protected @Inject SharedPreferences mPrefs;
    protected @Inject ApiProvider mApi;
    protected @Inject MemorizingTrustManager mMTM;

    /**
     * Class for clients to access.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with
     * IPC.
     * */
    public class OwnCloudSyncServiceBinder extends Binder {
       public OwnCloudSyncService getService() {
			return OwnCloudSyncService.this;
		}
    }


    public void startSync() {
        if(!isSyncRunning()) {
            startedSync();
            start();
        }
    }

    public boolean isSyncRunning() {
		return syncRunning;
	}

	@Override
	public void onCreate() {
        ((NewsReaderApplication) getApplication()).getAppComponent().injectService(this);
		super.onCreate();
		Log.v(TAG, "onCreate() called");
	}

    @Override
    public void onDestroy() {
        Log.v(TAG, "onDestroy() called");
        super.onDestroy();
    }

    @Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	@Override
    public boolean onUnbind(Intent intent) {
        Log.v(TAG, "onUnbind() called with: intent = [" + intent + "]");

        //Destroy service if no sync is running
        if(!syncRunning) {
            Log.v(TAG, "Stopping service because of inactivity");
            stopSelf();
        }

        return super.onUnbind(intent);
    }

    private class SyncResult {
        List<Folder> folders;
        List<Feed>   feeds;
        boolean      stateSyncSuccessful;

        SyncResult(List<Folder> folders, List<Feed> feeds, Boolean stateSyncSuccessful) {
            this.folders = folders;
            this.feeds = feeds;
            this.stateSyncSuccessful = stateSyncSuccessful;
        }
    }

    // Start sync
    private void start() {
        syncStopWatch = new StopWatch();
        syncStopWatch.start();


        if(mApi.getAPI() == null) {
            ThrowException(new IllegalStateException("API is NOT initialized"));
            Log.e(TAG, "API is NOT initialized..");
        } else {
            Log.v(TAG, "API is initialized..");
        }

        final DatabaseConnectionOrm dbConn = new DatabaseConnectionOrm(OwnCloudSyncService.this);

        Observable<Boolean> rssStateSync = Observable.fromPublisher(
                new Publisher<Boolean>() {
                   @Override
                   public void subscribe(Subscriber<? super Boolean> s) {
                       Log.v(TAG, "(rssStateSync) subscribe() called with: s = [" + s + "] [" + Thread.currentThread().getName() + "]");
                       try {
                           ItemStateSync.PerformItemStateSync(mApi.getAPI(), dbConn);
                           s.onNext(true);
                           s.onComplete();
                       } catch(Exception ex) {
                           s.onError(ex);
                       }
                   }
               }).subscribeOn(Schedulers.newThread());

        // First sync Feeds and Folders and rss item states (in parallel)
        Observable<List<Folder>> folderObservable = mApi
                .getAPI()
                .folders()
                .subscribeOn(Schedulers.newThread());

        Observable<List<Feed>> feedsObservable = mApi
                .getAPI()
                .feeds()
                .subscribeOn(Schedulers.newThread());

        // Wait for results
        Observable<SyncResult> combined = Observable.zip(folderObservable, feedsObservable, rssStateSync, new Function3<List<Folder>, List<Feed>, Boolean, SyncResult>() {
            @Override
            public SyncResult apply(@NonNull List<Folder> folders, @NonNull List<Feed> feeds, @NonNull Boolean mRes) {
                Log.v(TAG, "apply() called with: folders = [" + folders + "], feeds = [" + feeds + "], mRes = [" + mRes + "] [" + Thread.currentThread().getName() + "]");
                return new SyncResult(folders, feeds, mRes);
            }
        });

        Log.v(TAG, "subscribing now.. [" + Thread.currentThread().getName() + "]");

        try {
            SyncResult syncResult = combined.blockingFirst();

            InsertIntoDatabase.InsertFoldersIntoDatabase(syncResult.folders, dbConn);
            InsertIntoDatabase.InsertFeedsIntoDatabase(syncResult.feeds, dbConn);

            // Start the sync (Rss Items)
            syncRssItems(dbConn);
        } catch(Exception ex) {
            //Log.e(TAG, "ThrowException: ", ex);
            ThrowException(ex);
        }
    }

    private void syncRssItems(final DatabaseConnectionOrm dbConn) {
        Log.v(TAG, "syncRssItems() called with: dbConn = [" + dbConn + "] [" + Thread.currentThread().getName() + "]");

        Observable.fromPublisher(new RssItemObservable(dbConn, mApi.getAPI(), mPrefs))
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new Observer<Integer>() {
                @Override
                public void onSubscribe(@NonNull Disposable d) {
                    Log.d(TAG, "onSubscribe() called with: d = [" + d + "]");
                }

                @Override
                public void onNext(@NonNull Integer totalCount) {
                    Log.v(TAG, "onNext() called with: totalCount = [" + totalCount + "]");
                    Toast.makeText(
                            OwnCloudSyncService.this,
                            OwnCloudSyncService.this.getResources().getQuantityString(R.plurals.fetched_items_so_far, totalCount, totalCount),
                            Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(@NonNull Throwable e) {
                    Log.v(TAG, "onError() called with: throwable = [" + e + "]");
                    ThrowException(e);
                }

                @Override
                public void onComplete() {
                    Log.v(TAG, "onComplete() called");
                    finishedSync();
                }
            });
    }


    private void ThrowException(Throwable ex) {
        Log.e(TAG, "ThrowException() called [" + Thread.currentThread().getName() + "]", ex);
        syncRunning = false;
        if(ex instanceof Exception) {
            EventBus.getDefault().post(SyncFailedEvent.create(OkHttpSSLClient.HandleExceptions((Exception) ex)));
        } else {
            EventBus.getDefault().post(SyncFailedEvent.create(ex));
        }
	}

	private void startedSync() {
		syncRunning = true;
		Log.v(TAG, "Synchronization started [" + Thread.currentThread().getName() + "]");
		EventBus.getDefault().post(new SyncStartedEvent());
	}

	private void finishedSync() {
		WidgetProvider.UpdateWidget(this);

		syncRunning = false;

        syncStopWatch.stop();
        Log.v(TAG, "Time needed (synchronization): " + syncStopWatch.toString());

		SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(OwnCloudSyncService.this);

		DatabaseConnectionOrm dbConn = new DatabaseConnectionOrm(this);
		int newItemsCount = Integer.parseInt(dbConn.getUnreadItemsCountForSpecificFolder(SubscriptionExpandableListAdapter.SPECIAL_FOLDERS.ALL_UNREAD_ITEMS));
		//int newItemsCount = mPrefs.getInt(Constants.LAST_UPDATE_NEW_ITEMS_COUNT_STRING, 0);

		if(newItemsCount > 0) {
            String foregroundActivityPackageName = "";

            ActivityManager mActivityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningTaskInfo> rtiList = mActivityManager.getRunningTasks(1);
            if(rtiList.size() > 0) {
                foregroundActivityPackageName = rtiList.get(0).topActivity.getPackageName();
            }

            //Log.v(TAG, "foregroundActivityPackageName=" + foregroundActivityPackageName);

            // If another app is opened show a notification
            if (!foregroundActivityPackageName.equals(getPackageName())) {
                if (mPrefs.getBoolean(SettingsActivity.CB_SHOW_NOTIFICATION_NEW_ARTICLES_STRING, true)) {
                    NextcloudNotificationManager.showUnreadRssItemsNotification(OwnCloudSyncService.this, newItemsCount);
                }
            }
        }

        Intent data = new Intent();
        data.putExtra(DownloadImagesService.DOWNLOAD_MODE_STRING, DownloadImagesService.DownloadMode.FAVICONS_ONLY);
        DownloadImagesService.enqueueWork(OwnCloudSyncService.this, data);

        EventBus.getDefault().post(new SyncFinishedEvent());
    }
}
