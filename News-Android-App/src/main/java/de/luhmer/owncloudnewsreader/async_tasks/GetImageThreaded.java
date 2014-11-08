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

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.util.ByteArrayBuffer;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import de.luhmer.owncloudnewsreader.helper.BitmapDrawableLruCache;
import de.luhmer.owncloudnewsreader.helper.ImageDownloadFinished;
import de.luhmer.owncloudnewsreader.helper.ImageHandler;

public class GetImageThreaded extends Thread
{
	private static final String TAG = "GetImageAsyncTask";

	//private static int count = 0;

	private URL WEB_URL_TO_FILE;
	private ImageDownloadFinished imageDownloadFinished;
	private long ThreadId;
	private String rootPath;
	private Context cont;

	public boolean scaleImage = false;
	public int dstHeight; // height in pixels
	public int dstWidth; // width in pixels

    Bitmap bmp;

	public GetImageThreaded(String WEB_URL_TO_FILE, ImageDownloadFinished imgDownloadFinished, long ThreadId, String rootPath, Context cont) {
		try
		{
			this.WEB_URL_TO_FILE = new URL(WEB_URL_TO_FILE);
		}
		catch(Exception ex)
		{
            Log.d(TAG, ex.getLocalizedMessage() + " - URL: " + WEB_URL_TO_FILE);
			//ex.printStackTrace();
		}

		this.cont = cont;
		imageDownloadFinished = imgDownloadFinished;
		this.ThreadId = ThreadId;
		this.rootPath = rootPath;
		//this.imageViewReference = new WeakReference<ImageView>(imageView);
	}


    @Override
    public void run() {
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
                int current;
                while ((current = bis.read()) != -1) {
                    baf.append((byte) current);
                }

                //If the file is not empty
                if(baf.length() > 0) {
                    bmp = BitmapFactory.decodeByteArray(baf.toByteArray(), 0, baf.length());

                    FileOutputStream fos = new FileOutputStream(cacheFile);
                    fos.write(baf.toByteArray());
                    fos.close();
                }
            }
            //return cacheFile.getPath();
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
        //return bmp;

        if(imageDownloadFinished != null)
            imageDownloadFinished.DownloadFinished(ThreadId, bmp);

        super.run();
    }


}
