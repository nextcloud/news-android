package de.luhmer.owncloudnewsreader.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import de.luhmer.owncloudnewsreader.authentication.OwnCloudSyncAdapter;

public class OwnCloudSyncService extends Service {

    // https://developer.android.com/training/sync-adapters/creating-sync-adapter#java
    private static final String TAG = OwnCloudSyncService.class.getCanonicalName();

    private static final Object sSyncAdapterLock = new Object();
    private static OwnCloudSyncAdapter sSyncAdapter = null;

    @Override
    public void onCreate() {
        /*
        * Create the sync adapter as a singleton.
        * Set the sync adapter as syncable
        * Disallow parallel syncs
        */
        synchronized (sSyncAdapterLock) {
            if (sSyncAdapter == null) {
                sSyncAdapter = new OwnCloudSyncAdapter(this, true);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        /*
        * Get the object that allows external processes
        * to call onPerformSync(). The object is created
        * in the base class code when the SyncAdapter
        * constructors call super()
        */
        return sSyncAdapter.getSyncAdapterBinder();
    }

    public static boolean isSyncRunning() {
        Log.d(TAG, "isSyncRunning() called");
        //return syncRunning;
        if(sSyncAdapter != null) {
            return sSyncAdapter.syncRunning;
        }
        return false;
    }
}