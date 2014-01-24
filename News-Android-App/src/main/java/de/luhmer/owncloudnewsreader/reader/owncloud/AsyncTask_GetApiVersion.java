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

package de.luhmer.owncloudnewsreader.reader.owncloud;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import de.luhmer.owncloudnewsreader.SettingsActivity;
import de.luhmer.owncloudnewsreader.reader.AsyncTask_Reader;
import de.luhmer.owncloudnewsreader.reader.OnAsyncTaskCompletedListener;

public class AsyncTask_GetApiVersion extends AsyncTask_Reader {
	    
    private String username;
    private String password;
    
    public AsyncTask_GetApiVersion(final int task_id, final Context context, String username, String password, final OnAsyncTaskCompletedListener[] listener) {
          super(task_id, context, listener);

          this.username = username;
          this.password = password;
    }
    
    @Override
    protected Object doInBackground(Object... params) {
        try {
        	SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
    		String oc_root_path = mPrefs.getString(SettingsActivity.EDT_OWNCLOUDROOTPATH_STRING, "");
        	return OwnCloudReaderMethods.GetVersionNumber(context, username, password, oc_root_path);
        } catch (Exception ex) {
            return ex;
        }
    }
}

