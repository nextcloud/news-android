package de.luhmer.owncloudnewsreader.helper;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.ref.WeakReference;
import java.math.BigInteger;
import java.net.URL;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Random;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.SparseArray;
import android.widget.ImageView;
import de.luhmer.owncloudnewsreader.R;
import de.luhmer.owncloudnewsreader.helper.ImageHandler.GetImageFromWebAsyncTask;

public class FavIconHandler {
	public static Drawable GetFavIconFromCache(String URL_TO_PAGE, Context context)
	{
		try
		{	
			File favIconFile = ImageHandler.getFullPathOfCacheFile(URL_TO_PAGE, context);						
			if(favIconFile.isFile())			
				return Drawable.createFromPath(favIconFile.getPath());
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		return null;
	}
	
	static SparseArray<FavIconCache> imageViewReferences = new SparseArray<FavIconCache>();
	
	public static void GetImageAsync(ImageView imageView, String WEB_URL_TO_FILE, Context context)
	{	
		WeakReference<ImageView> imageViewReference = new WeakReference<ImageView>(imageView);
		FavIconCache favIconCache = new FavIconCache();
		favIconCache.context = context;
		favIconCache.WEB_URL_TO_FILE = WEB_URL_TO_FILE;
		favIconCache.imageViewReference = imageViewReference;
		
		int key = 0;
		if(imageViewReferences.size() > 0)
			key = imageViewReferences.keyAt(imageViewReferences.size() -1) + 1;
		imageViewReferences.append(key, favIconCache);
		
		GetImageFromWebAsyncTask getImageAsync = new GetImageFromWebAsyncTask(WEB_URL_TO_FILE, context, imgDownloadFinished, key/*, imageView*/);
		getImageAsync.execute((Void)null);
	}
	
	static ImageDownloadFinished imgDownloadFinished = new ImageDownloadFinished() {

		@Override
		public void DownloadFinished(int AsynkTaskId, String fileCachePath) {
			//WeakReference<ImageView> imageViewRef = imageViewReferences.get(AsynkTaskId);			
			FavIconCache favIconCache = imageViewReferences.get(AsynkTaskId);
			WeakReference<ImageView> imageViewRef = favIconCache.imageViewReference;
			
			if(imageViewRef != null)
			{	
	            ImageView imageView = imageViewRef.get();
	            if (imageView != null) {
	                imageView.setImageDrawable(FavIconHandler.GetFavIconFromCache(favIconCache.WEB_URL_TO_FILE, favIconCache.context));
	            }
			}
			
			imageViewReferences.remove(AsynkTaskId);			
		}
	};
	
	static class FavIconCache
	{
		public WeakReference<ImageView> imageViewReference;
		public String WEB_URL_TO_FILE;
		public Context context;
	}
}
