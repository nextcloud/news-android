package de.luhmer.owncloudnewsreader.reader;

import android.app.Activity;

public interface AsyncTask_Reader {
	public void attach(final Activity context, final OnAsyncTaskCompletedListener[] listener);
}
