package de.luhmer.owncloudnewsreader.ListView;

import android.content.Context;
import de.luhmer.owncloudnewsreader.async_tasks.IGetTextForTextViewAsyncTask;
import de.luhmer.owncloudnewsreader.database.DatabaseConnection;

public class UnreadFolderCount implements IGetTextForTextViewAsyncTask {

	Context context;
	String idDatabase;
	
	public UnreadFolderCount(Context context, String idDatabase) { 
		this.context = context;
		this.idDatabase = idDatabase;
	}
	
	@Override
	public String getText() {
		DatabaseConnection dbConn = new DatabaseConnection(context);
		int unread = 0;
		try
		{
			unread = dbConn.getCountFeedsForFolder(idDatabase, true);
		} finally {
			dbConn.closeDatabase();
		}
        
        return String.valueOf(unread);
	}
}
