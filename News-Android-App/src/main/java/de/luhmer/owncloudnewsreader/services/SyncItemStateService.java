/**
* Android ownCloud News
*
* @author David Luhmer
* @copyright 2013 David Luhmer david-dev@live.de
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU AFFERO GENERAL PUBLIC LICENSE
* License as published by the Free Software Foundation; either
* version 3 of the License, or any later version.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU AFFERO GENERAL PUBLIC LICENSE for more details.
*
* You should have received a copy of the GNU Affero General Public
* License along with this library.  If not, see <http://www.gnu.org/licenses/>.
*
*/

package de.luhmer.owncloudnewsreader.services;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import java.io.IOException;

import javax.inject.Inject;

import de.luhmer.owncloudnewsreader.NewsReaderApplication;
import de.luhmer.owncloudnewsreader.database.DatabaseConnectionOrm;
import de.luhmer.owncloudnewsreader.di.ApiProvider;
import de.luhmer.owncloudnewsreader.reader.nextcloud.ItemStateSync;

public class SyncItemStateService extends IntentService {

	@Inject ApiProvider mApi;

	public SyncItemStateService() {
		super(null);
	}	
	
	public SyncItemStateService(String name) {
		super(name);
	}

	@Override
	public void onCreate() {
		((NewsReaderApplication) getApplication()).getAppComponent().injectService(this);
		super.onCreate();
	}

	@Override
	protected void onHandleIntent(Intent intent) {
        final DatabaseConnectionOrm dbConn = new DatabaseConnectionOrm(this);

        try {
            ItemStateSync.PerformItemStateSync(mApi.getAPI(), dbConn);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

	public static boolean isMyServiceRunning(Context context) {
	    ActivityManager manager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
	    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
	        if ("de.luhmer.owncloudnewsreader.services.SyncItemStateService".equals(service.service.getClassName())) {
	            return true;
	        }
	    }
	    return false;
	}
}
