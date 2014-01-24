package de.luhmer.owncloudnewsreader.services;

import de.luhmer.owncloudnewsreader.services.IOwnCloudSyncServiceCallback;

// Declare any non-default types here with import statements

/** Example service interface */
interface IOwnCloudSyncService {
    /** Request the process ID of this service, to do evil things with it. */
    
    
    void registerCallback(IOwnCloudSyncServiceCallback callback);
    void unregisterCallback(IOwnCloudSyncServiceCallback callback);
    void startSync();
    boolean isSyncRunning();
}