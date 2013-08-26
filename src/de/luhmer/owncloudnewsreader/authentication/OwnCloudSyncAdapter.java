package de.luhmer.owncloudnewsreader.authentication;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;

public class OwnCloudSyncAdapter extends AbstractThreadedSyncAdapter {
    //private final AccountManager mAccountManager;
    public static final String AUTHTOKEN_TYPE_FULL_ACCESS = "Full access";
    
    
    public OwnCloudSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        //mAccountManager = AccountManager.get(context);
    }
 
    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.d("udinic", "onPerformSync for account[" + account.name + "]");
        try {
            // Get the auth token for the current account
            //String authToken = mAccountManager.blockingGetAuthToken(account, AUTHTOKEN_TYPE_FULL_ACCESS, true);

        	
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}