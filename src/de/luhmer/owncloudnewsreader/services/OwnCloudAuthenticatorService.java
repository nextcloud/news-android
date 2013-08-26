package de.luhmer.owncloudnewsreader.services;

import de.luhmer.owncloudnewsreader.authentication.OwnCloudAccountAuthenticator;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class OwnCloudAuthenticatorService extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        OwnCloudAccountAuthenticator authenticator = new OwnCloudAccountAuthenticator(this);
        return authenticator.getIBinder();
    }
}
