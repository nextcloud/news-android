package de.luhmer.owncloudnewsreader.authentication;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SyncResult;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.reactivestreams.Publisher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import de.luhmer.owncloudnewsreader.Constants;
import de.luhmer.owncloudnewsreader.NewsReaderApplication;
import de.luhmer.owncloudnewsreader.R;
import de.luhmer.owncloudnewsreader.database.DatabaseConnectionOrm;
import de.luhmer.owncloudnewsreader.database.model.Feed;
import de.luhmer.owncloudnewsreader.database.model.Folder;
import de.luhmer.owncloudnewsreader.di.ApiProvider;
import de.luhmer.owncloudnewsreader.helper.ForegroundListener;
import de.luhmer.owncloudnewsreader.helper.StopWatch;
import de.luhmer.owncloudnewsreader.notification.NextcloudNotificationManager;
import de.luhmer.owncloudnewsreader.reader.InsertIntoDatabase;
import de.luhmer.owncloudnewsreader.reader.nextcloud.ItemStateSync;
import de.luhmer.owncloudnewsreader.reader.nextcloud.RssItemObservable;
import de.luhmer.owncloudnewsreader.services.DownloadImagesService;
import de.luhmer.owncloudnewsreader.services.events.SyncFailedEvent;
import de.luhmer.owncloudnewsreader.services.events.SyncFinishedEvent;
import de.luhmer.owncloudnewsreader.services.events.SyncStartedEvent;
import de.luhmer.owncloudnewsreader.ssl.OkHttpSSLClient;
import de.luhmer.owncloudnewsreader.widget.WidgetProvider;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class OwnCloudSyncAdapter extends AbstractThreadedSyncAdapter {

    private static final String TAG = OwnCloudSyncAdapter.class.getCanonicalName();
    public boolean syncRunning = false;

    protected @Inject SharedPreferences mPrefs;
    protected @Inject ApiProvider mApi;


    public OwnCloudSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);

        ((NewsReaderApplication) context).getAppComponent().injectService(this);
    }



    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.d("udinic", "onPerformSync for account[" + account.name + "] [" + Thread.currentThread().getName() + "]\"");
        StopWatch syncStopWatch = new StopWatch();
        syncStopWatch.start();

        // Send sync started event
        syncRunning = true;
        EventBus.getDefault().post(new SyncStartedEvent());

        // run actual sync
        sync();

        // Update Widget / Notification
        WidgetProvider.UpdateWidget(getContext());
        updateNotification();

        // Download Favicons for feeds
        startFaviconDownload();


        // Send sync finished event
        syncRunning = false;
        EventBus.getDefault().post(new SyncFinishedEvent());

        syncStopWatch.stop();
        Log.v(TAG, "Finished sync - time needed (synchronization): " + syncStopWatch.toString());
    }

    private static class NextcloudSyncResult {
        private final List<Folder> folders;
        private final List<Feed>   feeds;
        private final boolean      stateSyncSuccessful;

        NextcloudSyncResult(List<Folder> folders, List<Feed> feeds, Boolean stateSyncSuccessful) {
            this.folders = folders;
            this.feeds = feeds;
            this.stateSyncSuccessful = stateSyncSuccessful;
        }
    }

    // Start sync
    private void sync() {
        if(mApi.getNewsAPI() == null) {
            throwException(new IllegalStateException("API is NOT initialized"));
            Log.e(TAG, "API is NOT initialized..");
            return;
        } else {
            Log.v(TAG, "API is initialized..");
        }

        final DatabaseConnectionOrm dbConn = new DatabaseConnectionOrm(getContext());

        Observable<Boolean> rssStateSync = Observable.fromPublisher(
                (Publisher<Boolean>) s -> {
                    Log.v(TAG, "(rssStateSync) subscribe() called with: s = [" + s + "] [" + Thread.currentThread().getName() + "]");
                    try {
                        ItemStateSync.PerformItemStateSync(mApi.getNewsAPI(), dbConn);
                        s.onNext(true);
                        s.onComplete();
                    } catch(Exception ex) {
                        s.onError(ex);
                    }
                }).subscribeOn(Schedulers.newThread());

        // First sync Feeds and Folders and rss item states (in parallel)
        Observable<ArrayList<Folder>> folderObservable = mApi
                .getNewsAPI()
                .folders()
                .map(folders -> {
                    // If the folders is more than one, returns the most new.
                    HashMap<String, Folder> uniqueLabelFolders = new HashMap<>();
                    for (Folder folder : folders) {
                        String label = folder.getLabel();
                        Folder uniqueFolder = uniqueLabelFolders.get(label);
                        if (uniqueFolder == null || uniqueFolder.getId() < folder.getId()) {
                            uniqueLabelFolders.put(label, folder);
                        }
                    }
                    return new ArrayList<>(uniqueLabelFolders.values());
                })
                .subscribeOn(Schedulers.newThread());

        Observable<List<Feed>> feedsObservable = mApi
                .getNewsAPI()
                .feeds()
                .subscribeOn(Schedulers.newThread());

        // Wait for results
        Observable<NextcloudSyncResult> combined = Observable.zip(folderObservable, feedsObservable, rssStateSync, (folders, feeds, mRes) -> {
            Log.v(TAG, "apply() called with: folders = [" + folders + "], feeds = [" + feeds + "], mRes = [" + mRes + "] [" + Thread.currentThread().getName() + "]");
            return new NextcloudSyncResult(folders, feeds, mRes);
        });

        Log.v(TAG, "subscribing now.. [" + Thread.currentThread().getName() + "]");


        try {
            NextcloudSyncResult syncResult = combined.blockingFirst();

            // Delete cached entities to keep entity relationships up to date for observers and readers,
            // for example, relationship of RSS items with feeds that have changed (name changed, etc).
            // The presence of old data in the cache can affect the obtaining of up-to-date information.
            dbConn.clearSessionCache();

            InsertIntoDatabase.InsertFoldersIntoDatabase(syncResult.folders, dbConn);
            InsertIntoDatabase.InsertFeedsIntoDatabase(syncResult.feeds, dbConn);
            Log.v(TAG, "State sync successful: " + syncResult.stateSyncSuccessful);

            // Start the sync (Rss Items)
            syncRssItems(dbConn);
        } catch(Exception ex) {
            //Log.e(TAG, "throwException: ", ex);
            throwException(ex);
        }
    }

    private void syncRssItems(final DatabaseConnectionOrm dbConn) {
        Log.v(TAG, "syncRssItems() called with: dbConn = [" + dbConn + "] [" + Thread.currentThread().getName() + "]");

        // .observeOn(AndroidSchedulers.mainThread())

        Observable.fromPublisher(new RssItemObservable(dbConn, mApi.getNewsAPI(), mPrefs))
                .subscribeOn(Schedulers.newThread())
                .blockingSubscribe(new Observer<>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        Log.d(TAG, "[syncRssItems] - onSubscribe() called");
                    }

                    @Override
                    public void onNext(@NonNull final Integer totalCount) {
                        Log.v(TAG, "[syncRssItems] - onNext() called with: totalCount = [" + totalCount + "]");

                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.post(() -> Toast.makeText(
                                getContext(),
                                getContext().getResources().getQuantityString(R.plurals.fetched_items_so_far, totalCount, totalCount),
                                Toast.LENGTH_SHORT).show());
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        Log.v(TAG, "[syncRssItems] - onError() called with: throwable = [" + e + "]");
                        throwException(e);
                    }

                    @Override
                    public void onComplete() {
                        Log.v(TAG, "[syncRssItems] - onComplete() called");
                    }
                });
    }


    private void throwException(Throwable ex) {
        Log.e(TAG, "throwException() called [" + Thread.currentThread().getName() + "]", ex);
        syncRunning = false;
        if(ex instanceof Exception) {
            EventBus.getDefault().post(SyncFailedEvent.create(OkHttpSSLClient.HandleExceptions((Exception) ex)));
        } else {
            EventBus.getDefault().post(SyncFailedEvent.create(ex));
        }
    }

    private void updateNotification() {
        // DatabaseConnectionOrm dbConn = new DatabaseConnectionOrm(getContext());
        int newItemsCountLastSync = mPrefs.getInt(Constants.LAST_UPDATE_NEW_ITEMS_COUNT_STRING, 0);

        if (newItemsCountLastSync > 0) {
            // int newItemsCount = Integer.parseInt(dbConn.getUnreadItemsCountForSpecificFolder(SubscriptionExpandableListAdapter.SPECIAL_FOLDERS.ALL_UNREAD_ITEMS));

            // If another app is not in foreground
            if (!ForegroundListener.isInForeground()) {
                NextcloudNotificationManager.showUnreadRssItemsNotification(getContext(), mPrefs, false);
            }
        }
    }

    private void startFaviconDownload() {
        Intent data = new Intent();
        data.putExtra(DownloadImagesService.DOWNLOAD_MODE_STRING, DownloadImagesService.DownloadMode.FAVICONS_ONLY);
        DownloadImagesService.enqueueWork(getContext(), data);
    }
}