package de.luhmer.owncloudnewsreader.reader;

import android.content.Context;
import android.util.SparseArray;
import de.luhmer.owncloudnewsreader.database.DatabaseConnection;
import de.luhmer.owncloudnewsreader.helper.ImageDownloadFinished;
import de.luhmer.owncloudnewsreader.helper.ImageHandler.GetImageFromWebAsyncTask;

public class DownloadItemsToCache {
	SparseArray<String> URLs;
	Context context;
	
	public DownloadItemsToCache(Context context) {
		URLs = new SparseArray<String>();
		this.context = context;
	}
	
	public void StartDownloadOfImage(String URL_TO_IMAGE)
	{
		int key = 0;
		if(URLs.size() > 0)
			key = URLs.keyAt(URLs.size() -1) + 1;
		URLs.append(key, URL_TO_IMAGE);
		
		 GetImageFromWebAsyncTask getImageAsync = new GetImageFromWebAsyncTask(URL_TO_IMAGE, context, imgDownloadFinished, key);
		 getImageAsync.execute((Void)null);
	}	
	
	ImageDownloadFinished imgDownloadFinished = new ImageDownloadFinished() {

		@Override
		public void DownloadFinished(int AsynkTaskId, String fileCachePath) {
			
			DatabaseConnection dbConn = new DatabaseConnection(context);
			try
			{
				
				
			} finally {
				dbConn.closeDatabase();
			}			
		}		
	};
}
