package de.luhmer.owncloudnewsreader.services;

import java.util.ArrayList;
import java.util.List;

import de.luhmer.owncloudnewsreader.NewsReaderListActivity;
import de.luhmer.owncloudnewsreader.R;
import de.luhmer.owncloudnewsreader.database.DatabaseConnection;
import de.luhmer.owncloudnewsreader.helper.ImageDownloadFinished;
import de.luhmer.owncloudnewsreader.helper.ImageHandler;
import de.luhmer.owncloudnewsreader.helper.ImageHandler.GetImageAsyncTask;
import android.annotation.TargetApi;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class DownloadImagesService extends IntentService {

	private static final int NOTIFICATION_ID = 1;
	private NotificationManager notificationManager;
	private NotificationCompat.Builder NotificationDownloadImages;
	private int count;
	private int maxCount;
	public static final String LAST_ITEM_ID = "LAST_ITEM_ID";
	
	public DownloadImagesService() {
		super(null);
		count = 0;
		maxCount = 0;
	}
	
	public DownloadImagesService(String name) {
		super(name);
		count = 0;
		maxCount = 0;
	}

	@Override
	public void onDestroy() {
		if(NotificationDownloadImages != null)
		{
			if(maxCount == 0)
				notificationManager.cancel(NOTIFICATION_ID);
			/*
			else if(maxCount != count)
			{
				NotificationDownloadImages.setProgress(maxCount, count, false);
				NotificationDownloadImages.setContentText("Stopped downloading images. Application was closed.");
	        	notificationManager.notify(NOTIFICATION_ID, NotificationDownloadImages.build());
			}*/
		}
		super.onDestroy();
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	protected void onHandleIntent(Intent intent) {
		String lastId = intent.getStringExtra(LAST_ITEM_ID);
		DatabaseConnection dbConn = new DatabaseConnection(this);
		Cursor cursor = dbConn.getAllItemsWithIdHigher(lastId);
		List<String> links = new ArrayList<String>();
		try
		{
			if(cursor != null)
			{
				while(cursor.moveToNext())
				{
					String body = cursor.getString(cursor.getColumnIndex(DatabaseConnection.RSS_ITEM_BODY));
					links.addAll(ImageHandler.getImageLinksFromText(body));
				}
			}
		} catch(Exception ex) {
			ex.printStackTrace();
		} finally {
			cursor.close();
			dbConn.closeDatabase();
		}
		maxCount = links.size();
						
		Intent intentNewsReader = new Intent(this, NewsReaderListActivity.class);
		PendingIntent pIntent = PendingIntent.getActivity(this, 0, intentNewsReader, 0);
		notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
		NotificationDownloadImages = new NotificationCompat.Builder(this)
	        .setContentTitle("ownCloud News Reader")
	        .setContentText("Downloading Images for offline usage")
	        .setSmallIcon(R.drawable.ic_launcher)
	        .setContentIntent(pIntent);
   
		Notification notify = NotificationDownloadImages.build();
		
		//Hide the notification after its selected
		notify.flags |= Notification.FLAG_AUTO_CANCEL;
		
		if(maxCount > 0)		
			notificationManager.notify(NOTIFICATION_ID, notify); 
		
		for(String link : links)	
	    	new GetImageAsyncTask(link, imgDownloadFinished, 999, ImageHandler.getPathImageCache(this)).execute();
	}
	
	ImageDownloadFinished imgDownloadFinished = new ImageDownloadFinished() {
		
		@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
		@Override
		public void DownloadFinished(int AsynkTaskId, String fileCachePath) {
			count++;
            // Sets the progress indicator to a max value, the
            // current completion percentage, and "determinate"
            // state
			NotificationDownloadImages.setProgress(maxCount, count, false);
			NotificationDownloadImages.setContentText("Downloading Images for offline usage - " + count + "/" + maxCount);
            // Displays the progress bar for the first time.
            notificationManager.notify(NOTIFICATION_ID, NotificationDownloadImages.build());
            
            if(maxCount == count)
            	notificationManager.cancel(NOTIFICATION_ID);
		}
	};
}
