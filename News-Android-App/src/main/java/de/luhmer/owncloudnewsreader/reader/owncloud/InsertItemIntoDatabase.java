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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.luhmer.owncloudnewsreader.database.DatabaseConnectionOrm;
import de.luhmer.owncloudnewsreader.database.model.RssItem;

public class InsertItemIntoDatabase implements IHandleJsonObject {

	DatabaseConnectionOrm dbConn;
    List<RssItem> buffer;
    static final short bufferSize = 200;
    int index = 0;
    //List<Feed> feeds;

	public InsertItemIntoDatabase(DatabaseConnectionOrm dbConn) {
		this.dbConn = dbConn;
        //buffer = new RssItem[bufferSize];
        buffer = new ArrayList<RssItem>(bufferSize);

        //feeds = dbConn.getListOfFeeds();
	}

    private static RssItem parseItem(JSONObject e) throws JSONException {
		Date pubDate = new Date(e.optLong("pubDate") * 1000);

        String content = e.optString("body");
        content = content.replaceAll("<img[^>]*feedsportal.com.*>", "");
        content = content.replaceAll("<img[^>]*statisches.auslieferung.commindo-media-ressourcen.de.*>", "");
        content = content.replaceAll("<img[^>]*auslieferung.commindo-media-ressourcen.de.*>", "");
        content = content.replaceAll("<img[^>]*rss.buysellads.com.*>", "");

        String url = e.optString("url");
        String guid = e.optString("guid");
        String enclosureLink = e.optString("enclosureLink");
        String enclosureMime = e.optString("enclosureMime");

        if(enclosureLink.trim().equals("") && guid.startsWith("http://gdata.youtube.com/feeds/api/")) {
            enclosureLink = url;
            enclosureMime = "youtube";
        }

        RssItem rssItem = new RssItem();
        rssItem.setId(e.getLong("id"));
        rssItem.setFeedId(e.optLong("feedId"));
        rssItem.setLink(url);
        rssItem.setTitle(e.optString("title"));
        rssItem.setGuid(guid);
        rssItem.setGuidHash(e.optString("guidHash"));
        rssItem.setBody(content);
        rssItem.setAuthor(e.optString("author"));
        rssItem.setLastModified(new Date(e.optLong("lastModified")));
        rssItem.setEnclosureLink(enclosureLink);
        rssItem.setEnclosureMime(enclosureMime);
        rssItem.setRead(!e.optBoolean("unread"));
        rssItem.setRead_temp(rssItem.getRead());
        rssItem.setStarred(e.optBoolean("starred"));
        rssItem.setStarred_temp(rssItem.getStarred());
        rssItem.setPubDate(pubDate);

        return rssItem;
        /*
        new RssItem(0, e.optString("id"),
                                e.optString("title"),
                                url, content,
                                !e.optBoolean("unread"), null,
                                e.optString("feedId"), null,
                                date, e.optBoolean("starred"),
                                guid,
                                e.optString("guidHash"),
                                e.optString("lastModified"),
                                e.optString("author"),
                                enclosureLink,
                                enclosureMime);
                                */
	}

	@Override
	public boolean performAction(JSONObject jObj) {
        boolean result = false;

        try {
            RssItem rssFile = parseItem(jObj);
            //buffer[index] = rssFile;
            buffer.add(rssFile);
            index++;


            if (rssFile != null)
                result = !rssFile.getRead();

            //if (index >= bufferSize) {
            if (buffer.size() >= bufferSize) {
                performDatabaseBatchInsert();
            }
        } catch(Exception ex) {
            ex.printStackTrace();
        }

        return result;
    }


    public boolean performDatabaseBatchInsert() {
        if(index > 0) {
            //dbConn.insertNewItems(buffer);
            dbConn.insertNewItems(buffer.toArray(new RssItem[buffer.size()]));

            index = 0;
            //buffer = new RssItem[bufferSize];
            buffer = new ArrayList<RssItem>(bufferSize);
        }

        return true;
    }
}
