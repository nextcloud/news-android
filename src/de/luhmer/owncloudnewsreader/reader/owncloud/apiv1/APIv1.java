package de.luhmer.owncloudnewsreader.reader.owncloud.apiv1;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import de.luhmer.owncloudnewsreader.SettingsActivity;
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

	private String getOcRootPath() {
		String oc_root_path = super.mPrefs.getString(SettingsActivity.EDT_OWNCLOUDROOTPATH_STRING, "");
		
		if(oc_root_path.endsWith("/"))
			oc_root_path = oc_root_path.substring(0, oc_root_path.length() - 1);
		
		return oc_root_path;
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
