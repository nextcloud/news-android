package de.luhmer.owncloudnewsreader.async_tasks;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import de.luhmer.owncloudnewsreader.SettingsActivity;
import de.luhmer.owncloudnewsreader.helper.ImageDownloadFinished;
import de.luhmer.owncloudnewsreader.helper.ImageHandler;

public class GetImageAsyncTask extends AsyncTask<Void, Void, String>
{
	private static int count = 0;
	
	private URL WEB_URL_TO_FILE;
	private ImageDownloadFinished imageDownloadFinished;
	private int AsynkTaskId;
	private String rootPath;
	private Context cont;
	
	public boolean scaleImage = false;
	public int dstHeight; // height in pixels
	public int dstWidth; // width in pixels		
	
	//private ImageView imgView;
	//private WeakReference<ImageView> imageViewReference;
	
	public GetImageAsyncTask(String WEB_URL_TO_FILE, ImageDownloadFinished imgDownloadFinished, int AsynkTaskId, String rootPath, Context cont/*, ImageView imageView*/) {
		try
		{
			this.WEB_URL_TO_FILE = new URL(WEB_URL_TO_FILE);
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		this.cont = cont;
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
				cacheFile = ImageHandler.getFullPathOfCacheFile(WEB_URL_TO_FILE.toString(), rootPath);				
				//cacheFile.createNewFile();
				FileOutputStream fOut = new FileOutputStream(cacheFile);
				Bitmap mBitmap = BitmapFactory.decodeStream(WEB_URL_TO_FILE.openStream());
				
				if(scaleImage)
					mBitmap = Bitmap.createScaledBitmap(mBitmap, dstWidth, dstHeight, true);
				
				mBitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
				fOut.close();
				

				count++;
				if(count >= 25)//Check every 25 images the cache size
				{
					count = 0;
					HashMap<File, Long> files;
					long size = ImageHandler.getFolderSize(new File(ImageHandler.getPath(cont)));
					size = (long) (size / 1024d / 1024d);
					
					SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(cont);
					int max_allowed_size = Integer.parseInt(mPrefs.getString(SettingsActivity.SP_MAX_CACHE_SIZE, "1000"));//Default is 1Gb --> 1000mb
					if(size > max_allowed_size)
					{
						files = new HashMap<File, Long>();
						for(File file : ImageHandler.getFilesFromDir(new File(ImageHandler.getPathImageCache(cont))))
						{
							files.put(file, file.lastModified());
						}
						
						for(Object itemObj : sortHashMapByValuesD(files).entrySet())
						{
							File file = (File) itemObj;
							file.delete();
							size -= file.length();
							if(size < max_allowed_size)
								break;
						}
					}
				}
			}
			return cacheFile.getPath();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	    return null;
	}

	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static LinkedHashMap sortHashMapByValuesD(HashMap passedMap) {
		List mapKeys = new ArrayList(passedMap.keySet());
		List mapValues = new ArrayList(passedMap.values());
		Collections.sort(mapValues);
		Collections.sort(mapKeys);

		LinkedHashMap sortedMap = new LinkedHashMap();

		Iterator valueIt = mapValues.iterator();
		while (valueIt.hasNext()) {
	       Object val = valueIt.next();
			Iterator keyIt = mapKeys.iterator();

			while (keyIt.hasNext()) {
				Object key = keyIt.next();
				String comp1 = passedMap.get(key).toString();
				String comp2 = val.toString();

				if (comp1.equals(comp2)){
					passedMap.remove(key);
					mapKeys.remove(key);
					sortedMap.put((String)key, (Double)val);
					break;
				}
			}
		}
		return sortedMap;
	}
}
