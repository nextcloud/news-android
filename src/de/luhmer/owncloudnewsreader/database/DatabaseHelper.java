package de.luhmer.owncloudnewsreader.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
	private static final String DATABASE_NAME="OwncloudNewsReader.db";	
	
	public DatabaseHelper(Context context) {		
		super(context, DATABASE_NAME, null, 1);
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
											+ "folder_idfolder INTEGER, FOREIGN KEY (folder_idfolder) REFERENCES folder(rowid)"
											+ ");");
		db.execSQL("CREATE TABLE rss_item (title TEXT NOT NULL, "
											+ "link TEXT, "
											+ "body TEXT, "
											+ "read BOOL, "
											+ "starred BOOL, "
											+ "rssitem_id TEXT NOT NULL, "
											+ "pubdate DATETIME NULL, "
											+ "author TEXT, "
											+ "guid TEXT, "
											+ "guidHash TEXT, "
											+ "subscription_id_subscription INTEGER, FOREIGN KEY (subscription_id_subscription) REFERENCES subscription(rowid));");		


		/*
		
		ContentValues cv = new ContentValues();
		cv.put(TITLE, "Gravity, Death Star I");
		cv.put(VALUE, SensorManager.GRAVITY_DEATH_STAR_I);
		db.insert("constants", TITLE, cv);
		*/
	}
	
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		android.util.Log.w("Constants", "Upgrading database, which will destroy all old data");
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
