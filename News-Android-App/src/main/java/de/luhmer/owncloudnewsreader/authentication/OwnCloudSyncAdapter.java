package de.luhmer.owncloudnewsreader.authentication;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ComponentName;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SyncResult;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import de.luhmer.owncloudnewsreader.services.IOwnCloudSyncService;
import de.luhmer.owncloudnewsreader.services.OwnCloudSyncService;

public class OwnCloudSyncAdapter extends AbstractThreadedSyncAdapter {
        
    public OwnCloudSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }
    
    ServiceConnection mConnection = null;
    
 
    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.d("udinic", "onPerformSync for account[" + account.name + "]");
        try {        	
        	
        	Intent serviceIntent = new Intent(getContext(), OwnCloudSyncService.class);
        	mConnection = generateServiceConnection();
        	getContext().bindService(serviceIntent, mConnection, Context.BIND_AUTO_CREATE);        	
        	//getContext().unbindService(mConnection);
	        
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private ServiceConnection generateServiceConnection() {
    	return new ServiceConnection() {
        	
        	public void onServiceConnected(ComponentName name, IBinder binder) {        	
        		IOwnCloudSyncService _ownCloadSyncService = IOwnCloudSyncService.Stub.asInterface(binder);
        		try {
        			_ownCloadSyncService.startSync();
        			getContext().unbindService(mConnection);
        			mConnection = null;
        		}
        		catch (Exception e) {
        			e.printStackTrace();
        		}
        	}

    		@Override
    		public void onServiceDisconnected(ComponentName name) {					
    		}
    	};
    }
}