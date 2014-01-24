package de.luhmer.owncloudnewsreader.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import de.luhmer.owncloudnewsreader.authentication.OwnCloudSyncAdapter;

public class OwnCloudSettingsSyncService extends Service {
	  private static final Object sSyncAdapterLock = new Object();
	  private static OwnCloudSyncAdapter sSyncAdapter = null;
	 
	  @Override
	  public void onCreate() {
	      synchronized (sSyncAdapterLock) {
	          if (sSyncAdapter == null) {
	              sSyncAdapter = new OwnCloudSyncAdapter(this, true);
	          }
	      }
	  }
	 
	  @Override
	  public IBinder onBind(Intent intent) {
	      return sSyncAdapter.getSyncAdapterBinder();
	  }
}