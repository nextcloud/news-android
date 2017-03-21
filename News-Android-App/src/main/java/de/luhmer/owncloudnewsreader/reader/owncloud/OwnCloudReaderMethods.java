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

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.luhmer.owncloudnewsreader.database.DatabaseConnectionOrm;
import de.luhmer.owncloudnewsreader.database.model.RssItem;
import de.luhmer.owncloudnewsreader.reader.FeedItemTags;
import de.luhmer.owncloudnewsreader.reader.HttpJsonRequest;
import de.luhmer.owncloudnewsreader.reader.owncloud.apiv1.APIv1;
import de.luhmer.owncloudnewsreader.reader.owncloud.apiv2.APIv2;
import okhttp3.HttpUrl;

public class OwnCloudReaderMethods {

	@SuppressWarnings("unused")
	private static final String TAG = "OwnCloudReaderMethods";
	public static String maxSizePerSync = "300";

	public static int[] GetUpdatedItems(FeedItemTags tag, Context cont, long lastSync, API api) throws Exception
	{
		HttpUrl.Builder getItemUpdatedUrlBuilder = api.getItemUpdatedUrl().newBuilder();
		if(tag.equals(FeedItemTags.ALL_STARRED) || tag.equals(FeedItemTags.ALL))
		{
			getItemUpdatedUrlBuilder.addQueryParameter("type", tag.toString())
					.addQueryParameter("id", "0");
		}

		getItemUpdatedUrlBuilder.addQueryParameter("lastModified", String.valueOf(lastSync));

    	InputStream is = HttpJsonRequest.getInstance().PerformJsonRequest(getItemUpdatedUrlBuilder.build());

		DatabaseConnectionOrm dbConn = new DatabaseConnectionOrm(cont);
        try
        {
        	if(api instanceof APIv1)
    			return readJsonStreamV1(is, new InsertItemIntoDatabase(dbConn));
        	else if(api instanceof APIv2)
        		return readJsonStreamV2(is, new InsertItemIntoDatabase(dbConn));
        } finally {
        	is.close();
        }
        return new int[] { 0, 0 };
	}

	//"type": 1, // the type of the query (Feed: 0, Folder: 1, Starred: 2, All: 3)
	public static int GetItems(FeedItemTags tag, Context cont, String offset, boolean getRead, String id, String type, API api) throws Exception
	{
		HttpUrl.Builder getItemsUrlBuilder = api.getItemUrl().newBuilder();

		getItemsUrlBuilder.addQueryParameter("batchSize", maxSizePerSync)
				.addQueryParameter("offset", offset)
				.addQueryParameter("getRead", String.valueOf(getRead));

		if(tag.equals(FeedItemTags.ALL_STARRED) ||tag.equals(FeedItemTags.ALL))
		{
			getItemsUrlBuilder.addQueryParameter("type",type)
					.addQueryParameter("id", id);
		}

		InputStream is = HttpJsonRequest.getInstance().PerformJsonRequest(getItemsUrlBuilder.build());

		DatabaseConnectionOrm dbConn = new DatabaseConnectionOrm(cont);
        try
        {
        	if(api instanceof APIv1)
    			return readJsonStreamV1(is, new InsertItemIntoDatabase(dbConn))[0];
        	else if(api instanceof APIv2)
        		return readJsonStreamV2(is, new InsertItemIntoDatabase(dbConn))[0];
        } finally {
        	is.close();
        }
        return 0;
	}


	public static int GetFolderTags(Context cont, API api) throws Exception
	{
		InputStream is = HttpJsonRequest.getInstance().PerformJsonRequest(api.getFolderUrl());
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
        	is.close();
        }

		return result[0];
	}

	public static int[] GetFeeds(Context cont, API api) throws Exception
	{
		InputStream inputStream = HttpJsonRequest.getInstance().PerformJsonRequest(api.getFeedUrl());

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
	 */
	public static int[] readJsonStreamV2(InputStream in, IHandleJsonObject iJoBj) throws IOException {
        List<String> allowedArrays = Arrays.asList("feeds", "folders", "items");

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
        	JSONObject e = getJSONObjectFromReader(reader);

        	if(iJoBj.performAction(e))
                newItemsCount++;

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
	 * @return new int[] { count, newItemsCount }
	 * @throws IOException
	 */
	public static int[] readJsonStreamV1(InputStream in, IHandleJsonObject iJoBj) throws IOException {
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
	 */
	private static int readJsonStreamSimple(InputStream in, IHandleJsonObject iJoBj) throws IOException {
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
                        case BEGIN_ARRAY:
                            jsonReader.skipValue();
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



	public static boolean PerformTagExecutionAPIv2(List<String> itemIds, FeedItemTags tag, Context context, API api)
	{
        String jsonIds;

		HttpUrl.Builder urlBuilder = api.getTagBaseUrl().newBuilder();
		if(tag.equals(FeedItemTags.MARK_ITEM_AS_READ) || tag.equals(FeedItemTags.MARK_ITEM_AS_UNREAD))
        {
			jsonIds = buildIdsToJSONArray(itemIds);

        	urlBuilder.addPathSegment(tag.toString());
        } else {
            DatabaseConnectionOrm dbConn = new DatabaseConnectionOrm(context);

            HashMap<String, String> items = new HashMap<>();
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
	            nameValuePairs.put("itemIds", jsonIds));
	        }*/

            if(tag.equals(FeedItemTags.MARK_ITEM_AS_STARRED) || tag.equals(FeedItemTags.MARK_ITEM_AS_UNSTARRED))
				urlBuilder.addPathSegment(tag.toString());

            /*
            url += "/" + guidHash;

            if(tag.equals(TAGS.MARK_ITEM_AS_STARRED))
                url += "/star";
            else if(tag.equals(TAGS.MARK_ITEM_AS_UNSTARRED))
                url += "/unstar";
            */

        }

		urlBuilder.addPathSegment("multiple");

        try
        {
		    int result = HttpJsonRequest.getInstance().performTagChangeRequest(urlBuilder.build(), jsonIds);
		    //if(result != -1 || result != 405)
			return (result == 200);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            return false;
        }
	}

	public static boolean PerformTagExecutionAPIv1(String itemId, FeedItemTags tag, Context context, API api)
	{
		HttpUrl.Builder urlBuilder = api.getTagBaseUrl().newBuilder();
		if(tag.equals(FeedItemTags.MARK_ITEM_AS_READ) || tag.equals(FeedItemTags.MARK_ITEM_AS_UNREAD)) {
			urlBuilder
					.addPathSegment(itemId)
					.addPathSegment(tag.toString());
        } else {
            DatabaseConnectionOrm dbConn = new DatabaseConnectionOrm(context);

            RssItem rssItem = dbConn.getRssItemById(Long.parseLong(itemId));

            urlBuilder.addPathSegment(String.valueOf(rssItem.getFeedId()));

            urlBuilder.addPathSegment(rssItem.getGuidHash());

            if(tag.equals(FeedItemTags.MARK_ITEM_AS_STARRED) || tag.equals(FeedItemTags.MARK_ITEM_AS_UNSTARRED))
                urlBuilder.addPathSegment(tag.toString());
        }
        try
        {
		    int result = HttpJsonRequest.getInstance().performTagChangeRequest(urlBuilder.build(), null);
			return (result == 200);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            return false;
        }
	}

	public static String GetVersionNumber(HttpUrl basePath) throws Exception
	{
		//Try APIv2
		try {
            HttpUrl requestUrl = basePath.resolve(OwnCloudConstants.ROOT_PATH_APIv2).newBuilder()
					.addPathSegment(OwnCloudConstants.VERSION_PATH)
                    .build();

            InputStream is = HttpJsonRequest.getInstance().PerformJsonRequest(requestUrl);

			try {
				GetVersion_v2 gv = new GetVersion_v2();
				readJsonStreamSimple(is, gv);
				return gv.getVersion();
			} finally {
				is.close();
			}
		} catch(Exception ex) {
			HttpUrl requestUrl = basePath.resolve(OwnCloudConstants.ROOT_PATH_APIv1).newBuilder()
					.addPathSegment(OwnCloudConstants.VERSION_PATH)
					.addQueryParameter("format", "json")
					.build();

			InputStream is = HttpJsonRequest.getInstance().PerformJsonRequest(requestUrl);

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
