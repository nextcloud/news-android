package de.luhmer.owncloudnewsreader.helper;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.ref.WeakReference;
import java.math.BigInteger;
import java.net.URL;
import java.security.MessageDigest;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Environment;
import android.widget.ImageView;
import de.luhmer.owncloudnewsreader.R;

public class FavIconHandler {
	public static Drawable GetFavIconFromCache(String URL_TO_PAGE, Context context)
	{
		try
		{	
			File favIconFile = getFullPathOfCacheFile(URL_TO_PAGE, context);						
			if(favIconFile.isFile())			
				return Drawable.createFromPath(favIconFile.getPath());
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		return null;
	}
	
	public static class GetImageFromWebAsyncTask extends AsyncTask<Void, Void, Void>
	{
		private URL WEB_URL_TO_FILE;
		private Context context;
		//private ImageView imgView;
		private WeakReference<ImageView> imageViewReference;
		
		public GetImageFromWebAsyncTask(String WEB_URL_TO_FILE, Context context, ImageView imageView) {
			try
			{
				this.WEB_URL_TO_FILE = new URL(WEB_URL_TO_FILE);
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
			}
			this.context = context;
			this.imageViewReference = new WeakReference<ImageView>(imageView);
		}
		
		@Override
		protected void onPostExecute(Void result) {
	        if (imageViewReference != null) {
	            ImageView imageView = imageViewReference.get();
	            if (imageView != null) {
	                imageView.setImageDrawable(GetFavIconFromCache(WEB_URL_TO_FILE.toString(), context));
	            }
	        }
			//imgView.setImageDrawable(GetFavIconFromCache(WEB_URL_TO_FILE.toString(), context));
			super.onPostExecute(result);
		}

		@Override
		protected Void doInBackground(Void... params) {
			try
			{	
				File dir = new File(getPathFavIcons(context));
				dir.mkdirs();
				File cacheFile = getFullPathOfCacheFile(WEB_URL_TO_FILE.toString(), context);				
				//cacheFile.createNewFile();
				FileOutputStream fOut = new FileOutputStream(cacheFile);
				Bitmap mBitmap = BitmapFactory.decodeStream(WEB_URL_TO_FILE.openStream());
				mBitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
				fOut.close();
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
			}
		    return null;
		}
	}
	
	
	public static File getFullPathOfCacheFile(String WEB_URL_TO_FILE, Context context) throws Exception
	{
		URL url = new URL(WEB_URL_TO_FILE);
		
		MessageDigest m = MessageDigest.getInstance("MD5");
		m.reset();
		m.update(url.toString().getBytes());
		byte[] digest = m.digest();
		BigInteger bigInt = new BigInteger(1,digest);
		String hashtext = bigInt.toString(16);
		
		String fileEnding = url.getFile().substring(url.getFile().lastIndexOf("."));
		fileEnding = fileEnding.replaceAll("\\?(.*)", "");
		
		return new File(getPathFavIcons(context) + "/" + hashtext  + fileEnding);
	}
	
	
	
	public static String getPathFavIcons(Context context)
	{
		return getPath(context) + "/favIcons";
	}
	
	public static String getPath(Context context) {
		String url = Environment.getExternalStorageDirectory().getAbsolutePath();
		if (android.os.Build.DEVICE.contains("Samsung") || android.os.Build.MANUFACTURER.contains("Samsung")) {
			url = url + "/external_sd";
		}
		url = url + "/" + context.getString(R.string.app_name);
		return url;
	}
}
