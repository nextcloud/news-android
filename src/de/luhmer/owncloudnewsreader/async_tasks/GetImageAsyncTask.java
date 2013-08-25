/**
* Android ownCloud News
*
* @author David Luhmer
* @copyright 2013 David Luhmer david-dev@live.de
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU AFFERO GENERAL PUBLIC LICENSE
* License as published by the Free Software Foundation; either
* version 3 of the License, or any later version.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU AFFERO GENERAL PUBLIC LICENSE for more details.
*
* You should have received a copy of the GNU Affero General Public
* License along with this library.  If not, see <http://www.gnu.org/licenses/>.
*
*/

package de.luhmer.owncloudnewsreader.async_tasks;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.http.util.ByteArrayBuffer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import de.luhmer.owncloudnewsreader.SettingsActivity;
import de.luhmer.owncloudnewsreader.helper.ImageDownloadFinished;
import de.luhmer.owncloudnewsreader.helper.ImageHandler;

public class GetImageAsyncTask extends AsyncTask<Void, Void, String>
{
	//private static final String TAG = "GetImageAsyncTask";

	private static int count = 0;
	
	private URL WEB_URL_TO_FILE;
	private ImageDownloadFinished imageDownloadFinished;
	private int AsynkTaskId;
	private String rootPath;
	private Context cont;
	
	public String feedID = null;
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

	@SuppressLint("NewApi")
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
				
				
				
				/* Open a connection to that URL. */
                URLConnection urlConn = WEB_URL_TO_FILE.openConnection();

                urlConn.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");
                
                
                /*
                 * Define InputStreams to read from the URLConnection.
                 */
                InputStream is = urlConn.getInputStream();
                BufferedInputStream bis = new BufferedInputStream(is);

                /*
                 * Read bytes to the Buffer until there is nothing more to read(-1).
                 */
                ByteArrayBuffer baf = new ByteArrayBuffer(50);
                int current = 0;
                while ((current = bis.read()) != -1) {
                        baf.append((byte) current);
                }

                /* Convert the Bytes read to a String. */
                FileOutputStream fos = new FileOutputStream(cacheFile);
                fos.write(baf.toByteArray());
                fos.close();
				
				
				/*
				FileOutputStream fOut = new FileOutputStream(cacheFile);
				Bitmap mBitmap = BitmapFactory.decodeStream(WEB_URL_TO_FILE.openStream());
				
				Log.d(TAG, "Downloading image: " + WEB_URL_TO_FILE.toString());
				
				if(mBitmap != null) {					
					if(scaleImage)
						mBitmap = Bitmap.createScaledBitmap(mBitmap, dstWidth, dstHeight, true);
					
					mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
				}
				fOut.close();
				*/

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
