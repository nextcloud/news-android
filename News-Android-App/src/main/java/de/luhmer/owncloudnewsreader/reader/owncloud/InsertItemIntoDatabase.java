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

import org.json.JSONObject;

import java.util.Date;

import de.luhmer.owncloudnewsreader.data.RssFile;
import de.luhmer.owncloudnewsreader.database.DatabaseConnection;

public class InsertItemIntoDatabase implements IHandleJsonObject {

	DatabaseConnection dbConn;
    RssFile[] buffer;
    static final short bufferSize = 200;
    int index = 0;
    SparseArray<Integer> feedIds;

	public InsertItemIntoDatabase(DatabaseConnection dbConn) {
		this.dbConn = dbConn;
        buffer = new RssFile[bufferSize];

        feedIds = dbConn.getFeedIds();
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
                                e.optString("guid"),
                                e.optString("guidHash"),
                                e.optString("lastModified"),
                                e.optString("author"));
	}

	@Override
	public boolean performAction(JSONObject jObj) {
        boolean result = false;

		RssFile rssFile = parseItem(jObj);
        buffer[index] = rssFile;
        index++;

        String FeedId_Db = feedIds.get(rssFile.getFeedID());
        if(FeedId_Db != null) {
            rssFile.setFeedID_Db(FeedId_Db);
            result = !rssFile.getRead();
        }

        if(index >= bufferSize) {
            performDatabaseBatchInsert();
        }

        return result;
    }


    public boolean performDatabaseBatchInsert() {
        if(index > 0) {
            dbConn.insertNewItems(buffer);
            index = 0;
            buffer = new RssFile[bufferSize];
        }

        return true;
    }
}
