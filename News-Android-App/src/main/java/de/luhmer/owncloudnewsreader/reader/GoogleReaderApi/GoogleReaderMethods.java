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

package de.luhmer.owncloudnewsreader.reader.GoogleReaderApi;

import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import de.luhmer.owncloudnewsreader.Constants;
import de.luhmer.owncloudnewsreader.model.FolderSubscribtionItem;
import de.luhmer.owncloudnewsreader.model.RssFile;

public class GoogleReaderMethods {

	public static ArrayList<String[]> getTagList(String _USERNAME, String _PASSWORD) {
		Log.d(GoogleReaderConstants.APP_NAME, "METHOD: getTagList()");
		ArrayList<String[]> _TAGTITLE_ARRAYLIST = new ArrayList<String[]>();
		String _TAG_LABEL = null;
		try {
			_TAG_LABEL = "user/" + AuthenticationManager.getGoogleUserID(_USERNAME,_PASSWORD) + "/label/";
		} catch (IOException e) {
			e.printStackTrace();
		}

		Document doc = null;
		try {
			doc = Jsoup.connect(GoogleReaderConstants._TAG_LIST_URL)
				.header("Authorization", GoogleReaderConstants._AUTHPARAMS + AuthenticationManager.getGoogleAuthKey(_USERNAME,_PASSWORD))
				.userAgent(GoogleReaderConstants.APP_NAME)
				.timeout(6000)
				.get();
		} catch (IOException e) {
			e.printStackTrace();
		}

		Elements links = doc.select("string");
		for (Element link : links) {
			//String tagAttrib = link.attr("name");
			String tagText = link.text();
			if(Func_Strings.FindWordInString(tagText, _TAG_LABEL)) {
				_TAGTITLE_ARRAYLIST.add(new String[] { tagText.substring(32), tagText });
			}
		}

		//String[] _TAGTITLE_ARRAY = new String[_TAGTITLE_ARRAYLIST.size()];
		//_TAGTITLE_ARRAYLIST.toArray(_TAGTITLE_ARRAY);
		//return _TAGTITLE_ARRAY;
		return _TAGTITLE_ARRAYLIST;
	}

	@SuppressWarnings("unused")
	public static ArrayList<FolderSubscribtionItem> getSubList(String _USERNAME, String _PASSWORD) throws UnsupportedEncodingException, IOException {
		ArrayList<FolderSubscribtionItem> _SUBTITLE_ARRAYLIST = new ArrayList<FolderSubscribtionItem>();

		Document doc = Jsoup.connect(GoogleReaderConstants._SUBSCRIPTION_LIST_URL)
			.header("Authorization", GoogleReaderConstants._AUTHPARAMS + AuthenticationManager.getGoogleAuthKey(_USERNAME,_PASSWORD))
			.userAgent(GoogleReaderConstants.APP_NAME)
			.timeout(5000)
			.get();



		Elements objects = doc.select("object");
		Element element = objects.get(0);
		Node childTemp = element.childNodes().get(0);
		List<Node> childs = childTemp.childNodes();

		for (Node node : childs) {
			Elements links = ((Element) node).select("string");
			String idFeed = null;
			String feedName;
			String parentSubscriptionName;

			for (Element link : links) {
				String tagAttrib = link.attr("name");
				String tagText = link.text();
				if(tagAttrib.equals("id") && idFeed == null)
					idFeed = tagText;
				else if(tagAttrib.equals("title"))
					feedName = tagText;
				else if(tagAttrib.equals("label"))
					parentSubscriptionName = tagText;
			}

			//String idFeed = node.attr("id");
			//String name = node.attr("title");

			//_SUBTITLE_ARRAYLIST.add(new FolderSubscribtionItem(feedName, -1, idFeed, parentSubscriptionName));//TODO implements this again... ? Update FolderSubscribtionItem
		}


		//String[] _SUBTITLE_ARRAY = new String[_SUBTITLE_ARRAYLIST.size()];
		//_SUBTITLE_ARRAYLIST.toArray(_SUBTITLE_ARRAY);
		return _SUBTITLE_ARRAYLIST;
	}


	@SuppressWarnings("unused")
	public static ArrayList<RssFile> getFeeds(String _USERNAME, String _PASSWORD, String _TAG_LABEL) {
		Log.d("mygr","METHOD: getUnreadFeeds()");

		ArrayList<RssFile> items = new ArrayList<RssFile>();

		String returnString = null;

		/*
		String _TAG_LABEL = null;
		try {
			//_TAG_LABEL = "stream/contents/user/" + AuthenticationManager.getGoogleUserID(_USERNAME, _PASSWORD) + "/state/com.google/reading-list?n=1000&r=n&xt=user/-/state/com.google/read";
			_TAG_LABEL = "stream/contents/user/-/state/com.google/reading-list?n=1000&r=n&xt=user/-/state/com.google/read";
		}catch(Exception e){
			e.printStackTrace();
		}*/

		try{

			HttpClient client = new DefaultHttpClient();
			HttpGet request = new HttpGet(GoogleReaderConstants._API_URL + _TAG_LABEL);
			request.addHeader("Authorization", GoogleReaderConstants._AUTHPARAMS + AuthenticationManager.getGoogleAuthKey(_USERNAME, _PASSWORD));

			HttpResponse response = client.execute(request);

			returnString = HttpHelper.request(response);


			try {
				JSONObject jObj = new JSONObject(returnString);
				JSONArray jItems = (JSONArray) jObj.get("items");
				for(int i = 0; i < jItems.length(); i++) {
					JSONObject jItem = jItems.getJSONObject(i);

					try
					{
						String streamID = jItem.optJSONObject("origin").optString("streamId");
						String feedTitel = jItem.getString("title");
						String feedID = jItem.optString("id");
						String content = "";
						String link = "";
						String timestamp = jItem.optString("published");

						JSONObject jSummary = jItem.optJSONObject("summary");
						JSONObject jContent = jItem.optJSONObject("content");

						//JSONArray jCategories

						if(jSummary != null)
							content = (String) jItem.getJSONObject("summary").get("content");

						if(jContent != null)
							content = (String) jItem.getJSONObject("content").get("content");

						//if(jItem.has("origin"));
						//	link = (String) jItem.getJSONObject("origin").get("htmlUrl");

						if(jItem.has("alternate"));
							link = (String) jItem.optJSONArray("alternate").getJSONObject(0).getString("href");

						JSONArray jCategories = jItem.optJSONArray("categories");
						List<String> categories = new ArrayList<String>();
						if(jCategories != null)
						{
							for(int t = 0; t < jCategories.length(); t++)
								categories.add((String) jCategories.get(t));
						}

						Boolean starred = false;
						Boolean read = false;

						if(_TAG_LABEL.equals(Constants._TAG_LABEL_STARRED))
						{
							starred = true;
							read = true;
						}

						content = content.replaceAll("<img[^>]*feedsportal.com.*>", "");
						content = content.replaceAll("<img[^>]*statisches.auslieferung.commindo-media-ressourcen.de.*>", "");
						content = content.replaceAll("<img[^>]*auslieferung.commindo-media-ressourcen.de.*>", "");
						content = content.replaceAll("<img[^>]*rss.buysellads.com.*>", "");


						//content = (String) jItem.getJSONObject("content").get("content");
						/*
						RssFile sItem = new  RssFile(0, feedTitel, link, content, read, 0, feedID, categories, streamID, new Date(Long.parseLong(timestamp) * 1000), starred, null, null);//TODO implement this here again
						items.add(sItem);*/
					}
					catch(Exception ex)
					{
						ex.printStackTrace();
					}
				}
				//Log.d("HI", jObj.toString());
			} catch (JSONException e) {
				e.printStackTrace();
			}


			/*
			Pattern pattern = Pattern.compile("\"alternate\":\\[\\{\"href\":\"(.*?)\",");
			Matcher matcher = pattern.matcher(returnString);

			ArrayList<String> resultList = new ArrayList<String>();

			while (matcher.find())
				resultList.add(matcher.group(1));

			String[] ret = new String[resultList.size()];
			resultList.toArray(ret);*/
		    //return ret;

		}catch(IOException e){
			e.printStackTrace();
			return null;
		}
		return items;
	}




	public static Boolean performTagExecute(String _USERNAME, String _PASSWORD, List<NameValuePair> nameValuePairs)
	{
		Log.d("mygr","METHOD: performTagExecute");

		try{
			String authToken = AuthenticationManager.getGoogleToken(_USERNAME, _PASSWORD);
			String authKey = GoogleReaderConstants._AUTHPARAMS + AuthenticationManager.getGoogleAuthKey(_USERNAME, _PASSWORD);

			nameValuePairs.add(new BasicNameValuePair("T", authToken));

			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost(GoogleReaderConstants._EDIT_TAG_URL);
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			httppost.addHeader("Authorization", authKey);
			HttpResponse response = httpclient.execute(httppost);
			String returnString = HttpHelper.request(response);

			if(returnString.contains("OK"))
				return true;
			else
				return false;

			/*
			String authToken = AuthenticationManager.getGoogleToken(_USERNAME, _PASSWORD);
			String authKey = GoogleReaderConstants._AUTHPARAMS + AuthenticationManager.getGoogleAuthKey(_USERNAME, _PASSWORD);
			//String action = "user/" + AuthenticationManager.getGoogleUserID(_USERNAME, _PASSWORD) + "/state/com.google/read";

			ACTION = ACTION.replace("-", AuthenticationManager.getGoogleUserID(_USERNAME, _PASSWORD));

			Document doc = Jsoup.connect(TAG_URL)
					.header("Authorization", authKey)
					.data(
							"a", ACTION,
							//"a", "user/-/state/com.google/read",
							"async", "true",
							"s", SUBS_ID,
							"i", FEED_ID,
							"T", authToken)
	        //I also send my API key, but I don't think this is mandatory
			.userAgent(GoogleReaderConstants.APP_NAME)
			.timeout(10000)
	        // don't forget the post! (using get() will not work)
			.post();


			if(doc.body().text().equals("OK"))
				return true;
			else
				return false;
			*/
		} catch(IOException e){
			e.printStackTrace();
			return false;
		}
	}


}