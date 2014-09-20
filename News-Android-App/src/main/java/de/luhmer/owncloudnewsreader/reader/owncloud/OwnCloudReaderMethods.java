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

package de.luhmer.owncloudnewsreader.reader.owncloud;

import android.content.Context;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.luhmer.owncloudnewsreader.database.DatabaseConnectionOrm;
import de.luhmer.owncloudnewsreader.database.model.RssItem;
import de.luhmer.owncloudnewsreader.reader.FeedItemTags;
import de.luhmer.owncloudnewsreader.reader.FeedItemTags.TAGS;
import de.luhmer.owncloudnewsreader.reader.HttpJsonRequest;
import de.luhmer.owncloudnewsreader.reader.owncloud.apiv1.APIv1;
import de.luhmer.owncloudnewsreader.reader.owncloud.apiv2.APIv2;

public class OwnCloudReaderMethods {
	//private static final String TAG = "OwnCloudReaderMethods";
	public static String maxSizePerSync = "300";

	public static int[] GetUpdatedItems(TAGS tag, Context cont, long lastSync, API api) throws Exception
	{
		List<NameValuePair> nVPairs = new ArrayList<NameValuePair>();
		//nVPairs.add(new BasicNameValuePair("batchSize", maxSizePerSync));
		if(tag.equals(TAGS.ALL_STARRED))
		{
			nVPairs.add(new BasicNameValuePair("type", "2"));
			nVPairs.add(new BasicNameValuePair("id", "0"));
		}
		else if(tag.equals(TAGS.ALL))
		{
			nVPairs.add(new BasicNameValuePair("type", "3"));
			nVPairs.add(new BasicNameValuePair("id", "0"));
		}
		nVPairs.add(new BasicNameValuePair("lastModified", String.valueOf(lastSync)));


    	InputStream is = HttpJsonRequest.PerformJsonRequest(api.getItemUpdatedUrl(), nVPairs, api.getUsername(), api.getPassword(), cont);

		DatabaseConnectionOrm dbConn = new DatabaseConnectionOrm(cont);
        try
        {
        	if(api instanceof APIv1)
    			return readJsonStreamV1(is, new InsertItemIntoDatabase(dbConn));
        	else if(api instanceof APIv2)
        		return readJsonStreamV2(is, new InsertItemIntoDatabase(dbConn));
        } finally {
        	//dbConn.closeDatabase();//TODO this line is needed
        	is.close();
        }
        return new int[] { 0, 0 };
	}

	//"type": 1, // the type of the query (Feed: 0, Folder: 1, Starred: 2, All: 3)
	public static int GetItems(TAGS tag, Context cont, String offset, boolean getRead, String id, String type, API api) throws Exception
	{
		List<NameValuePair> nVPairs = new ArrayList<NameValuePair>();
		nVPairs.add(new BasicNameValuePair("batchSize", maxSizePerSync));
		if(tag.equals(TAGS.ALL_STARRED))
		{
			nVPairs.add(new BasicNameValuePair("type", type));
			nVPairs.add(new BasicNameValuePair("id", id));
		}
		else if(tag.equals(TAGS.ALL))
		{
			nVPairs.add(new BasicNameValuePair("type", type));
			nVPairs.add(new BasicNameValuePair("id", id));
		}
		nVPairs.add(new BasicNameValuePair("offset", offset));
		if(getRead)
			nVPairs.add(new BasicNameValuePair("getRead", "true"));
		else
			nVPairs.add(new BasicNameValuePair("getRead", "false"));


		InputStream is = HttpJsonRequest.PerformJsonRequest(api.getItemUrl(), nVPairs, api.getUsername(), api.getPassword(), cont);

		DatabaseConnectionOrm dbConn = new DatabaseConnectionOrm(cont);
        try
        {
        	if(api instanceof APIv1)
    			return readJsonStreamV1(is, new InsertItemIntoDatabase(dbConn))[0];
        	else if(api instanceof APIv2)
        		return readJsonStreamV2(is, new InsertItemIntoDatabase(dbConn))[0];
        } finally {
            //dbConn.closeDatabase();//TODO this line is needed
        	is.close();
        }
        return 0;
	}


	public static int GetFolderTags(Context cont, API api) throws Exception
	{
		InputStream is = HttpJsonRequest.PerformJsonRequest(api.getFolderUrl(), null, api.getUsername(), api.getPassword(), cont);
        DatabaseConnectionOrm dbConn = new DatabaseConnectionOrm(cont);
		int[] result = new int[2];
		try
        {
			InsertFolderIntoDatabase ifid = new InsertFolderIntoDatabase(dbConn);

			if(api instanceof APIv1)
				result = readJsonStreamV1(is, ifid);
        	else if(api instanceof APIv2)
        		result = readJsonStreamV2(is, ifid);

			ifid.WriteAllToDatabaseNow();
        } finally {
            //dbConn.closeDatabase();//TODO this line is needed
        	is.close();
        }

		return result[0];
	}

	public static int[] GetFeeds(Context cont, API api) throws Exception
	{
		InputStream inputStream = HttpJsonRequest.PerformJsonRequest(api.getFeedUrl() , null, api.getUsername(), api.getPassword(), cont);

        DatabaseConnectionOrm dbConn = new DatabaseConnectionOrm(cont);
		int result[] = new int[2];
		try {
			InsertFeedIntoDatabase ifid = new InsertFeedIntoDatabase(dbConn);

			if(api instanceof APIv1)
				result = readJsonStreamV1(inputStream, ifid);
        	else if(api instanceof APIv2)
        		result = readJsonStreamV2(inputStream, ifid);

			ifid.WriteAllToDatabaseNow();
		} finally {
            //dbConn.closeDatabase();//TODO this line is needed
			inputStream.close();
		}
		return result;
	}

	/**
	 * can parse json like {"items":[{"id":6782}]}
	 * @param in
	 * @param iJoBj
	 * @return count all, count new items
	 * @throws IOException
	 * @throws JSONException
	 */
	public static int[] readJsonStreamV2(InputStream in, IHandleJsonObject iJoBj) throws IOException, JSONException {
        List<String> allowedArrays = Arrays.asList(new String[] { "feeds", "folders", "items" });

		int count = 0;
        int newItemsCount = 0;
        JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
        reader.beginObject();

        String currentName;
        while(reader.hasNext() && (currentName = reader.nextName()) != null) {
            if(allowedArrays.contains(currentName))
                break;
            else
                reader.skipValue();
        }

        reader.beginArray();
        while (reader.hasNext()) {
        	//reader.beginObject();

        	JSONObject e = getJSONObjectFromReader(reader);

        	if(iJoBj.performAction(e))
                newItemsCount++;
    		//reader.endObject();
    		count++;
        }

        if(iJoBj instanceof  InsertItemIntoDatabase)
            ((InsertItemIntoDatabase) iJoBj).performDatabaseBatchInsert(); //Save pending buffer

        //reader.endArray();
        //reader.endObject();
        reader.close();

        return new int[] { count, newItemsCount };
    }

	/**
	 * can parse json like {"items":[{"id":6782}]}
	 * @param in
	 * @param iJoBj
	 * @return
	 * @throws IOException
	 * @throws JSONException
	 */
	public static int[] readJsonStreamV1(InputStream in, IHandleJsonObject iJoBj) throws IOException, JSONException {
		int count = 0;
        int newItemsCount = 0;
        JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
        reader.beginObject();//{
        reader.nextName();//"ocs"
        reader.beginObject();//{
        reader.nextName();//meta

        getJSONObjectFromReader(reader);//skip status etc.

        reader.nextName();//data
        reader.beginObject();//{
        reader.nextName();//folders etc..

        reader.beginArray();
        while (reader.hasNext()) {
        	//reader.beginObject();

        	JSONObject e = getJSONObjectFromReader(reader);

        	if(iJoBj.performAction(e))
                newItemsCount++;

    		//reader.endObject();
    		count++;
        }

        if(iJoBj instanceof  InsertItemIntoDatabase)
            ((InsertItemIntoDatabase) iJoBj).performDatabaseBatchInsert(); //Save pending buffer

        //reader.endArray();
        //reader.endObject();
        reader.close();

        return new int[] { count, newItemsCount };
    }

	/**
	 * can read json like {"version":"1.101"}
	 * @param in
	 * @param iJoBj
	 * @return
	 * @throws IOException
	 * @throws JSONException
	 */
	private static int readJsonStreamSimple(InputStream in, IHandleJsonObject iJoBj) throws IOException, JSONException {
		int count = 0;
        JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
        //reader.setLenient(true);

        //JsonToken token = reader.peek();
        //while(token.equals(JsonToken.STRING))
        //	reader.skipValue();

        JSONObject e = getJSONObjectFromReader(reader);
        iJoBj.performAction(e);

        reader.close();

        return count;
    }

	private static JSONObject getJSONObjectFromReader(JsonReader jsonReader) {
		JSONObject jObj = new JSONObject();
		JsonToken tokenInstance;
		try {
			tokenInstance = jsonReader.peek();
			if(tokenInstance == JsonToken.BEGIN_OBJECT)
				jsonReader.beginObject();
			else if (tokenInstance == JsonToken.BEGIN_ARRAY)
				jsonReader.beginArray();

			while(jsonReader.hasNext()) {
				JsonToken token;
				String name;
				try {
					name = jsonReader.nextName();
					token = jsonReader.peek();

					//Log.d(TAG, token.toString());

					switch(token) {
						case NUMBER:
							jObj.put(name, jsonReader.nextLong());
							break;
						case NULL:
							jsonReader.skipValue();
							break;
						case BOOLEAN:
							jObj.put(name, jsonReader.nextBoolean());
							break;
						case BEGIN_OBJECT:
							//jsonReader.beginObject();
							jObj.put(name, getJSONObjectFromReader(jsonReader));
							//jsonReader.endObject();
							break;
						default:
							jObj.put(name, jsonReader.nextString());
					}
				} catch(Exception ex) {
					ex.printStackTrace();
					jsonReader.skipValue();
				}
			}

			if(tokenInstance == JsonToken.BEGIN_OBJECT)
				jsonReader.endObject();
			else if (tokenInstance == JsonToken.BEGIN_ARRAY)
				jsonReader.endArray();

			return jObj;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}



	public static boolean PerformTagExecutionAPIv2(List<String> itemIds, FeedItemTags.TAGS tag, Context context, API api)
	{
        String jsonIds;


		String url = api.getTagBaseUrl();
		if(tag.equals(TAGS.MARK_ITEM_AS_READ) || tag.equals(TAGS.MARK_ITEM_AS_UNREAD))
        {
			jsonIds = buildIdsToJSONArray(itemIds);

	        if(tag.equals(TAGS.MARK_ITEM_AS_READ))
	        	url += "read/multiple";
	        else
	        	url += "unread/multiple";
        } else {
            DatabaseConnectionOrm dbConn = new DatabaseConnectionOrm(context);

            HashMap<String, String> items = new HashMap<String, String>();
            for(String idItem : itemIds)
            {
	            RssItem rssItem = dbConn.getRssItemById(Long.parseLong(idItem));
	            items.put(rssItem.getGuidHash(), String.valueOf(rssItem.getFeedId()));
            }

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
		    int result = HttpJsonRequest.performTagChangeRequest(url, api.getUsername(), api.getPassword(), context, jsonIds);
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

	public static boolean PerformTagExecutionAPIv1(String itemId, FeedItemTags.TAGS tag, Context context, API api)
	{
		String url = api.getTagBaseUrl();
		if(tag.equals(TAGS.MARK_ITEM_AS_READ) || tag.equals(TAGS.MARK_ITEM_AS_UNREAD))
        {
			if(tag.equals(TAGS.MARK_ITEM_AS_READ))
				url += itemId + "/read";
			else
				url += itemId + "/unread";
        } else {
            DatabaseConnectionOrm dbConn = new DatabaseConnectionOrm(context);

            RssItem rssItem = dbConn.getRssItemById(Long.parseLong(itemId));

            url += rssItem.getFeedId();


            url += "/" + rssItem.getGuidHash();
            if(tag.equals(TAGS.MARK_ITEM_AS_STARRED))
                url += "/star";
            else if(tag.equals(TAGS.MARK_ITEM_AS_UNSTARRED))
                url += "/unstar";
        }
        try
        {
		    int result = HttpJsonRequest.performTagChangeRequest(url, api.getUsername(), api.getPassword(), context, null);
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


	public static String GetVersionNumber(Context cont, String username, String password, String oc_root_path) throws Exception
	{
		//Try APIv2
		try {
			String requestUrl = oc_root_path + OwnCloudConstants.ROOT_PATH_APIv2 + OwnCloudConstants.VERSION_PATH;
            requestUrl = API.validateURL(requestUrl);
			InputStream is = HttpJsonRequest.PerformJsonRequest(requestUrl, null, username, password, cont);
			try {
				GetVersion_v2 gv = new GetVersion_v2();
				readJsonStreamSimple(is, gv);
				return gv.getVersion();
			} finally {
				is.close();
			}
		}
		catch(AuthenticationException ex) {
			throw ex;
		} catch(Exception ex) {//TODO GET HERE THE RIGHT EXCEPTION
    		String requestUrl = oc_root_path + OwnCloudConstants.ROOT_PATH_APIv1 + OwnCloudConstants.VERSION_PATH + OwnCloudConstants.JSON_FORMAT;
            requestUrl = API.validateURL(requestUrl);
			InputStream is = HttpJsonRequest.PerformJsonRequest(requestUrl, null, username, password, cont);
			try {
				GetVersion_v1 gv = new GetVersion_v1();
				readJsonStreamSimple(is, gv);
				return gv.getVersion();
			} finally {
				is.close();
			}
		}
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
