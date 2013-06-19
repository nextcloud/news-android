package de.luhmer.owncloudnewsreader.helper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URL;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Environment;
import de.luhmer.owncloudnewsreader.R;

/**
 * Created by David on 24.05.13.
 */
public class ImageHandler {
    //private static final String TAG = "DownloadImagesFromWeb";

    public static Drawable LoadImageFromWebOperations(String url) {
        try {
            InputStream is = (InputStream) new URL(url).getContent();
            Drawable d = Drawable.createFromStream(is, "src name");
            return d;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    
    
    
    
    public static class GetImageAsyncTask extends AsyncTask<Void, Void, String>
	{
		private URL WEB_URL_TO_FILE;
		private ImageDownloadFinished imageDownloadFinished;
		private int AsynkTaskId;
		private String rootPath;
		
		public boolean scaleImage = false;
		public int dstHeight; // height in pixels
		public int dstWidth; // width in pixels		
		
		//private ImageView imgView;
		//private WeakReference<ImageView> imageViewReference;
		
		public GetImageAsyncTask(String WEB_URL_TO_FILE, ImageDownloadFinished imgDownloadFinished, int AsynkTaskId, String rootPath/*, ImageView imageView*/) {
			try
			{
				this.WEB_URL_TO_FILE = new URL(WEB_URL_TO_FILE);
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
			}
			imageDownloadFinished = imgDownloadFinished;
			this.AsynkTaskId = AsynkTaskId;
			this.rootPath = rootPath;
			//this.imageViewReference = new WeakReference<ImageView>(imageView);
		}
		
		@Override
		protected void onPostExecute(String result) {
			if(imageDownloadFinished != null)
				imageDownloadFinished.DownloadFinished(AsynkTaskId, result);
			//imgView.setImageDrawable(GetFavIconFromCache(WEB_URL_TO_FILE.toString(), context));
			super.onPostExecute(result);
		}

		@Override
		protected String doInBackground(Void... params) {
			try
			{
				File cacheFile = ImageHandler.getFullPathOfCacheFile(WEB_URL_TO_FILE.toString(), rootPath);						
				if(!cacheFile.isFile())
				{
					File dir = new File(rootPath);
					dir.mkdirs();
					cacheFile = getFullPathOfCacheFile(WEB_URL_TO_FILE.toString(), rootPath);				
					//cacheFile.createNewFile();
					FileOutputStream fOut = new FileOutputStream(cacheFile);
					Bitmap mBitmap = BitmapFactory.decodeStream(WEB_URL_TO_FILE.openStream());
					
					if(scaleImage)
						mBitmap = Bitmap.createScaledBitmap(mBitmap, dstWidth, dstHeight, true);
					
					mBitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
					fOut.close();
				}
				return cacheFile.getPath();
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
			}
		    return null;
		}
	}
    
    
    public static File getFullPathOfCacheFile(String WEB_URL_TO_FILE, String rootPath) throws Exception
	{
		URL url = new URL(WEB_URL_TO_FILE.trim());
		
		MessageDigest m = MessageDigest.getInstance("MD5");
		m.reset();
		m.update(url.toString().getBytes());
		byte[] digest = m.digest();
		BigInteger bigInt = new BigInteger(1,digest);
		String hashtext = bigInt.toString(16);
		
		String fileEnding = "";
		try
		{
			fileEnding = url.getFile().substring(url.getFile().lastIndexOf("."));
			fileEnding = fileEnding.replaceAll("\\?(.*)", "");
		}
		catch(Exception ex)
		{
			fileEnding = ".png";
			//ex.printStackTrace();
		}
		
		return new File(rootPath + "/" + hashtext  + fileEnding);
	}
	
	
	
	public static String getPathFavIcons(Context context)
	{
		return getPath(context) + "/favIcons";
	}
	
	public static String getPathImageCache(Context context)
	{
		return getPath(context) + "/imgCache";
	}
	
	public static String getPath(Context context) {
		String url = null;		
		Boolean isSDPresent = android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
		if(isSDPresent)
		{
			url = Environment.getExternalStorageDirectory().getAbsolutePath();
			if (android.os.Build.DEVICE.contains("Samsung") || android.os.Build.MANUFACTURER.contains("Samsung")) {
				url = url + "/external_sd";
			}
			url = url + "/" + context.getString(R.string.app_name);
		}
		else
			url = context.getCacheDir().getAbsolutePath(); //Environment.getDownloadCacheDirectory().getAbsolutePath();		
		
		return url;
	}
    
	
	public static List<String> getImageLinksFromText(String text)
	{
		List<String> links = new ArrayList<String>();
		Pattern pattern = Pattern.compile("<img[^>]*>");
		Pattern patternSrcLink = Pattern.compile("src=\"(.*?)\"");
		Matcher matcher = pattern.matcher(text);
	    // Check all occurance
	    while (matcher.find()) {
	    	//System.out.print("Start index: " + matcher.start());
	    	//System.out.print(" End index: " + matcher.end() + " ");
	    	//System.out.println(matcher.group());
	    	
	    	Matcher matcherSrcLink = patternSrcLink.matcher(matcher.group());
	    	if(matcherSrcLink.find()) {
	    		links.add(matcherSrcLink.group(1));	
	    	}
	    }
	    return links;
	}

	
	public static void clearCache(Context context)
	{
		deleteDir(new File(getPath(context)));
	}
	
	// Deletes all files and subdirectories under dir.
	// Returns true if all deletions were successful.
	// If a deletion fails, the method stops attempting to delete and returns
	// false.
	private static boolean deleteDir(File dir) {
		if (dir.isDirectory()) {
			String[] children = dir.list();
			for (int i = 0; i < children.length; i++) {
				boolean success = deleteDir(new File(dir, children[i]));
				if (!success) {
					return false;
				}
			}
		}

		// The directory is now empty so delete it
		return dir.delete();
	}
	

    /*
    public static Bitmap loadBitmap(String url) {
        Bitmap bitmap = null;
        InputStream in = null;
        BufferedOutputStream out = null;

        try {
            in = new BufferedInputStream(new URL(url).openStream(), IO_BUFFER_SIZE);

            final ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
            out = new BufferedOutputStream(dataStream, IO_BUFFER_SIZE);
            copy(in, out);
            out.flush();

            final byte[] data = dataStream.toByteArray();
            BitmapFactory.Options options = new BitmapFactory.Options();
            //options.inSampleSize = 1;

            bitmap = BitmapFactory.decodeByteArray(data, 0, data.length,options);
        } catch (IOException e) {
            Log.d(TAG, "Could not load Bitmap from: " + url);
        } finally {
            closeStream(in);
            closeStream(out);
        }

        return bitmap;
    }*/
}
