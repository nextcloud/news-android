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

import java.util.Date;

import org.json.JSONObject;

import de.luhmer.owncloudnewsreader.data.RssFile;
import de.luhmer.owncloudnewsreader.database.DatabaseConnection;
import de.luhmer.owncloudnewsreader.reader.InsertIntoDatabase;

public class InsertItemIntoDatabase implements IHandleJsonObject {
	
	DatabaseConnection dbConn;
	
	public InsertItemIntoDatabase(DatabaseConnection dbConn) {
		this.dbConn = dbConn;
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
                                e.optString("lastModified"),
                                e.optString("author"));
	}

	@Override
	public boolean performAction(JSONObject jObj) {
		RssFile rssFile = parseItem(jObj);
        return InsertIntoDatabase.InsertSingleFeedItemIntoDatabase(rssFile, dbConn);
	}
}
