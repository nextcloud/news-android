package de.luhmer.owncloudnewsreader.services;

import android.app.DownloadManager;
import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import androidx.core.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.util.Locale;

import de.luhmer.owncloudnewsreader.helper.NewsFileUtils;
import de.luhmer.owncloudnewsreader.model.PodcastItem;
import de.luhmer.owncloudnewsreader.notification.NextcloudNotificationManager;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * helper methods.
 */
public class PodcastDownloadService extends IntentService {

    private static final String TAG = PodcastDownloadService.class.getCanonicalName();

    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_DOWNLOAD = "de.luhmer.owncloudnewsreader.services.action.DOWNLOAD";

    private static final String EXTRA_RECEIVER = "de.luhmer.owncloudnewsreader.services.extra.RECEIVER";
    private static final String EXTRA_URL = "de.luhmer.owncloudnewsreader.services.extra.URL";

    private final EventBus eventBus;

    /**
     * Starts this service to download a podcast. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startPodcastDownload(Context context, PodcastItem podcastItem/*, ResultReceiver receiver*/) {
        Intent intent = new Intent(context, PodcastDownloadService.class);
        intent.setAction(ACTION_DOWNLOAD);
        intent.putExtra(EXTRA_URL, podcastItem);
        //intent.putExtra(EXTRA_RECEIVER, receiver);
        context.startService(intent);
    }


    public PodcastDownloadService() {
        super("PodcastDownloadService");

        eventBus = EventBus.getDefault();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_DOWNLOAD.equals(action)) {
                //ResultReceiver receiver = intent.getParcelableExtra(EXTRA_RECEIVER);
                PodcastItem podcast = (PodcastItem) intent.getSerializableExtra(EXTRA_URL);

                downloadPodcast(podcast, this);
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionDownload(PodcastItem podcast) {
        Uri uri = Uri.parse(podcast.link);
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setDescription(podcast.mimeType);
        request.setTitle(podcast.title);

        request.allowScanningByMediaScanner();
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

        String path = "file://" + getUrlToPodcastFile(this, podcast.fingerprint, podcast.link, true);
        request.setDestinationUri(Uri.parse(path));
        //request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "bla.txt");

        // get download service and enqueue file
        DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        manager.enqueue(request);
    }


    public static String getUrlToPodcastFile(Context context, String fingerprint, String WEB_URL_TO_FILE, boolean createDir) {
        File file = new File(WEB_URL_TO_FILE);

        String path = NewsFileUtils.getPathPodcasts(context) + "/" + fingerprint + "/";
        if(createDir)
            new File(path).mkdirs();

        return path + file.getName();
    }

    private void downloadPodcast(PodcastItem podcast, Context context) {

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder mNotificationDownloadPodcast = NextcloudNotificationManager.buildDownloadPodcastNotification(context, "Download Podcast");
        int NOTIFICATION_ID = 543226;
        notificationManager.notify(NOTIFICATION_ID, mNotificationDownloadPodcast.build());



        try {
            String urlTemp = podcast.link;
            String path = getUrlToPodcastFile(this, podcast.fingerprint, urlTemp, true);
            Log.v(TAG, "Storing podcast to: " + path);

            URL url = new URL(urlTemp);
            URLConnection connection = url.openConnection();
            connection.connect();
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(120000);//2min
            // this will be useful so that you can show a typical 0-100% progress bar
            int fileLength = connection.getContentLength();
            //float fileSizeInMb = (float)fileLength / 1024f / 1024f;
            float fileSizeInMb = (float)fileLength / 1000f / 1000f; // This matches the actual file size..

            // download the file
            InputStream input = new BufferedInputStream(url.openStream());


            String pathCache = path + ".download";
            OutputStream output = new FileOutputStream(pathCache);

            long startTime = System.nanoTime();

            byte[] data = new byte[1024];
            long total = 0;
            int count;
            int lastProgress = -1;
            int byteCountSinceLastProgress = 0;
            while ((count = input.read(data)) != -1) {
                total += count;
                byteCountSinceLastProgress += count;

                podcast.downloadProgress = (int) (total * 100 / fileLength);

                //Only update the ui/notification if the progress changed (e.g. from 1% to 2%)
                if(lastProgress != podcast.downloadProgress) {
                    lastProgress = podcast.downloadProgress;
                    eventBus.post(new DownloadProgressUpdate(podcast));

                    float speedInKBps = calculateNetworkSpeed(byteCountSinceLastProgress, startTime);
                    startTime = System.nanoTime();
                    byteCountSinceLastProgress = 0;

                    mNotificationDownloadPodcast.setProgress(100, podcast.downloadProgress, false);
                    mNotificationDownloadPodcast.setContentText(podcast.downloadProgress + "% - " + formatFloat(speedInKBps) + "KB/s - " + formatFloat(fileSizeInMb) + "MB");
                    notificationManager.notify(NOTIFICATION_ID, mNotificationDownloadPodcast.build());
                }

                output.write(data, 0, count);
            }

            output.flush();
            output.close();
            input.close();


            new File(pathCache).renameTo(new File(path));
        } catch (IOException e) {
            e.printStackTrace();

            Toast.makeText(context, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }

        podcast.downloadProgress = 100;
        eventBus.post(new DownloadProgressUpdate(podcast));

        notificationManager.cancel(NOTIFICATION_ID);

        /*
        Bundle resultData = new Bundle();
        resultData.putInt("progress" ,100);
        receiver.send(UPDATE_PROGRESS, resultData);
        */
    }

    private float calculateNetworkSpeed(int byteCountSinceLastProgress, long startTime) {
        float speedInKBps = 0.0f;
        try {
            // seconds, milliseconds, microseconds, nanoseconds
            long currentTime = System.nanoTime();
            float timeInSecs = (currentTime - startTime) / 1000f / 1000f / 1000f;
            speedInKBps = ((float)byteCountSinceLastProgress / timeInSecs) / 1024f;
        } catch (ArithmeticException ae) {
            // ignore..
        }
        return speedInKBps;
    }

    private String formatFloat(float val) {
        return String.format(Locale.getDefault(), "%.1f", val);
    }

    //public static final int UPDATE_PROGRESS = 5555;


    public static class DownloadProgressUpdate {
        public DownloadProgressUpdate(PodcastItem podcast) {
            this.podcast = podcast;
        }
        public PodcastItem podcast;
    }

    public static boolean PodcastAlreadyCached(Context context, String podcastFingerprint, String podcastUrl) {
        File file = new File(PodcastDownloadService.getUrlToPodcastFile(context, podcastFingerprint, podcastUrl, false));
        return file.exists();
    }
}
