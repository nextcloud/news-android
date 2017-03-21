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

import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.luhmer.owncloudnewsreader.reader.FeedItemTags;
import de.luhmer.owncloudnewsreader.reader.owncloud.apiv2.APIv2;
import okhttp3.HttpUrl;

public abstract class API {
	private HttpUrl baseUrl;

	public API(HttpUrl baseUrl) {
		this.baseUrl = baseUrl;
	}

	/**
	 * @param appVersion e.g. "6.0.4".
	 * @return e.g. [0] = 6, [1] = 0, [2] = 4
	 */
	public static int[] ExtractVersionNumberFromString(String appVersion) {
		Pattern p = Pattern.compile("(\\d+).(\\d+).(\\d+)");
		Matcher m = p.matcher(appVersion);

		int version[] = new int[] { 0, 0, 0 };
		if (m.matches()) {
			version[0] = Integer.parseInt(m.group(1));
			version[1] = Integer.parseInt(m.group(2));
			version[2] = Integer.parseInt(m.group(3));
		}
		return version;
	}

	public static API GetRightApiForVersion(String appVersion, HttpUrl baseUrl) {
		API api;
		int[] version = ExtractVersionNumberFromString(appVersion);

        //TODO do some version checks here (when API v2.0 gets released)
        api = new APIv2(baseUrl);

		return api;
	}

	public abstract HttpUrl getItemUrl();
	public abstract HttpUrl getItemUpdatedUrl();
	public abstract HttpUrl getFeedUrl();
	public abstract HttpUrl getFolderUrl();
    public abstract HttpUrl getUserUrl();
	public abstract HttpUrl getTagBaseUrl();

	protected HttpUrl getAPIUrl(String format, String... urlSegments) {
        String url = StringUtils.join(urlSegments, "/");
		HttpUrl.Builder apiUrlBuilder = baseUrl.resolve(url).newBuilder();

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
}