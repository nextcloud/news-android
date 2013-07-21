package de.luhmer.owncloudnewsreader.reader.owncloud;

import java.util.ArrayList;

import org.json.JSONObject;

import de.luhmer.owncloudnewsreader.database.DatabaseConnection;
import de.luhmer.owncloudnewsreader.reader.InsertIntoDatabase;

public class InsertFolderIntoDatabase implements IHandleJsonObject{
	
	DatabaseConnection dbConn;
	ArrayList<String[]> folders = new ArrayList<String[]>();
	
	public InsertFolderIntoDatabase(DatabaseConnection dbConn) {
		this.dbConn = dbConn;
	}
	
    private static String[] parseFolder(JSONObject e)
	{
    	return new String[] { e.optString("name"), e.optString("id") };
	}

	@Override
	public void performAction(JSONObject jObj) {		
		folders.add(parseFolder(jObj));
	}
	
	public void WriteAllToDatabaseNow() {
		InsertIntoDatabase.InsertFoldersIntoDatabase(folders, dbConn);
	}
}
