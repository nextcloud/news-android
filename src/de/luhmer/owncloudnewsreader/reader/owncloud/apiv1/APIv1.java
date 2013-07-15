package de.luhmer.owncloudnewsreader.reader.owncloud.apiv1;

import android.app.Activity;
import de.luhmer.owncloudnewsreader.SettingsActivity;
import de.luhmer.owncloudnewsreader.reader.owncloud.API;
import de.luhmer.owncloudnewsreader.reader.owncloud.OwnCloudConstants;

public class APIv1 extends API {

	public APIv1(Activity act) {
		super(act);
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
	public void markSingleItemAsReadApiv1() {
		
	}
}
