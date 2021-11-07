/*
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
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;

import java.io.IOException;

import javax.inject.Inject;

import de.luhmer.owncloudnewsreader.NewsReaderApplication;
import de.luhmer.owncloudnewsreader.database.DatabaseConnectionOrm;
import de.luhmer.owncloudnewsreader.di.ApiProvider;
import de.luhmer.owncloudnewsreader.reader.nextcloud.ItemStateSync;

public class SyncItemStateService extends JobIntentService {

	/**
	 * Unique job/channel ID for this service.
	 */
	private static final int JOB_ID = 1001;
    private static final String TAG = SyncItemStateService.class.getCanonicalName();

    protected @Inject ApiProvider mApi;

	/**
	 * Convenience method for enqueuing work in to this service.
	 */
	public static void enqueueWork(Context context, Intent work) {
		enqueueWork(context, SyncItemStateService.class, JOB_ID, work);
	}

	@Override
	public void onCreate() {
		((NewsReaderApplication) getApplication()).getAppComponent().injectService(this);
		super.onCreate();
	}

	@Override
	protected void onHandleWork(@NonNull Intent intent) {
		if(mApi.getNewsAPI() == null) {
			Log.w(TAG, "API is not initialized");
			return;
		}

        final DatabaseConnectionOrm dbConn = new DatabaseConnectionOrm(this);

        try {
			ItemStateSync.PerformItemStateSync(mApi.getNewsAPI(), dbConn);
			Log.v(TAG, "SyncItemStateService finished.");
		} catch (IOException e) {
			Log.e(TAG, "SyncItemState failed:" + e.toString());
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
