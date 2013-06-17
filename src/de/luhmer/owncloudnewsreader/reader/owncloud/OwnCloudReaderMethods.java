package de.luhmer.owncloudnewsreader.reader.owncloud;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import de.luhmer.owncloudnewsreader.SettingsActivity;
import de.luhmer.owncloudnewsreader.data.ConcreteSubscribtionItem;
import de.luhmer.owncloudnewsreader.data.RssFile;
import de.luhmer.owncloudnewsreader.database.DatabaseConnection;
import de.luhmer.owncloudnewsreader.reader.FeedItemTags;
import de.luhmer.owncloudnewsreader.reader.FeedItemTags.TAGS;
import de.luhmer.owncloudnewsreader.reader.HttpJsonRequest;
import de.luhmer.owncloudnewsreader.reader.InsertIntoDatabase;

public class OwnCloudReaderMethods {
	public static String maxSizePerSync = "200";
	
	public static int GetUpdatedItems(TAGS tag, Activity act, long lastSync) throws Exception
	{	
		SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(act);		
		//ArrayList<RssFile> rssFiles = new ArrayList<RssFile>();
		
		List<NameValuePair> nVPairs = new ArrayList<NameValuePair>();
		nVPairs.add(new BasicNameValuePair("batchSize", maxSizePerSync));
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
		nVPairs.add(new BasicNameValuePair("lastModified", String.valueOf(lastSync)));
				
		String username = mPrefs.getString(SettingsActivity.EDT_USERNAME_STRING, null);
		String password = mPrefs.getString(SettingsActivity.EDT_PASSWORD_STRING, null);
		String oc_root_path = mPrefs.getString(SettingsActivity.EDT_OWNCLOUDROOTPATH_STRING, "");

		
		String requestURL = oc_root_path + OwnCloudConstants.FEED_PATH_UPDATED_ITEMS + OwnCloudConstants.JSON_FORMAT;		
			
        JSONObject jsonObj = HttpJsonRequest.PerformJsonRequest(requestURL, nVPairs, username, password, act);

        DatabaseConnection dbConn = new DatabaseConnection(act);
        try
        {
        	return parseItems(jsonObj, dbConn, act);
        } finally {
        	dbConn.closeDatabase();
        }
		//return rssFiles;
	}
	
	public static int GetItems(TAGS tag, Activity act, String offset, boolean getRead) throws Exception
	{	
		SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(act);		
		//ArrayList<RssFile> rssFiles = new ArrayList<RssFile>();
		
		List<NameValuePair> nVPairs = new ArrayList<NameValuePair>();
		nVPairs.add(new BasicNameValuePair("batchSize", maxSizePerSync));
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
		nVPairs.add(new BasicNameValuePair("offset", offset));
		if(getRead)
			nVPairs.add(new BasicNameValuePair("getRead", "true"));		 
		else
			nVPairs.add(new BasicNameValuePair("getRead", "false"));
		
		
		String username = mPrefs.getString(SettingsActivity.EDT_USERNAME_STRING, null);
		String password = mPrefs.getString(SettingsActivity.EDT_PASSWORD_STRING, null);
		String oc_root_path = mPrefs.getString(SettingsActivity.EDT_OWNCLOUDROOTPATH_STRING, "");

		String requestURL = oc_root_path + OwnCloudConstants.FEED_PATH + OwnCloudConstants.JSON_FORMAT;
			
        JSONObject jsonObj = HttpJsonRequest.PerformJsonRequest(requestURL, nVPairs, username, password, act);

        DatabaseConnection dbConn = new DatabaseConnection(act);
        try
        {
        	return parseItems(jsonObj, dbConn, act);
        } finally {
        	dbConn.closeDatabase();
        }
		//return rssFiles;
	}
	
	private static int parseItems(JSONObject jsonObj, DatabaseConnection dbConn, Context context)
	{
		//ArrayList<RssFile> rssFiles = new ArrayList<RssFile>();
		int count = 0;
		
		//jsonObj = jsonObj.optJSONObject("ocs");
        //jsonObj = jsonObj.optJSONObject("data");
        JSONArray jsonArr = jsonObj.optJSONArray("items");

        if(jsonArr != null)
        {
            for (int i = 0; i < jsonArr.length(); i++) {
                JSONObject e = jsonArr.optJSONObject(i);
                
                //rssFiles.add(parseItem(e));
                
                RssFile rssFile = parseItem(e);
                InsertIntoDatabase.InsertSingleFeedItemIntoDatabase(rssFile, dbConn);
                
                //new AsyncTask_DownloadImages(rssFile.getDescription(), context).execute();
                
                count++;
            }
        }
        
        return count;
        //return rssFiles;
	}
	
	private static RssFile parseItem(JSONObject e)
	{
		Date date = new Date(e.optLong("pubDate") * 1000);

        String content = e.optString("body");
        content = content.replaceAll("<img[^>]*feedsportal.com.*>", "");
        content = content.replaceAll("<img[^>]*statisches.auslieferung.commindo-media-ressourcen.de.*>", "");
        content = content.replaceAll("<img[^>]*auslieferung.commindo-media-ressourcen.de.*>", "");
        content = content.replaceAll("<img[^>]*rss.buysellads.com.*>", "");

        return new RssFile(0, e.optString("id"),
                                e.optString("title"),
                                e.optString("url"), content,
                                !e.optBoolean("unread"), null,
                                e.optString("feedId"), null,
                                date, e.optBoolean("starred"),
                                e.optString("guid"), e.optString("guidHash"),
                                e.optString("lastModified"));
	}
	
	
	public static ArrayList<String[]> GetFolderTags(Activity act) throws Exception
	{	
		ArrayList<String[]> folderTags = null;
		
		SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(act);
		String username = mPrefs.getString(SettingsActivity.EDT_USERNAME_STRING, null);
		String password = mPrefs.getString(SettingsActivity.EDT_PASSWORD_STRING, null);
		String oc_root_path = mPrefs.getString(SettingsActivity.EDT_OWNCLOUDROOTPATH_STRING, "");
		
		if(oc_root_path.endsWith("/"))
			oc_root_path = oc_root_path.substring(0, oc_root_path.length() - 1);
		
		String requestUrl = oc_root_path + OwnCloudConstants.FOLDER_PATH + OwnCloudConstants.JSON_FORMAT;
		JSONObject jsonObj = HttpJsonRequest.PerformJsonRequest(requestUrl, null, username, password, act);

        //jsonObj = jsonObj.optJSONObject("ocs");
        //jsonObj = jsonObj.optJSONObject("data");
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
		
		JSONObject jsonObj = HttpJsonRequest.PerformJsonRequest(oc_root_path + OwnCloudConstants.SUBSCRIPTION_PATH + OwnCloudConstants.JSON_FORMAT, null, username, password, act);

        //jsonObj = jsonObj.optJSONObject("ocs");
        //jsonObj = jsonObj.optJSONObject("data");
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
	
	public static boolean PerformTagExecution(List<String> itemIds, FeedItemTags.TAGS tag, Context context)
	{
		SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
		String username = mPrefs.getString(SettingsActivity.EDT_USERNAME_STRING, null);
		String password = mPrefs.getString(SettingsActivity.EDT_PASSWORD_STRING, null);
		String oc_root_path = mPrefs.getString(SettingsActivity.EDT_OWNCLOUDROOTPATH_STRING, "");


        //List<NameValuePair> nameValuePairs = null;        
        String jsonIds = null;
        
        
		String url = oc_root_path + OwnCloudConstants.FEED_PATH + "/";
		if(tag.equals(TAGS.MARK_ITEM_AS_READ) || tag.equals(TAGS.MARK_ITEM_AS_UNREAD))
        {
			jsonIds = buildIdsToJSONArray(itemIds);
			/*
	        if(jsonIds != null)
	        {
	            nameValuePairs = new ArrayList<NameValuePair>();
	            nameValuePairs.add(new BasicNameValuePair("itemIds", jsonIds));
	        }*/
			//url += itemIds.get(0) + "/read";
	        
	        if(tag.equals(TAGS.MARK_ITEM_AS_READ))
	        	url += "read/multiple";
	        else
	        	url += "unread/multiple";
        }
		//else if(tag.equals(TAGS.MARK_ITEM_AS_UNREAD))
		//	url += itemIds.get(0) + "/unread";//TODO HERE...
        else
        {
            DatabaseConnection dbConn = new DatabaseConnection(context);
            
            HashMap<String, String> items = new HashMap<String, String>();
            for(String idItem : itemIds)
            {
	            Cursor cursor = dbConn.getArticleByID(dbConn.getRowIdOfFeedByItemID(idItem));
	            //Cursor cursor = dbConn.getFeedByID itemID);
	            cursor.moveToFirst();
	
	            String idSubscription = cursor.getString(cursor.getColumnIndex(DatabaseConnection.RSS_ITEM_SUBSCRIPTION_ID));
	            String guidHash = cursor.getString(cursor.getColumnIndex(DatabaseConnection.RSS_ITEM_GUIDHASH));
	            
	            cursor.close();
	            
	            String subscription_id = dbConn.getSubscriptionIdByRowID(idSubscription);
	            //url += subscription_id;
	            
	            items.put(guidHash, subscription_id);
            }
            dbConn.closeDatabase();
            
            jsonIds = buildIdsToJSONArrayWithGuid(items);
            /*
	        if(jsonIds != null)
	        {
	            nameValuePairs = new ArrayList<NameValuePair>();
	            nameValuePairs.add(new BasicNameValuePair("itemIds", jsonIds));
	        }*/
            
            if(tag.equals(TAGS.MARK_ITEM_AS_STARRED))
                url += "star/multiple";
            else if(tag.equals(TAGS.MARK_ITEM_AS_UNSTARRED))
                url += "unstar/multiple";
            
            
            /*
            url += "/" + guidHash;

            if(tag.equals(TAGS.MARK_ITEM_AS_STARRED))
                url += "/star";
            else if(tag.equals(TAGS.MARK_ITEM_AS_UNSTARRED))
                url += "/unstar";
            */
            
        }
        try
        {
		    int result = HttpJsonRequest.performTagChangeRequest(url, username, password, context, jsonIds);
		    //if(result != -1 || result != 405)
		    if(result == 200)
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

	
	public static String GetVersionNumber(Activity act, String username, String password, String oc_root_path) throws Exception
	{	
		if(oc_root_path.endsWith("/"))
			oc_root_path = oc_root_path.substring(0, oc_root_path.length() - 1);
		
		String requestUrl = oc_root_path + OwnCloudConstants.VERSION_PATH;
		JSONObject jsonObj = HttpJsonRequest.PerformJsonRequest(requestUrl, null, username, password, act);

		return jsonObj.optString("version");
	}
	
	
    private static String buildIdsToJSONArray(List<String> ids)
    {
        try
        {
            JSONArray jArr = new JSONArray();
            for(String id : ids)
                jArr.put(Integer.parseInt(id));

            
            JSONObject jObj = new JSONObject();
            jObj.put("items", jArr);
            
            return jObj.toString();
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
        return null;
    }
    
    private static String buildIdsToJSONArrayWithGuid(HashMap<String, String> items)
    {
        try
        {
            JSONArray jArr = new JSONArray();
            for(Map.Entry<String, String> entry : items.entrySet())
            {
            	JSONObject jOb = new JSONObject();
            	jOb.put("feedId", Integer.parseInt(entry.getValue()));
            	jOb.put("guidHash", entry.getKey());
            	jArr.put(jOb);
            }

            
            JSONObject jObj = new JSONObject();
            jObj.put("items", jArr);
            
            return jObj.toString();
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
        return null;
    }
}
