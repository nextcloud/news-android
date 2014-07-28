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

import java.util.ArrayList;

import de.luhmer.owncloudnewsreader.database.DatabaseConnectionOrm;
import de.luhmer.owncloudnewsreader.database.model.Feed;
import de.luhmer.owncloudnewsreader.reader.InsertIntoDatabase;

public class InsertFeedIntoDatabase implements IHandleJsonObject{

	DatabaseConnectionOrm dbConn;
	ArrayList<Feed> feeds = new ArrayList<Feed>();

	public InsertFeedIntoDatabase(DatabaseConnectionOrm dbConn) {
		this.dbConn = dbConn;
	}

    private static Feed parseFeed(JSONObject e)
	{
    	String faviconLink = e.optString("faviconLink");
        if(faviconLink != null)
            if(faviconLink.equals("null") || faviconLink.trim().equals(""))
                faviconLink = null;

        Feed feed = new Feed();
        feed.setId(e.optLong("id"));
        feed.setFeedTitle(e.optString("title"));
        feed.setFolderId(e.optLong("folderId"));
        feed.setFaviconUrl(faviconLink);

        return feed;
	}

	@Override
	public boolean performAction(JSONObject jObj) {
        Feed rssFeed = parseFeed(jObj);
		feeds.add(rssFeed);
        return true;
	}

	public void WriteAllToDatabaseNow() {
		InsertIntoDatabase.InsertFeedsIntoDatabase(feeds, dbConn);
	}
}
