package de.luhmer.owncloudnewsreader.ListView;

import android.content.Context;
import de.luhmer.owncloudnewsreader.async_tasks.IGetTextForTextViewAsyncTask;
import de.luhmer.owncloudnewsreader.database.DatabaseConnection;

public class UnreadFeedCount implements IGetTextForTextViewAsyncTask {

	Context context;
	String idDatabase;
	boolean execludeStarredItems;
	
	public UnreadFeedCount(Context context, String idDatabase, boolean execludeStarredItems) { 
		this.context = context;
		this.idDatabase = idDatabase;
		this.execludeStarredItems = execludeStarredItems;
	}
	
	@Override
	public String getText() {
		DatabaseConnection dbConn = new DatabaseConnection(context);
		int unread = 0;
		try
		{
			unread = dbConn.getCountItemsForSubscription(idDatabase, true, execludeStarredItems);
		} finally {
			dbConn.closeDatabase();
		}
        
        return String.valueOf(unread);
	}
}
