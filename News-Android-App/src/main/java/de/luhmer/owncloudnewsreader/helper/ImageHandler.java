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

package de.luhmer.owncloudnewsreader.helper;

import android.content.Context;
import android.graphics.drawable.Drawable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.net.URL;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ImageHandler {

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


    public static List<File> getFilesFromDir(File dir) {
    	List<File> files = new ArrayList<File>();
		if(dir.isDirectory())
			for (File file : dir.listFiles())
			    if (file.isFile())
			        files.add(file);
		return files;
	}

    public static long getFolderSize(File dir) {
    	long size = 0;
		if(dir.isDirectory())
		{
			for (File file : dir.listFiles()) {
			    if (file.isFile()) {
			        size += file.length();
			    }
			    else
			        size += getFolderSize(file);
			}
		}
		return size;
	}

    public static File getFullPathOfCacheFileSafe(String WEB_URL_TO_FILE, String rootPath) {
        try {
            return getFullPathOfCacheFile(WEB_URL_TO_FILE, rootPath);
        } catch (Exception ex) {
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


	public static boolean clearCache(Context context)
	{
        String path = FileUtils.getPath(context);
		boolean result = deleteDir(new File(path));
        createNoMediaFile(context);
        return result;
	}

    public static void createNoMediaFile(Context context) {
        String path = FileUtils.getPath(context);
        createEmptyFile(path + "/.nomedia");
    }

    public static void createEmptyFile(String path) {
        try {
            File file = new File(path);
            if(!file.exists()) {
                new File(file.getParent()).mkdirs();
                FileOutputStream fOut = new FileOutputStream(file, false);
                OutputStreamWriter osw = new OutputStreamWriter(fOut);
                osw.flush();
                osw.close();
            }
        } catch(Exception ex) {
            ex.printStackTrace();
        }
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
