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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import de.luhmer.owncloudnewsreader.services.PodcastDownloadService;

public class FileUtils {
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





    public static String getPath(Context context) {
        String url;
        Boolean isSDPresent = android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
        if(isSDPresent)
        {
            url = Environment.getExternalStorageDirectory().getAbsolutePath();
            if (android.os.Build.DEVICE.contains("Samsung") || android.os.Build.MANUFACTURER.contains("Samsung")) {
                url = url + "/external_sd";
            }
            //url = url + "/" + context.getString(R.string.app_name);
            url = url + "/ownCloud News Reader";
        }
        else
            url = context.getCacheDir().getAbsolutePath();

        return url;
    }



    public static boolean DeletePodcastFile(Context context, String url) {
        try {
            File file = new File(PodcastDownloadService.getUrlToPodcastFile(context, url, false));
            if(file.exists())
                return file.delete();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public static String getPathPodcasts(Context context)
    {
        return getPath(context) + "/podcasts";
    }

    public static String getPathFavIcons(Context context)
    {
        return getPath(context) + "/favIcons";
    }

    public static String getPathImageCache(Context context)
    {
        return getPath(context) + "/imgCache";
    }

}
