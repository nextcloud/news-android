package de.luhmer.owncloudnewsreader.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import de.luhmer.owncloudnewsreader.authentication.OwnCloudAccountAuthenticator

class OwnCloudAuthenticatorService : Service() {
    // Instance field that stores the authenticator object
    private var mAuthenticator: OwnCloudAccountAuthenticator? = null

    override fun onCreate() {
        // Create a new authenticator object
        mAuthenticator = OwnCloudAccountAuthenticator(this)
    }

    /*
     * When the system binds to this Service to make the RPC call
     * return the authenticator's IBinder.
     */
    override fun onBind(intent: Intent): IBinder? = mAuthenticator?.iBinder
}
