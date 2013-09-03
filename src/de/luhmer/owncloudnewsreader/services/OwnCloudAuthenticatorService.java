package de.luhmer.owncloudnewsreader.services;

import de.luhmer.owncloudnewsreader.authentication.OwnCloudAccountAuthenticator;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class OwnCloudAuthenticatorService extends Service {
	
	// Instance field that stores the authenticator object
    private OwnCloudAccountAuthenticator mAuthenticator;
    
    @Override
    public void onCreate() {
        // Create a new authenticator object
        mAuthenticator = new OwnCloudAccountAuthenticator(this);
    }
    /*
     * When the system binds to this Service to make the RPC call
     * return the authenticator's IBinder.
     */
    @Override
    public IBinder onBind(Intent intent) {
        return mAuthenticator.getIBinder();
    }
}
