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

import java.util.List;
import java.util.regex.Pattern;

import de.luhmer.owncloudnewsreader.SettingsActivity;
import de.luhmer.owncloudnewsreader.reader.FeedItemTags;
import de.luhmer.owncloudnewsreader.reader.FeedItemTags.TAGS;
import de.luhmer.owncloudnewsreader.reader.owncloud.apiv1.APIv1;
import de.luhmer.owncloudnewsreader.reader.owncloud.apiv2.APIv2;

public abstract class API {
	protected SharedPreferences mPrefs;
	//static final Pattern RemoveAllDoubleSlashes = Pattern.compile("[^:](\\/\\/)");
	static final Pattern RemoveAllDoubleSlashes = Pattern.compile("(?<!:)\\/\\/");


	public API(Context cont) {
		mPrefs = PreferenceManager.getDefaultSharedPreferences(cont);
	}

	public static API GetRightApiForVersion(String appVersion, Context context) {
		API api;
        int majorVersion = 0;
		int minorVersion = 0;
		if(appVersion != null)
		{
            majorVersion = Integer.parseInt(appVersion.substring(0,1));
            appVersion = appVersion.substring(2);

            appVersion = appVersion.replace(".", "");
            minorVersion = Integer.parseInt(appVersion);
		}

        switch (majorVersion) {
            case 1:
                if (minorVersion >= 101) {
                    api = new APIv2(context);
                } else {
                    api = new APIv1(context);
                }
                break;
            case 2:
                api = new APIv2(context);
                break;
            case 3:
                api = new APIv2(context);
                break;
            case 4:
                api = new APIv2(context);
                break;
            default:
                //Api is not known. Fallback to APIv2
                api = new APIv2(context);
                break;
        }

		return api;
	}

	protected abstract String getItemUrl();
	protected abstract String getItemUpdatedUrl();
	public abstract String getFeedUrl();
	protected abstract String getFolderUrl();

	protected abstract String getTagBaseUrl();

	/**
	 *
	 * @return http(s)://url_to_server
	 */
	protected String getOcRootPath() {
		String oc_root_path = mPrefs.getString(SettingsActivity.EDT_OWNCLOUDROOTPATH_STRING, "");
		oc_root_path = RemoveAllDoubleSlashes.matcher(oc_root_path).replaceAll("/");

		//if(!oc_root_path.endsWith("/"))
		//	oc_root_path += "/";
		//while(oc_root_path.endsWith("/"))
		//	oc_root_path += oc_root_path.substring(0, oc_root_path.length() - 2);

		return oc_root_path;
	}


	public String getUsername() {
		return mPrefs.getString(SettingsActivity.EDT_USERNAME_STRING, null);
	}

	public String getPassword() {
		return mPrefs.getString(SettingsActivity.EDT_PASSWORD_STRING, null);
	}

	public int[] GetFeeds(Context cont, API api) throws Exception {
		return OwnCloudReaderMethods.GetFeeds(cont, api);
	}

	public int GetFolderTags(Context cont, API api) throws Exception {
		return OwnCloudReaderMethods.GetFolderTags(cont, api);
	}

	public int GetItems(TAGS tag, Context cont, String offset, boolean getRead, int id, String type, API api) throws Exception {
		return OwnCloudReaderMethods.GetItems(tag, cont, offset, getRead, String.valueOf(id), type, api);
	}

	public int[] GetUpdatedItems(TAGS tag, Context cont, long lastSync, API api) throws Exception {
		return OwnCloudReaderMethods.GetUpdatedItems(tag, cont, lastSync, api);
	}

	public static String validateURL(String url) {
		return RemoveAllDoubleSlashes.matcher(url).replaceAll("/");
	}

	public abstract boolean PerformTagExecution(List<String> itemIds, FeedItemTags.TAGS tag, Context context, API api);
}