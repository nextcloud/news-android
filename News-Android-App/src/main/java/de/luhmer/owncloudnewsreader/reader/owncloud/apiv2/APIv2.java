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

package de.luhmer.owncloudnewsreader.reader.owncloud.apiv2;

import android.content.Context;

import java.util.List;

import de.luhmer.owncloudnewsreader.reader.FeedItemTags;
import de.luhmer.owncloudnewsreader.reader.owncloud.API;
import de.luhmer.owncloudnewsreader.reader.owncloud.OwnCloudConstants;
import de.luhmer.owncloudnewsreader.reader.owncloud.OwnCloudReaderMethods;
import okhttp3.HttpUrl;

public class APIv2 extends API {
	
	public APIv2(HttpUrl baseUrl) {
		super(baseUrl);
	}

	@Override
	public HttpUrl getItemUrl() {
		return getAPIUrl(OwnCloudConstants.JSON_FORMAT, OwnCloudConstants.ROOT_PATH_APIv2, OwnCloudConstants.FEED_PATH);
	}
	
	@Override
	public HttpUrl getItemUpdatedUrl() {
		return getAPIUrl(OwnCloudConstants.JSON_FORMAT, OwnCloudConstants.ROOT_PATH_APIv2, OwnCloudConstants.FEED_PATH_UPDATED_ITEMS);
	}

	@Override
	public HttpUrl getFeedUrl() {
		return getAPIUrl(OwnCloudConstants.JSON_FORMAT, OwnCloudConstants.ROOT_PATH_APIv2, OwnCloudConstants.SUBSCRIPTION_PATH);
	}

	@Override
	public HttpUrl getFolderUrl() {
		return getAPIUrl(OwnCloudConstants.JSON_FORMAT, OwnCloudConstants.ROOT_PATH_APIv2, OwnCloudConstants.FOLDER_PATH);
	}
	
	@Override
	public HttpUrl getTagBaseUrl() {
		return getAPIUrl(null, OwnCloudConstants.ROOT_PATH_APIv2, OwnCloudConstants.FEED_PATH);
	}

	@Override
	public HttpUrl getUserUrl() {
		return getAPIUrl(null, OwnCloudConstants.ROOT_PATH_APIv2, OwnCloudConstants.USER_PATH);
	}
	
	@Override
	public boolean PerformTagExecution(List<String> itemIds, FeedItemTags tag,
			Context context) {
		if(itemIds.size() > 0)
			return OwnCloudReaderMethods.PerformTagExecutionAPIv2(itemIds, tag, context, this);
		else
			return true;
	}
}
