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
	public void performAction(JSONObject jObj) {		
		RssFile rssFile = parseItem(jObj);
        InsertIntoDatabase.InsertSingleFeedItemIntoDatabase(rssFile, dbConn);
	}
}
