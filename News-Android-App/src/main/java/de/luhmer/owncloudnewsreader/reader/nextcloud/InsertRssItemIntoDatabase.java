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

package de.luhmer.owncloudnewsreader.reader.nextcloud;

import com.google.gson.JsonObject;

import java.util.Date;
import java.util.UUID;

import de.luhmer.owncloudnewsreader.database.model.RssItem;

public class InsertRssItemIntoDatabase {

    public static RssItem parseItem(JsonObject e) {
		Date pubDate = new Date(e.get("pubDate").getAsLong() * 1000);

        String content = e.get("body").getAsString();

        // Remove some ads
        content = content.replaceAll("<img[^>]*feedsportal.com.*>", "");
        content = content.replaceAll("<img[^>]*statisches.auslieferung.commindo-media-ressourcen.de.*>", "");
        content = content.replaceAll("<img[^>]*auslieferung.commindo-media-ressourcen.de.*>", "");
        content = content.replaceAll("<img[^>]*rss.buysellads.com.*>", "");



        String url = e.get("url").getAsString();
        String guid = e.get("guid").getAsString();
        String enclosureLink = getStringOrNull("enclosureLink", e);
        String enclosureMime = getStringOrNull("enclosureMime", e);

        if(enclosureLink.trim().equals("") && url.matches("^https?://(www.)?youtube.com/.*")) {
            enclosureLink = url;
            enclosureMime = "youtube";
        }

        RssItem rssItem = new RssItem();
        rssItem.setId(e.get("id").getAsLong());
        rssItem.setFeedId(e.get("feedId").getAsLong());
        rssItem.setGuid(guid);
        rssItem.setGuidHash(e.get("guidHash").getAsString());
        rssItem.setFingerprint(e.get("fingerprint").getAsString());
        rssItem.setBody(content);
        rssItem.setLastModified(new Date(e.get("lastModified").getAsLong()));
        rssItem.setRead(!e.get("unread").getAsBoolean());
        rssItem.setRead_temp(rssItem.getRead());
        rssItem.setStarred(e.get("starred").getAsBoolean());
        rssItem.setStarred_temp(rssItem.getStarred());
        rssItem.setPubDate(pubDate);

        //Possible XSS fields
        rssItem.setTitle(e.get("title").getAsString());
        rssItem.setAuthor(e.get("author").getAsString());
        rssItem.setLink(url);
        rssItem.setEnclosureLink(enclosureLink);
        rssItem.setEnclosureMime(enclosureMime);

        if(rssItem.getFingerprint() == null) {
            rssItem.setFingerprint(UUID.randomUUID().toString());
        }

        return rssItem;
	}

	private static String getStringOrNull(String key, JsonObject jObj) {
        if(jObj.has(key) && !jObj.get(key).isJsonNull()) {
            return jObj.get(key).getAsString();
        } else {
            return "";
        }
    }

}
