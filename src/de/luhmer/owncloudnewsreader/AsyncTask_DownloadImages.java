package de.luhmer.owncloudnewsreader;

import java.util.List;

import android.content.Context;
import android.os.AsyncTask;
import de.luhmer.owncloudnewsreader.helper.ImageHandler;
import de.luhmer.owncloudnewsreader.helper.ImageHandler.GetImageAsyncTask;

public class AsyncTask_DownloadImages extends AsyncTask<Void, Void, Void>{
	String text;
	Context context;
	
	public AsyncTask_DownloadImages(String text, Context context) {
		this.text = text;
		this.context = context;
	}
	
	@Override
	protected Void doInBackground(Void... params) {
		if(text != null)
		{
			List<String> links = ImageHandler.getImageLinksFromText(text);
		    
		    for(String link : links)	
		    	new GetImageAsyncTask(link, null, 999, ImageHandler.getPathImageCache(context)).execute();
		}
		return null;
	}
}
