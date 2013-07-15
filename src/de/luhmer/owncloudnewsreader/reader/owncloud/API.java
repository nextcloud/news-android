package de.luhmer.owncloudnewsreader.reader.owncloud;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import de.luhmer.owncloudnewsreader.SettingsActivity;
import de.luhmer.owncloudnewsreader.reader.FeedItemTags;
import de.luhmer.owncloudnewsreader.reader.FeedItemTags.TAGS;

public abstract class API {
	protected SharedPreferences mPrefs;
	
	public API(Activity act) {
		mPrefs = PreferenceManager.getDefaultSharedPreferences(act);
	}
	
	protected abstract String getItemUrl();
	protected abstract String getItemUpdatedUrl();
	protected abstract String getFeedUrl();
	protected abstract String getFolderUrl();
	
	protected abstract String getTagBaseUrl();
	
	//public abstract void markSingleItemAsReadApiv1();
	
	public String getUsername() {
		return mPrefs.getString(SettingsActivity.EDT_USERNAME_STRING, null);
	}
	
	public String getPassword() {
		return mPrefs.getString(SettingsActivity.EDT_PASSWORD_STRING, null);
	}
	
	public int GetFeeds(Activity act, API api) throws Exception {
		return OwnCloudReaderMethods.GetFeeds(act, api);
	}
		
	public int GetFolderTags(Activity act, API api) throws Exception {
		return OwnCloudReaderMethods.GetFolderTags(act, api);
	}
	
	public int GetItems(TAGS tag, Activity act, String offset, boolean getRead, String id, String type, API api) throws Exception {
		return OwnCloudReaderMethods.GetItems(tag, act, offset, getRead, id, type, api);
	}
	
	public int GetUpdatedItems(TAGS tag, Activity act, long lastSync, API api) throws Exception {
		return OwnCloudReaderMethods.GetUpdatedItems(tag, act, lastSync, api);
	}
	
	public abstract boolean PerformTagExecution(List<String> itemIds, FeedItemTags.TAGS tag, Context context, API api);
	/*
	public boolean PerformTagExecution(List<String> itemIds, FeedItemTags.TAGS tag, Context context, API api) {
		return OwnCloudReaderMethods.PerformTagExecution(itemIds, tag, context, api);
	}*/
}