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

package de.luhmer.owncloudnewsreader.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
	public static final String DATABASE_NAME = "OwncloudNewsReader.db";

	private static DatabaseHelper instance;
	//private Context context;
	private boolean shouldResetDatabase = false;

    /**
	 * @return the shouldResetDatabase
	 */
	public boolean isShouldResetDatabase() {
		return shouldResetDatabase;
	}

	public static synchronized DatabaseHelper getHelper(Context context)
    {
        if (instance == null)
            instance = new DatabaseHelper(context);

        return instance;
    }

	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, 6);
        //this.context = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		/* OLD */ //db.execSQL("CREATE TABLE folder (label TEXT NOT NULL,label_path TEXT);");
		//db.execSQL("CREATE TABLE subscription(header_text TEXT NOT NULL, subscription_id_subscription INTEGER, FOREIGN KEY (subscription_id_subscription) REFERENCES subscription(rowid));");
		/* OLD */ //db.execSQL("CREATE TABLE subscription(header_text TEXT NOT NULL, stream_id TEXT NOT NULL , folder_idfolder INTEGER, FOREIGN KEY (folder_idfolder) REFERENCES folder(rowid));");
		/* OLD */ //db.execSQL("CREATE TABLE rss_item (title TEXT NOT NULL, link TEXT, description TEXT, read BOOL, starred BOOL, rssitem_id TEXT NOT NULL, timestamp DATETIME NULL, subscription_id_subscription INTEGER,FOREIGN KEY (subscription_id_subscription) REFERENCES subscription(rowid));");

		db.execSQL("CREATE TABLE folder (label TEXT NOT NULL, label_id TEXT);");
		db.execSQL("CREATE TABLE subscription(header_text TEXT NOT NULL, "
											+ "subscription_id TEXT NOT NULL, "
											+ "favicon_url TEXT, "
											+ "link TEXT, "
											+ "avg_colour TEXT, "
											+ "folder_idfolder INTEGER, FOREIGN KEY (folder_idfolder) REFERENCES folder(rowid)"
											+ ");");
		db.execSQL("CREATE TABLE rss_item (title TEXT NOT NULL, "
											+ "link TEXT, "
											+ "body TEXT, "
											+ "read BOOL, "
											+ "starred BOOL, "
											+ "rssitem_id INT NOT NULL, "
											+ "pubdate DATETIME NULL, "
											+ "author TEXT, "
											+ "guid TEXT, "
											+ "guidHash TEXT, "
											+ "read_temp BOOL, "
  											+ "starred_temp BOOL, "
                                            + "enclosureLink TEXT, "
                                            + "enclosureMime TEXT, "
  											+ "lastModified DATETIME NULL, "
											+ "subscription_id_subscription INTEGER, FOREIGN KEY (subscription_id_subscription) REFERENCES subscription(rowid));");

		createRssCurrentViewTable(db);

		/*

		ContentValues cv = new ContentValues();
		cv.put(TITLE, "Gravity, Death Star I");
		cv.put(VALUE, SensorManager.GRAVITY_DEATH_STAR_I);
		db.insert("constants", TITLE, cv);
		*/
	}

	public void createRssCurrentViewTable(SQLiteDatabase db) {
		db.beginTransaction();
		try {
			db.execSQL("DROP TABLE IF EXISTS " + DatabaseConnection.RSS_CURRENT_VIEW_TABLE);
			db.execSQL("CREATE TABLE " + DatabaseConnection.RSS_CURRENT_VIEW_TABLE
						+ " (" + DatabaseConnection.RSS_CURRENT_VIEW_RSS_ITEM_ID + " INT NOT NULL,"
						+ " FOREIGN KEY (" + DatabaseConnection.RSS_CURRENT_VIEW_RSS_ITEM_ID + ") REFERENCES rss_item(rssitem_id))");
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
		//db.endTransaction();
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		android.util.Log.w("Constants", "Upgrading database, which will destroy all old data");

        //Toast.makeText(, "Updating Database. All Items are deleted. Please trigger a sync.", Toast.LENGTH_LONG).show();

		//shouldResetDatabase = true;
		resetDatabase(db);
	}

	public void resetDatabase(SQLiteDatabase db)
	{
		db.execSQL("DROP TABLE rss_item;");
		db.execSQL("DROP TABLE subscription;");
		db.execSQL("DROP TABLE folder;");

		onCreate(db);
	}
}
