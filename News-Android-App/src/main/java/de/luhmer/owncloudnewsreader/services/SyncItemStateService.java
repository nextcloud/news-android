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
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import de.luhmer.owncloudnewsreader.Constants;
import de.luhmer.owncloudnewsreader.SettingsActivity;
import de.luhmer.owncloudnewsreader.reader.IReader;
import de.luhmer.owncloudnewsreader.reader.OnAsyncTaskCompletedListener;
import de.luhmer.owncloudnewsreader.reader.owncloud.API;
import de.luhmer.owncloudnewsreader.reader.owncloud.OwnCloud_Reader;
import de.luhmer.owncloudnewsreader.reader.owncloud.apiv1.APIv1;
import de.luhmer.owncloudnewsreader.reader.owncloud.apiv2.APIv2;

public class SyncItemStateService extends IntentService {

	IReader _Reader = new OwnCloud_Reader();
	
	public SyncItemStateService() {
		super(null);
	}	
	
	public SyncItemStateService(String name) {
		super(name);
	}	
	
	@Override
	protected void onHandleIntent(Intent intent) {
		OwnCloud_Reader ocReader = (OwnCloud_Reader) _Reader;
		SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		String username = mPrefs.getString(SettingsActivity.EDT_USERNAME_STRING, "");
		String password = mPrefs.getString(SettingsActivity.EDT_PASSWORD_STRING, "");
		ocReader.Start_AsyncTask_GetVersion(Constants.TaskID_GetVersion, this, onAsyncTask_GetVersionFinished, username, password);
    }

	OnAsyncTaskCompletedListener onAsyncTask_GetVersionFinished = new OnAsyncTaskCompletedListener() {

		@Override
		public void onAsyncTaskCompleted(int task_id, Object task_result) {
			
			if(!(task_result instanceof Exception))
			{	
				API api = null;
				String appVersion = task_result.toString();
				int versionCode = 0;
				if(appVersion != null)
				{
					appVersion = appVersion.replace(".", "");
					versionCode = Integer.parseInt(appVersion);
				}
				if (versionCode >= 1101) {
					api = new APIv2(SyncItemStateService.this);
				} else {
					api = new APIv1(SyncItemStateService.this);
				}
				
				((OwnCloud_Reader)_Reader).setApi(api);
				
				_Reader.Start_AsyncTask_PerformItemStateChange(Constants.TaskID_PerformStateChange, SyncItemStateService.this, null);
			}
		}
	};
	
	
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
