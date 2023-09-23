/*
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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


    public static boolean deletePodcastFile(Context context, String fingerprint, String url) {
        try {
            File file = new File(PodcastDownloadService.getUrlToPodcastFile(context, fingerprint, url, false));
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
            deleteDirectory(dir);
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
        //return context.getCacheDir().getAbsolutePath();
        return context.getExternalCacheDir().getAbsolutePath();
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


    /*
    Method below are copied from https://github.com/apache/commons-io/blob/master/src/main/java/org/apache/commons/io/FileUtils.java
     */

    /**
     * Deletes a directory recursively.
     *
     * @param directory directory to delete
     * @throws IOException              in case deletion is unsuccessful
     * @throws IllegalArgumentException if {@code directory} does not exist or is not a directory
     */
    public static void deleteDirectory(final File directory) throws IOException {
        if (!directory.exists()) {
            return;
        }

        cleanDirectory(directory);

        if (!directory.delete()) {
            final String message =
                    "Unable to delete directory " + directory + ".";
            throw new IOException(message);
        }
    }


    /**
     * Lists files in a directory, asserting that the supplied directory satisfies exists and is a directory
     * @param directory The directory to list
     * @return The files in the directory, never null.
     * @throws IOException if an I/O error occurs
     */
    private static File[] verifiedListFiles(final File directory) throws IOException {
        if (!directory.exists()) {
            final String message = directory + " does not exist";
            throw new IllegalArgumentException(message);
        }

        if (!directory.isDirectory()) {
            final String message = directory + " is not a directory";
            throw new IllegalArgumentException(message);
        }

        final File[] files = directory.listFiles();
        if (files == null) {  // null if security restricted
            throw new IOException("Failed to list contents of " + directory);
        }
        return files;
    }

    /**
     * Cleans a directory without deleting it.
     *
     * @param directory directory to clean
     * @throws IOException              in case cleaning is unsuccessful
     * @throws IllegalArgumentException if {@code directory} does not exist or is not a directory
     */
    public static void cleanDirectory(final File directory) throws IOException {
        final File[] files = verifiedListFiles(directory);

        IOException exception = null;
        for (final File file : files) {
            try {
                forceDelete(file);
            } catch (final IOException ioe) {
                exception = ioe;
            }
        }

        if (null != exception) {
            throw exception;
        }
    }

    /**
     * Deletes a file. If file is a directory, delete it and all sub-directories.
     * <p>
     * The difference between File.delete() and this method are:
     * <ul>
     * <li>A directory to be deleted does not have to be empty.</li>
     * <li>You get exceptions when a file or directory cannot be deleted.
     * (java.io.File methods returns a boolean)</li>
     * </ul>
     *
     * @param file file or directory to delete, must not be {@code null}
     * @throws NullPointerException  if the directory is {@code null}
     * @throws FileNotFoundException if the file was not found
     * @throws IOException           in case deletion is unsuccessful
     */
    public static void forceDelete(final File file) throws IOException {
        if (file.isDirectory()) {
            deleteDirectory(file);
        } else {
            final boolean filePresent = file.exists();
            if (!file.delete()) {
                if (!filePresent) {
                    throw new FileNotFoundException("File does not exist: " + file);
                }
                final String message =
                        "Unable to delete file: " + file;
                throw new IOException(message);
            }
        }
    }

    public static String[] getDownloadedPodcastsFingerprints(Context context) {
        File folder = new File(NewsFileUtils.getPathPodcasts(context));
        File[] files = folder.listFiles();
        if (files == null) {
            return new String[0];
        }
        List<String> ids = Arrays.stream(files).map(File::getName).collect(Collectors.toList());
        return ids.toArray(new String[0]);
    }

}
