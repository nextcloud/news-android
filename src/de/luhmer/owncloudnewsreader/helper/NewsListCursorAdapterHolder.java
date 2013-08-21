package de.luhmer.owncloudnewsreader.helper;

import de.luhmer.owncloudnewsreader.cursor.NewsListCursorAdapter;
import android.app.Application;

public class NewsListCursorAdapterHolder extends Application {
	
	private NewsListCursorAdapter lvAdapter = null;

	/**
	 * @param lvAdapter the lvAdapter to set
	 */
	public void setLvAdapter(NewsListCursorAdapter lvAdapter) {
		this.lvAdapter = lvAdapter;
	}

	/**
	 * @return the lvAdapter
	 */
	public NewsListCursorAdapter getLvAdapter() {
		return lvAdapter;
	}

}
