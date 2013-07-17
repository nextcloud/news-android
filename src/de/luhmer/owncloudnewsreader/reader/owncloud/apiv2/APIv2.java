package de.luhmer.owncloudnewsreader.reader.owncloud.apiv2;

import java.util.List;

import android.content.Context;
import de.luhmer.owncloudnewsreader.SettingsActivity;
import de.luhmer.owncloudnewsreader.reader.FeedItemTags.TAGS;
import de.luhmer.owncloudnewsreader.reader.owncloud.API;
import de.luhmer.owncloudnewsreader.reader.owncloud.OwnCloudConstants;
import de.luhmer.owncloudnewsreader.reader.owncloud.OwnCloudReaderMethods;

public class APIv2 extends API {
	
	public APIv2(Context cont) {
		super(cont);
	}

	@Override
	public String getItemUrl() {
		return getOcRootPath() + OwnCloudConstants.ROOT_PATH_APIv2 + OwnCloudConstants.FEED_PATH + OwnCloudConstants.JSON_FORMAT;
	}
	
	@Override
	public String getItemUpdatedUrl() {
		return getOcRootPath() + OwnCloudConstants.ROOT_PATH_APIv2 + OwnCloudConstants.FEED_PATH_UPDATED_ITEMS + OwnCloudConstants.JSON_FORMAT;
	}

	@Override
	public String getFeedUrl() {		
		return getOcRootPath() + OwnCloudConstants.ROOT_PATH_APIv2 + OwnCloudConstants.SUBSCRIPTION_PATH + OwnCloudConstants.JSON_FORMAT;
	}

	@Override
	public String getFolderUrl() {
		return getOcRootPath() + OwnCloudConstants.ROOT_PATH_APIv2 + OwnCloudConstants.FOLDER_PATH + OwnCloudConstants.JSON_FORMAT;
	}
	
	@Override
	public String getTagBaseUrl() {
		return getOcRootPath() + OwnCloudConstants.ROOT_PATH_APIv2 + OwnCloudConstants.FEED_PATH + "/";
	}

	private String getOcRootPath() {
		String oc_root_path = super.mPrefs.getString(SettingsActivity.EDT_OWNCLOUDROOTPATH_STRING, "");
		
		if(oc_root_path.endsWith("/"))
			oc_root_path = oc_root_path.substring(0, oc_root_path.length() - 1);
		
		return oc_root_path;
	}

	@Override
	public boolean PerformTagExecution(List<String> itemIds, TAGS tag,
			Context context, API api) {
		if(itemIds.size() > 0)
			return OwnCloudReaderMethods.PerformTagExecutionAPIv2(itemIds, tag, context, api);
		else
			return true;
	}
}
