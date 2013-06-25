package de.luhmer.owncloudnewsreader.helper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import android.annotation.SuppressLint;
import android.content.Context;
import de.luhmer.owncloudnewsreader.database.DatabaseHelper;

public class DatabaseUtils {
	
	public static boolean CopyDatabaseToSdCard(Context context)
	{
		//context.getPackageCodePath()//Path to apk file..!
		//String path = "/data/data/de.luhmer.owncloudnewsreader/databases/" + DatabaseHelper.DATABASE_NAME;
		String path = context.getDatabasePath(DatabaseHelper.DATABASE_NAME).getPath();
		
	    File db = new File(path);
	    File backupDb = new File(ImageHandler.getPath(context) + "/dbBackup/" + DatabaseHelper.DATABASE_NAME);
	    if (db.exists()) {
	    	try
	    	{
	    		File parentFolder = backupDb.getParentFile();
	    		parentFolder.mkdirs();
	    		
		        FileUtils.copyFile(new FileInputStream(db), new FileOutputStream(backupDb));
		        return true;
	    	} catch(Exception ex) {
	    		ex.printStackTrace();
	    	}
	    }
	    return false;
	}
}
