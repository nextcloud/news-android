package de.luhmer.owncloudnewsreader.reader.owncloud;

import java.util.List;
import java.util.regex.Pattern;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import de.luhmer.owncloudnewsreader.SettingsActivity;
import de.luhmer.owncloudnewsreader.reader.FeedItemTags;
import de.luhmer.owncloudnewsreader.reader.FeedItemTags.TAGS;
import de.luhmer.owncloudnewsreader.reader.owncloud.apiv1.APIv1;
import de.luhmer.owncloudnewsreader.reader.owncloud.apiv2.APIv2;

public abstract class API {
	protected SharedPreferences mPrefs;
	Pattern RemoveAllDoubleSlashes = Pattern.compile("[^:](\\/\\/)");
	
	public API(Context cont) {
		mPrefs = PreferenceManager.getDefaultSharedPreferences(cont);
	}
	
	public static API GetRightApiForVersion(String appVersion, Activity activity) {
		API api = null;
		int versionCode = 0;
		if(appVersion != null)
		{
			appVersion = appVersion.replace(".", "");
			versionCode = Integer.parseInt(appVersion);
		}
		if (versionCode >= 1101) {
			api = new APIv2(activity);
		} else {
			api = new APIv1(activity);
		}
		return api;
	}
	
	protected abstract String getItemUrl();
	protected abstract String getItemUpdatedUrl();
	protected abstract String getFeedUrl();
	protected abstract String getFolderUrl();	
	
	protected abstract String getTagBaseUrl();
	
	/**
	 * 
	 * @return http(s)://url_to_server/
	 */
	protected String getOcRootPath() {
		String oc_root_path = mPrefs.getString(SettingsActivity.EDT_OWNCLOUDROOTPATH_STRING, "");
		oc_root_path = RemoveAllDoubleSlashes.matcher(oc_root_path).replaceAll("/");
		
		if(!oc_root_path.endsWith("/"))
			oc_root_path += "/";
		
		return oc_root_path;
	}
	
	
	public String getUsername() {
		return mPrefs.getString(SettingsActivity.EDT_USERNAME_STRING, null);
	}
	
	public String getPassword() {
		return mPrefs.getString(SettingsActivity.EDT_PASSWORD_STRING, null);
	}
	
	public int GetFeeds(Context cont, API api) throws Exception {
		return OwnCloudReaderMethods.GetFeeds(cont, api);
	}
		
	public int GetFolderTags(Context cont, API api) throws Exception {
		return OwnCloudReaderMethods.GetFolderTags(cont, api);
	}
	
	public int GetItems(TAGS tag, Context cont, String offset, boolean getRead, String id, String type, API api) throws Exception {
		return OwnCloudReaderMethods.GetItems(tag, cont, offset, getRead, id, type, api);
	}
	
	public int GetUpdatedItems(TAGS tag, Context cont, long lastSync, API api) throws Exception {
		return OwnCloudReaderMethods.GetUpdatedItems(tag, cont, lastSync, api);
	}
	
	public abstract boolean PerformTagExecution(List<String> itemIds, FeedItemTags.TAGS tag, Context context, API api);
}