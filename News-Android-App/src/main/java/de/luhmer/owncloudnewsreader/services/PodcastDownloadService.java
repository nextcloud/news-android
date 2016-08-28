package de.luhmer.owncloudnewsreader.services;

import android.app.DownloadManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
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

import de.luhmer.owncloudnewsreader.NewsReaderListActivity;
import de.luhmer.owncloudnewsreader.R;
import de.luhmer.owncloudnewsreader.helper.FileUtils;
import de.luhmer.owncloudnewsreader.model.PodcastItem;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * helper methods.
 */
public class PodcastDownloadService extends IntentService {

    @SuppressWarnings("unused")
    private static final String TAG = PodcastDownloadService.class.getCanonicalName();

    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_DOWNLOAD = "de.luhmer.owncloudnewsreader.services.action.DOWNLOAD";

    private static final String EXTRA_RECEIVER = "de.luhmer.owncloudnewsreader.services.extra.RECEIVER";
    private static final String EXTRA_URL = "de.luhmer.owncloudnewsreader.services.extra.URL";

    private EventBus eventBus;

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

        String path = "file://" + getUrlToPodcastFile(this, podcast.link, true);
        request.setDestinationUri(Uri.parse(path));
        //request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "bla.txt");

        // get download service and enqueue file
        DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        manager.enqueue(request);
    }


    public static String getUrlToPodcastFile(Context context, String WEB_URL_TO_FILE, boolean createDir) {
        File file = new File(WEB_URL_TO_FILE);

        String path = FileUtils.getPathPodcasts(context) + "/" + getHashOfString(WEB_URL_TO_FILE) + "/";
        if(createDir)
            new File(path).mkdirs();

        return path + file.getName();
    }

    public static String getHashOfString(String WEB_URL_TO_FILE)
    {
        try {
            MessageDigest m = MessageDigest.getInstance("MD5");
            m.reset();
            m.update(WEB_URL_TO_FILE.trim().getBytes());
            byte[] digest = m.digest();
            BigInteger bigInt = new BigInteger(1,digest);

            return bigInt.toString(16);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return WEB_URL_TO_FILE;
    }


    private void downloadPodcast(PodcastItem podcast, Context context) {
        Intent intentNewsReader = new Intent(this, NewsReaderListActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intentNewsReader, 0);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder mNotificationDownloadPodcast = new NotificationCompat.Builder(this)
                .setContentTitle(getResources().getString(R.string.app_name))
                .setContentText("Downloading podcast")
                .setSmallIcon(R.drawable.ic_notification)
                .setContentIntent(pIntent)
                .setOngoing(true);
        Notification notify = mNotificationDownloadPodcast.build();
        //Hide the notification after its selected
        notify.flags |= Notification.FLAG_AUTO_CANCEL;
        notify.flags |= Notification.FLAG_NO_CLEAR;

        int NOTIFICATION_ID = 543226;
        notificationManager.notify(NOTIFICATION_ID, notify);



        try {
            String urlTemp = podcast.link;
            String path = getUrlToPodcastFile(this, urlTemp, true);

            URL url = new URL(urlTemp);
            URLConnection connection = url.openConnection();
            connection.connect();
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(120000);//2min
            // this will be useful so that you can show a typical 0-100% progress bar
            int fileLength = connection.getContentLength();

            // download the file
            InputStream input = new BufferedInputStream(url.openStream());


            String pathCache = path + ".download";
            OutputStream output = new FileOutputStream(pathCache);


            byte data[] = new byte[1024];
            long total = 0;
            int count;
            int lastProgress = -1;
            while ((count = input.read(data)) != -1) {
                total += count;

                podcast.downloadProgress = (int) (total * 100 / fileLength);

                //Only update the ui/notification if the progress changed (e.g. from 1% to 2%)
                if(lastProgress != podcast.downloadProgress) {
                    lastProgress = podcast.downloadProgress;
                    eventBus.post(new DownloadProgressUpdate(podcast));

                    mNotificationDownloadPodcast.setProgress(100, podcast.downloadProgress, false);
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

    //public static final int UPDATE_PROGRESS = 5555;


    public class DownloadProgressUpdate {
        public DownloadProgressUpdate(PodcastItem podcast) {
            this.podcast = podcast;
        }
        public PodcastItem podcast;
    }

    public static boolean PodcastAlreadyCached(Context context, String podcastUrl) {
        File file = new File(PodcastDownloadService.getUrlToPodcastFile(context, podcastUrl, false));
        return file.exists();
    }
}
