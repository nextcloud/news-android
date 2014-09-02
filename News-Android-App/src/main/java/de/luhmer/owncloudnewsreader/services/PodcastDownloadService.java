package de.luhmer.owncloudnewsreader.services;

import android.annotation.TargetApi;
import android.app.DownloadManager;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.ResultReceiver;
import android.widget.Toast;

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

import de.greenrobot.event.EventBus;
import de.luhmer.owncloudnewsreader.helper.FileUtils;
import de.luhmer.owncloudnewsreader.helper.JavaYoutubeDownloader;
import de.luhmer.owncloudnewsreader.model.PodcastItem;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class PodcastDownloadService extends IntentService {
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_DOWNLOAD = "de.luhmer.owncloudnewsreader.services.action.DOWNLOAD";



    // TODO: Rename parameters
    private static final String EXTRA_RECEIVER = "de.luhmer.owncloudnewsreader.services.extra.RECEIVER";
    private static final String EXTRA_URL = "de.luhmer.owncloudnewsreader.services.extra.URL";
    private static final String EXTRA_PARAM2 = "de.luhmer.owncloudnewsreader.services.extra.PARAM2";
    private static final String TAG = "PodcastDownloadService";

    private EventBus eventBus;

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startPodcastDownload(Context context, PodcastItem podcastItem/*, ResultReceiver receiver*/) {
        Intent intent = new Intent(context, PodcastDownloadService.class);
        intent.setAction(ACTION_DOWNLOAD);
        intent.putExtra(EXTRA_URL, podcastItem);
        //intent.putExtra(EXTRA_RECEIVER, receiver);
        //intent.putExtra(EXTRA_PARAM2, param2);
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
                ResultReceiver receiver = (ResultReceiver) intent.getParcelableExtra(EXTRA_RECEIVER);
                PodcastItem podcast = (PodcastItem) intent.getSerializableExtra(EXTRA_URL);
                //final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                //handleActionDownload(podcast);

                downloadPodcast(podcast, this);


            }/* else if (ACTION_BAZ.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleActionBaz(param1, param2);
            }*/
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    private void handleActionDownload(PodcastItem podcast) {
        Uri uri = Uri.parse(podcast.link);
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setDescription(podcast.mimeType);
        request.setTitle(podcast.title);

        // in order for this if to run, you must use the android 3.2 to compile your app
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            request.allowScanningByMediaScanner();
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        }

        String path = "file://" + getUrlToPodcastFile(this, podcast.link, true);
        request.setDestinationUri(Uri.parse(path));
        //request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "bla.txt");

        // get download service and enqueue file
        DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        manager.enqueue(request);
    }


    public static String getUrlToPodcastFile(Context context, String WEB_URL_TO_FILE, boolean createDir) {
        if(WEB_URL_TO_FILE.contains(JavaYoutubeDownloader.host))
            return getUrlToYoutubePodcastFile(context, WEB_URL_TO_FILE, createDir);

        File file = new File(WEB_URL_TO_FILE);

        String path = FileUtils.getPathPodcasts(context) + "/" + getHashOfString(WEB_URL_TO_FILE) + "/";
        if(createDir)
            new File(path).mkdirs();

        return path + file.getName();
    }

    private static String getUrlToYoutubePodcastFile(Context context, String WEB_URL_TO_FILE, boolean createDir) {
        String path = FileUtils.getPathPodcasts(context) + "/" + getHashOfString(WEB_URL_TO_FILE) + "/";
        if(createDir)
            new File(path).mkdirs();

        return path + "video.mp4";
    }


    public static String getHashOfString(String WEB_URL_TO_FILE)
    {
        try {
            MessageDigest m = MessageDigest.getInstance("MD5");
            m.reset();
            m.update(WEB_URL_TO_FILE.trim().getBytes());
            byte[] digest = m.digest();
            BigInteger bigInt = new BigInteger(1,digest);
            String hashtext = bigInt.toString(16);

            return hashtext;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return WEB_URL_TO_FILE;
    }


    private void downloadPodcast(PodcastItem podcast, Context context) {
        try {
            String urlTemp = podcast.link;
            String path = getUrlToPodcastFile(this, urlTemp, true);

            if(podcast.link.contains(JavaYoutubeDownloader.host)) {

                path = getUrlToPodcastFile(context, urlTemp, true);

                try {
                    urlTemp = new JavaYoutubeDownloader().getDownloadUrl(podcast.link, context);
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }

            }
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
            while ((count = input.read(data)) != -1) {
                total += count;

                int progress = (int) (total * 100 / fileLength);
                podcast.downloadProgress = progress;
                eventBus.post(new DownloadProgressUpdate(podcast));

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

        /*
        Bundle resultData = new Bundle();
        resultData.putInt("progress" ,100);
        receiver.send(UPDATE_PROGRESS, resultData);
        */
    }

    public static final int UPDATE_PROGRESS = 5555;


    public class DownloadProgressUpdate {

        public DownloadProgressUpdate(PodcastItem podcast) {
            this.podcast = podcast;
        }

        public PodcastItem podcast;
    }
}
