package de.luhmer.owncloudnewsreader.helper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.ImageView;

import java.io.*;
import java.lang.ref.WeakReference;
import java.math.BigInteger;
import java.net.URL;
import java.security.MessageDigest;

import de.luhmer.owncloudnewsreader.R;

/**
 * Created by David on 24.05.13.
 */
public class ImageHandler {
    private static final String TAG = "DownloadImagesFromWeb";

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
    
    
    
    
    
    public static class GetImageFromWebAsyncTask extends AsyncTask<Void, Void, String>
	{
		private URL WEB_URL_TO_FILE;
		private Context context;
		private ImageDownloadFinished imageDownloadFinished;
		private int AsynkTaskId;
		//private ImageView imgView;
		//private WeakReference<ImageView> imageViewReference;
		
		public GetImageFromWebAsyncTask(String WEB_URL_TO_FILE, Context context, ImageDownloadFinished imgDownloadFinished, int AsynkTaskId/*, ImageView imageView*/) {
			try
			{
				this.WEB_URL_TO_FILE = new URL(WEB_URL_TO_FILE);
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
			}
			this.context = context;
			imageDownloadFinished = imgDownloadFinished;
			this.AsynkTaskId = AsynkTaskId;
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
				File dir = new File(getPathFavIcons(context));
				dir.mkdirs();
				File cacheFile = getFullPathOfCacheFile(WEB_URL_TO_FILE.toString(), context);				
				//cacheFile.createNewFile();
				FileOutputStream fOut = new FileOutputStream(cacheFile);
				Bitmap mBitmap = BitmapFactory.decodeStream(WEB_URL_TO_FILE.openStream());
				mBitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
				fOut.close();
				
				return cacheFile.getPath();
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
