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
import android.os.Environment;
import android.util.Log;

import com.nostra13.universalimageloader.utils.StorageUtils;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import de.luhmer.owncloudnewsreader.services.DownloadWebPageService;
import de.luhmer.owncloudnewsreader.services.PodcastDownloadService;

public class NewsFileUtils {
    private static final String TAG = NewsFileUtils.class.getCanonicalName();

    /**
     * Creates the specified <code>toFile</code> as a byte for byte copy of the
     * <code>fromFile</code>. If <code>toFile</code> already exists, then it
     * will be replaced with a copy of <code>fromFile</code>. The name and path
     * of <code>toFile</code> will be that of <code>toFile</code>.<br/>
     * <br/>
     * <i> Note: <code>fromFile</code> and <code>toFile</code> will be closed by
     * this function.</i>
     *
     * @param fromFile
     *            - FileInputStream for the file to copy from.
     * @param toFile
     *            - FileInputStream for the file to copy to.
     */
    public static void copyFile(FileInputStream fromFile, FileOutputStream toFile) throws IOException {
        FileChannel fromChannel = null;
        FileChannel toChannel = null;
        try {
            fromChannel = fromFile.getChannel();
            toChannel = toFile.getChannel();
            fromChannel.transferTo(0, fromChannel.size(), toChannel);
        } finally {
            try {
                if (fromChannel != null) {
                    fromChannel.close();
                }
            } finally {
                if (toChannel != null) {
                    toChannel.close();
                }
            }
        }
    }


    public static boolean deletePodcastFile(Context context, String url) {
        try {
            File file = new File(PodcastDownloadService.getUrlToPodcastFile(context, url, false));
            if(file.exists())
                return file.delete();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public static boolean clearPodcastCache(Context context) {
        try {
            File dir = new File(getPathPodcasts(context));
            FileUtils.deleteDirectory(dir);
        } catch (IOException ex) {
            Log.e(TAG, "Error while deleting podcasts", ex);
        }
        return false;
    }

    public static void clearWebArchiveCache(Context context) {
        getWebPageArchiveStorage(context).mkdirs();

        String path = getWebPageArchiveStorage(context).getAbsolutePath();
        Log.d("Files", "Path: " + path);
        File directory = new File(path);
        File[] files = directory.listFiles();
        Log.d("Files", "Size: " + files.length);
        for (File file : files) {
            String name = file.getName();
            //og.d("Files", "FileName: " + file.getName());
            if (name.startsWith(DownloadWebPageService.WebArchiveFinalPrefix)) {
                Log.v(TAG, "Deleting file: " + name);
                //file.delete();
            }
        }
    }

    public static String getCacheDirPath(Context context) {
        return StorageUtils.getCacheDirectory(context).getPath();
    }


    public static String getPathPodcasts(Context context) {
        return context.getExternalFilesDir(Environment.DIRECTORY_MUSIC).getAbsolutePath()+ "/podcasts";
    }

    public static File getWebPageArchiveStorage(Context context) {
        return new File(NewsFileUtils.getCacheDirPath(context), "web-archive/");
    }

    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }
}
