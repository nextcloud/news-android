package de.luhmer.owncloudnewsreader.authentication;

import de.luhmer.owncloudnewsreader.helper.AidlException;
import de.luhmer.owncloudnewsreader.services.IOwnCloudSyncService;
import de.luhmer.owncloudnewsreader.services.IOwnCloudSyncServiceCallback;
import de.luhmer.owncloudnewsreader.services.OwnCloudSyncService;
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
import android.os.RemoteException;
import android.util.Log;

public class OwnCloudSyncAdapter extends AbstractThreadedSyncAdapter {
    //private final AccountManager mAccountManager;
    public static final String AUTHTOKEN_TYPE_FULL_ACCESS = "Full access";
    
    
    IOwnCloudSyncService _ownCloadSyncService;	
	private IOwnCloudSyncServiceCallback callback = new IOwnCloudSyncServiceCallback.Stub() {

		@Override
		public void startedSyncOfItemStates() throws RemoteException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void finishedSyncOfItemStates() throws RemoteException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void startedSyncOfFolders() throws RemoteException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void finishedSyncOfFolders() throws RemoteException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void startedSyncOfFeeds() throws RemoteException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void finishedSyncOfFeeds() throws RemoteException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void startedSyncOfItems() throws RemoteException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void finishedSyncOfItems() throws RemoteException {
			syncFinished = true;
			
		}

		@Override
		public void throwException(AidlException ex) throws RemoteException {
			// TODO Auto-generated method stub
			
		}
	
	};
	
	boolean syncFinished = true;
    
    
    public OwnCloudSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        //mAccountManager = AccountManager.get(context);
    }
 
    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.d("udinic", "onPerformSync for account[" + account.name + "]");
        try {        	
        	if(syncFinished) {
        		syncFinished = false;
        		
	        	Intent serviceIntent = new Intent(getContext(), OwnCloudSyncService.class);	        
	        	getContext().bindService(serviceIntent, new ServiceConnection() {
		        	
		        	public void onServiceConnected(ComponentName name, IBinder binder) {        	
		        		_ownCloadSyncService = IOwnCloudSyncService.Stub.asInterface(binder);
		        		try {
		        			_ownCloadSyncService.registerCallback(callback);
		        		}
		        		catch (Exception e) {
		        			e.printStackTrace();
		        		}
		        	}
		        	
		        	public void onServiceDisconnected(ComponentName name) {
		        		try {
		        			_ownCloadSyncService.unregisterCallback(callback);
		        		}
		        		catch (Exception e) { 
		        			e.printStackTrace();
		        		}
		        	}
					
				}, Context.BIND_AUTO_CREATE);        	
	        	
	        	_ownCloadSyncService.startSync();
        	}
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}