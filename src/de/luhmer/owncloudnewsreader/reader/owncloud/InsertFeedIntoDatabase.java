package de.luhmer.owncloudnewsreader.reader.owncloud;

import java.util.ArrayList;

import org.json.JSONObject;

import de.luhmer.owncloudnewsreader.data.ConcreteFeedItem;
import de.luhmer.owncloudnewsreader.database.DatabaseConnection;
import de.luhmer.owncloudnewsreader.reader.InsertIntoDatabase;

public class InsertFeedIntoDatabase implements IHandleJsonObject{
	
	DatabaseConnection dbConn;
	ArrayList<ConcreteFeedItem> feeds = new ArrayList<ConcreteFeedItem>();
	
	public InsertFeedIntoDatabase(DatabaseConnection dbConn) {
		this.dbConn = dbConn;
	}
	
    private static ConcreteFeedItem parseFeed(JSONObject e)
	{
    	String faviconLink = e.optString("faviconLink");
        if(faviconLink != null)
            if(faviconLink.equals("null") || faviconLink.trim().equals(""))
                faviconLink = null;
        
        return new ConcreteFeedItem(e.optString("title"), e.optString("folderId"), e.optString("id"), faviconLink, -1);
	}

	@Override
	public void performAction(JSONObject jObj) {		
		ConcreteFeedItem rssFeed = parseFeed(jObj);
		feeds.add(rssFeed);
	}
	
	public void WriteAllToDatabaseNow() {
		InsertIntoDatabase.InsertSubscriptionsIntoDatabase(feeds, dbConn);
	}
}
