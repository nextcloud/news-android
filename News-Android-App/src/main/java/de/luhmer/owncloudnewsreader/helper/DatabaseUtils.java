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

package de.luhmer.owncloudnewsreader.helper;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import de.luhmer.owncloudnewsreader.database.DatabaseHelper;

public class DatabaseUtils {

	public static boolean CopyDatabaseToSdCard(Context context)
	{
		//context.getPackageCodePath()//Path to apk file..!
		//String path = "/data/data/de.luhmer.owncloudnewsreader/databases/" + DatabaseHelper.DATABASE_NAME;
		String path = context.getDatabasePath(DatabaseHelper.DATABASE_NAME).getPath();

	    File db = new File(path);
	    File backupDb = GetPath(context);
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

    public static File GetPath(Context context) {
        return new File(FileUtils.getPath(context) + "/dbBackup/" + DatabaseHelper.DATABASE_NAME);
    }
}
