package de.luhmer.owncloudnewsreader.helper;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.util.SparseArray;
import android.widget.ImageView;
import de.luhmer.owncloudnewsreader.R;
import de.luhmer.owncloudnewsreader.helper.ImageHandler.GetImageAsyncTask;

public class FavIconHandler {
	public static Drawable GetFavIconFromCache(String URL_TO_PAGE, Context context)
	{
		try
		{	
			File favIconFile = ImageHandler.getFullPathOfCacheFile(URL_TO_PAGE, ImageHandler.getPathFavIcons(context));						
			if(favIconFile.isFile())
			{
				/*
				InputStream fs = new FileInputStream(favIconFile);
				BufferedInputStream is = new BufferedInputStream(fs, 32*1024);				
				Bitmap bitmap = GetScaledImage(is, 50, 50);
				if(bitmap != null)
					return new BitmapDrawable(context.getResources(), bitmap);
				else
					return null;
				*/
				return Drawable.createFromPath(favIconFile.getPath());
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		return null;
	}
	
	public static int getResourceIdForRightDefaultFeedIcon(Context context)
	{
		if(ThemeChooser.isDarkTheme(context))
			return R.drawable.default_feed_icon_light;
		else
			return R.drawable.default_feed_icon_dark;
		
	}
	
	/*
	private static Bitmap GetScaledImage(BufferedInputStream is, int minimumDesiredBitmapWidth, int minimumDesiredBitmapHeight)
	{
		try {
	        final Options decodeBitmapOptions = new Options();
	        // For further memory savings, you may want to consider using this option
	        // decodeBitmapOptions.inPreferredConfig = Config.RGB_565; // Uses 2-bytes instead of default 4 per pixel

	        if(minimumDesiredBitmapWidth > 0 && minimumDesiredBitmapHeight > 0 ) {
	            final Options decodeBoundsOptions = new Options();
	            decodeBoundsOptions.inJustDecodeBounds = true;
	            is.mark(32*1024); // 32k is probably overkill, but 8k is insufficient for some jpgs
	            BitmapFactory.decodeStream(is,null,decodeBoundsOptions);
	            is.reset();

	            final int originalWidth = decodeBoundsOptions.outWidth;
	            final int originalHeight = decodeBoundsOptions.outHeight;

	            // inSampleSize prefers multiples of 2, but we prefer to prioritize memory savings
	            decodeBitmapOptions.inSampleSize= Math.max(1,Math.min(originalWidth / minimumDesiredBitmapWidth, originalHeight / minimumDesiredBitmapHeight));
	        }
	        
	        return BitmapFactory.decodeStream(is,null,decodeBitmapOptions);

	    } catch( IOException e ) {
	        throw new RuntimeException(e); // this shouldn't happen
	    } finally {
	        try {
	            is.close();
	        } catch( IOException ignored ) {}
	    }
	}*/
	
	
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
		
		
		imageView.setImageDrawable(null);
		GetImageAsyncTask giAsync = new GetImageAsyncTask(WEB_URL_TO_FILE, imgDownloadFinished, key, ImageHandler.getPathFavIcons(context)/*, imageView*/);
		giAsync.scaleImage = true;
		giAsync.dstHeight = 2*32;
		giAsync.dstWidth = 2*32;
		giAsync.execute((Void)null);
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
