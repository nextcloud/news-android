package de.luhmer.owncloudnewsreader.reader.owncloud;

import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import de.luhmer.owncloudnewsreader.SettingsActivity;
import de.luhmer.owncloudnewsreader.reader.FeedItemTags;
import de.luhmer.owncloudnewsreader.reader.FeedItemTags.TAGS;

public abstract class API {
	protected SharedPreferences mPrefs;
	
	public API(Context cont) {
		mPrefs = PreferenceManager.getDefaultSharedPreferences(cont);
	}
	
	protected abstract String getItemUrl();
	protected abstract String getItemUpdatedUrl();
	protected abstract String getFeedUrl();
	protected abstract String getFolderUrl();
	
	protected abstract String getTagBaseUrl();
	
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