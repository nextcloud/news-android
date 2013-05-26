package de.luhmer.owncloudnewsreader.reader.owncloud;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import de.luhmer.owncloudnewsreader.data.ConcreteSubscribtionItem;
import de.luhmer.owncloudnewsreader.database.DatabaseConnection;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import de.luhmer.owncloudnewsreader.SettingsActivity;
import de.luhmer.owncloudnewsreader.data.FolderSubscribtionItem;
import de.luhmer.owncloudnewsreader.data.RssFile;
import de.luhmer.owncloudnewsreader.reader.FeedItemTags;
import de.luhmer.owncloudnewsreader.reader.FeedItemTags.TAGS;
import de.luhmer.owncloudnewsreader.reader.HttpJsonRequest;

public class OwnCloudReaderMethods {

	public static ArrayList<RssFile> GetFeeds(TAGS tag, Activity act) throws Exception
	{	
		ArrayList<RssFile> rssFiles = new ArrayList<RssFile>();
		
		List<NameValuePair> nVPairs = new ArrayList<NameValuePair>();
		nVPairs.add(new BasicNameValuePair("batchSize", "200"));
		if(tag.equals(TAGS.ALL_STARRED))
		{
			nVPairs.add(new BasicNameValuePair("type", "2"));
			nVPairs.add(new BasicNameValuePair("id", "0"));
		}
		else if(tag.equals(TAGS.ALL_UNREAD))
		{
			nVPairs.add(new BasicNameValuePair("type", "3"));
			nVPairs.add(new BasicNameValuePair("id", "0"));
		}
		nVPairs.add(new BasicNameValuePair("getRead", "false"));		 
		
		SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(act);
		String username = mPrefs.getString(SettingsActivity.EDT_USERNAME_STRING, null);
		String password = mPrefs.getString(SettingsActivity.EDT_PASSWORD_STRING, null);
		String oc_root_path = mPrefs.getString(SettingsActivity.EDT_OWNCLOUDROOTPATH_STRING, "");

        JSONObject jsonObj = HttpJsonRequest.PerformJsonRequest(oc_root_path + OwnCloudConstants.FEED_PATH + OwnCloudConstants.JSON_FORMAT, nVPairs, username, password);

        jsonObj = jsonObj.optJSONObject("ocs");
        jsonObj = jsonObj.optJSONObject("data");
        JSONArray jsonArr = jsonObj.optJSONArray("items");

        if(jsonArr != null)
        {
            for (int i = 0; i
                    < jsonArr.length(); i++) {
                JSONObject e = jsonArr.optJSONObject(i);

                Date date = new Date(e.optLong("pubDate") * 1000);

                String content = e.optString("body");
                content = content.replaceAll("<img[^>]*feedsportal.com.*>", "");
                content = content.replaceAll("<img[^>]*statisches.auslieferung.commindo-media-ressourcen.de.*>", "");
                content = content.replaceAll("<img[^>]*auslieferung.commindo-media-ressourcen.de.*>", "");
                content = content.replaceAll("<img[^>]*rss.buysellads.com.*>", "");

                rssFiles.add(new RssFile(0, e.optString("id"),
                                        e.optString("title"),
                                        e.optString("url"), content,
                                        !e.optBoolean("unread"), null,
                                        e.optString("feedId"), null,
                                        date, e.optBoolean("starred"),
                                        e.optString("guid"), e.optString("guidHash")));
            }
        }

		return rssFiles;
	}
	
	
	public static ArrayList<String[]> GetFolderTags(Activity act) throws Exception
	{	
		ArrayList<String[]> folderTags = null;
		
		SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(act);
		String username = mPrefs.getString(SettingsActivity.EDT_USERNAME_STRING, null);
		String password = mPrefs.getString(SettingsActivity.EDT_PASSWORD_STRING, null);
		String oc_root_path = mPrefs.getString(SettingsActivity.EDT_OWNCLOUDROOTPATH_STRING, "");
		
		JSONObject jsonObj = HttpJsonRequest.PerformJsonRequest(oc_root_path + OwnCloudConstants.FOLDER_PATH + OwnCloudConstants.JSON_FORMAT, null, username, password);

        jsonObj = jsonObj.optJSONObject("ocs");
        jsonObj = jsonObj.optJSONObject("data");
        JSONArray jsonArr = jsonObj.optJSONArray("folders");

        folderTags = new ArrayList<String[]>();
        for (int i = 0; i < jsonArr.length(); i++) {
            JSONObject e = jsonArr.optJSONObject(i);
            folderTags.add(new String[] { e.optString("name"), e.optString("id") });
        }
		
		return folderTags;
	}
	
	public static ArrayList<ConcreteSubscribtionItem> GetSubscriptionTags(Activity act) throws Exception
	{	
		ArrayList<ConcreteSubscribtionItem> subscriptionTags = new ArrayList<ConcreteSubscribtionItem>();
		
		SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(act);
		String username = mPrefs.getString(SettingsActivity.EDT_USERNAME_STRING, null);
		String password = mPrefs.getString(SettingsActivity.EDT_PASSWORD_STRING, null);
		String oc_root_path = mPrefs.getString(SettingsActivity.EDT_OWNCLOUDROOTPATH_STRING, "");
		
		JSONObject jsonObj = HttpJsonRequest.PerformJsonRequest(oc_root_path + OwnCloudConstants.SUBSCRIPTION_PATH + OwnCloudConstants.JSON_FORMAT, null, username, password);

        jsonObj = jsonObj.optJSONObject("ocs");
        jsonObj = jsonObj.optJSONObject("data");
        JSONArray jsonArr = jsonObj.optJSONArray("feeds");

        for (int i = 0; i < jsonArr.length(); i++) {
            JSONObject e = jsonArr.optJSONObject(i);
            String faviconLink = e.optString("faviconLink");
            if(faviconLink != null)
                if(faviconLink.equals("null"))
                    faviconLink = null;
            subscriptionTags.add(new ConcreteSubscribtionItem(e.optString("title"), e.optString("folderId"), e.optString("id"), faviconLink, -1));
        }

		return subscriptionTags;
	}
	
	public static boolean PerformTagExecution(String itemID, FeedItemTags.TAGS tag, Context context)
	{
		SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
		String username = mPrefs.getString(SettingsActivity.EDT_USERNAME_STRING, null);
		String password = mPrefs.getString(SettingsActivity.EDT_PASSWORD_STRING, null);
		String oc_root_path = mPrefs.getString(SettingsActivity.EDT_OWNCLOUDROOTPATH_STRING, "");
		
		String url = oc_root_path + OwnCloudConstants.FEED_PATH + "/";
		if(tag.equals(TAGS.MARK_ITEM_AS_READ))
			url += itemID + "/read";
		else if(tag.equals(TAGS.MARK_ITEM_AS_UNREAD))
			url += itemID + "/unread";
        else
        {
            DatabaseConnection dbConn = new DatabaseConnection(context);
            Cursor cursor = dbConn.getFeedByID(dbConn.getRowIdOfFeedByItemID(itemID));
            //Cursor cursor = dbConn.getFeedByID itemID);
            cursor.moveToFirst();

            String subscription_id = dbConn.getSubscriptionIdByRowID(cursor.getString(cursor.getColumnIndex(DatabaseConnection.RSS_ITEM_SUBSCRIPTION_ID)));
            url += subscription_id;

            String guidHash = cursor.getString(cursor.getColumnIndex(DatabaseConnection.RSS_ITEM_GUIDHASH));
            url += "/" + guidHash;

            if(tag.equals(TAGS.MARK_ITEM_AS_STARRED))
                url += "/star";
            else if(tag.equals(TAGS.MARK_ITEM_AS_UNSTARRED))
                url += "/unstar";

            cursor.close();
            dbConn.closeDatabase();
        }
        try
        {
		    int result = HttpJsonRequest.performTagChangeRequest(url, username, password);
		    if(result != -1 || result != 405)
    			return true;
    		else
    			return false;
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            return false;
        }
	}
}
