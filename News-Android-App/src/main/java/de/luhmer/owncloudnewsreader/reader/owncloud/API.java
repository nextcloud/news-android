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
import android.util.Log;

import com.squareup.okhttp.HttpUrl;

import org.apache.commons.lang3.StringUtils;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import de.luhmer.owncloudnewsreader.SettingsActivity;
import de.luhmer.owncloudnewsreader.reader.FeedItemTags;
import de.luhmer.owncloudnewsreader.reader.owncloud.apiv1.APIv1;
import de.luhmer.owncloudnewsreader.reader.owncloud.apiv2.APIv2;

public abstract class API {
	private String baseUrl;

	public API(String baseUrl) {
        if(!baseUrl.endsWith("/"))
            baseUrl = baseUrl + "/";

		this.baseUrl = baseUrl;
	}

	public static API GetRightApiForVersion(String appVersion, String baseUrl) {
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
                    api = new APIv2(baseUrl);
                } else {
                    api = new APIv1(baseUrl);
                }
                break;
            case 2:
                api = new APIv2(baseUrl);
                break;
            case 3:
                api = new APIv2(baseUrl);
                break;
            case 4:
                api = new APIv2(baseUrl);
                break;
            default:
                //Api is not known. Fallback to APIv2
                api = new APIv2(baseUrl);
                break;
        }

		return api;
	}

	public abstract HttpUrl getItemUrl();
	public abstract HttpUrl getItemUpdatedUrl();
	public abstract HttpUrl getFeedUrl();
	public abstract HttpUrl getFolderUrl();

	public abstract HttpUrl getTagBaseUrl();

	protected HttpUrl getAPIUrl(String format, String... urlSegments) {
		HttpUrl basePath = HttpUrl.parse(baseUrl);

        String url = "." + StringUtils.join(urlSegments, "/");
		HttpUrl.Builder apiUrlBuilder = basePath.resolve(url).newBuilder();

		if(format != null)
			apiUrlBuilder.addQueryParameter("format", format);

		return apiUrlBuilder.build();
	}

	public int[] GetFeeds(Context cont) throws Exception {
		return OwnCloudReaderMethods.GetFeeds(cont, this);
	}

	public int GetFolderTags(Context cont) throws Exception {
		return OwnCloudReaderMethods.GetFolderTags(cont, this);
	}

	public int GetItems(FeedItemTags tag, Context cont, String offset, boolean getRead, int id, String type) throws Exception {
		return OwnCloudReaderMethods.GetItems(tag, cont, offset, getRead, String.valueOf(id), type, this);
	}

	public int[] GetUpdatedItems(FeedItemTags tag, Context cont, long lastSync) throws Exception {
		return OwnCloudReaderMethods.GetUpdatedItems(tag, cont, lastSync, this);
	}

	public abstract boolean PerformTagExecution(List<String> itemIds, FeedItemTags tag, Context context);


    /*
    static final Pattern RemoveAllDoubleSlashes = Pattern.compile("(?<!:)\\/\\/");
    public static String validateURL(String url) {
        return RemoveAllDoubleSlashes.matcher(url).replaceAll("/");
    }   */
}