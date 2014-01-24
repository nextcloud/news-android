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

package de.luhmer.owncloudnewsreader.reader.owncloud.apiv1;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import de.luhmer.owncloudnewsreader.reader.FeedItemTags.TAGS;
import de.luhmer.owncloudnewsreader.reader.owncloud.API;
import de.luhmer.owncloudnewsreader.reader.owncloud.OwnCloudConstants;
import de.luhmer.owncloudnewsreader.reader.owncloud.OwnCloudReaderMethods;

public class APIv1 extends API {

	public APIv1(Context cont) {
		super(cont);
	}

	@Override
	public String getItemUrl() {
		return getOcRootPath() + OwnCloudConstants.ROOT_PATH_APIv1 + OwnCloudConstants.FEED_PATH + OwnCloudConstants.JSON_FORMAT;
	}
	
	@Override
	public String getItemUpdatedUrl() {
		return getOcRootPath() + OwnCloudConstants.ROOT_PATH_APIv1 + OwnCloudConstants.FEED_PATH_UPDATED_ITEMS + OwnCloudConstants.JSON_FORMAT;
	}

	@Override
	public String getFeedUrl() {		
		return getOcRootPath() + OwnCloudConstants.ROOT_PATH_APIv1 + OwnCloudConstants.SUBSCRIPTION_PATH + OwnCloudConstants.JSON_FORMAT;
	}

	@Override
	public String getFolderUrl() {
		return getOcRootPath() + OwnCloudConstants.ROOT_PATH_APIv1 + OwnCloudConstants.FOLDER_PATH + OwnCloudConstants.JSON_FORMAT;
	}
	
	@Override
	public String getTagBaseUrl() {
		return getOcRootPath() + OwnCloudConstants.ROOT_PATH_APIv1 + OwnCloudConstants.FEED_PATH + "/";
	}

	@Override
	public boolean PerformTagExecution(List<String> itemIds, TAGS tag,
			Context context, API api) {
		
		List<Boolean> succeeded = new ArrayList<Boolean>();
		for(String item : itemIds) {
			succeeded.add(OwnCloudReaderMethods.PerformTagExecutionAPIv1(item, tag, context, api));
		}
		
		if(succeeded.contains(false))
			return false;
		else
			return true;
	}
}
