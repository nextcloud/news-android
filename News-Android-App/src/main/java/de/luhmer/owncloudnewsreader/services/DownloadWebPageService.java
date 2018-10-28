package de.luhmer.owncloudnewsreader.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.webkit.ConsoleMessage;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.File;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import de.luhmer.owncloudnewsreader.R;
import de.luhmer.owncloudnewsreader.database.DatabaseConnectionOrm;
import de.luhmer.owncloudnewsreader.database.model.RssItem;
import de.luhmer.owncloudnewsreader.helper.NotificationActionReceiver;
import de.luhmer.owncloudnewsreader.notification.NextcloudNotificationManager;
import de.luhmer.owncloudnewsreader.services.events.StopWebArchiveDownloadEvent;

import static de.luhmer.owncloudnewsreader.Constants.NOTIFICATION_ACTION_STOP_STRING;

/**
 * An {@link Service} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * helper methods.
 */
public class DownloadWebPageService extends Service {

    private static final String TAG = DownloadWebPageService.class.getCanonicalName();
    private static final int JOB_ID = 1002;

    private static final String CHANNEL_ID = "Download Web Page Service";
    private static final String WebArchiveFinalPrefix = "web_archive_";
    private static final int NUMBER_OF_CORES = 4;
    private NotificationCompat.Builder mNotificationWebPages;
    private static final int NOTIFICATION_ID = JOB_ID;
    private NotificationManager mNotificationManager;


    // Sets the amount of time an idle thread waits before terminating
    private static final int KEEP_ALIVE_TIME = 1;
    // Sets the Time Unit to seconds
    private static final TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;

    private final AtomicInteger doneCount = new AtomicInteger();
    private Integer totalCount = 0;

    private ThreadPoolExecutor mDownloadThreadPool;



    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate() called");
        super.onCreate();

        initNotification();

        downloadWebPages();

        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy() called");
        mNotificationManager.cancel(NOTIFICATION_ID);
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void initNotification() {
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationWebPages = NextcloudNotificationManager.buildNotificationDownloadWebPageService(this, CHANNEL_ID);

        Intent stopIntent = new Intent(this, NotificationActionReceiver.class);
        stopIntent.setAction(NOTIFICATION_ACTION_STOP_STRING);
        PendingIntent stopPendingIntent = PendingIntent.getBroadcast(this, 0, stopIntent, PendingIntent.FLAG_ONE_SHOT);
        mNotificationWebPages.addAction(R.drawable.ic_action_pause, "Stop", stopPendingIntent);
    }

    @Subscribe
    public void onEvent(StopWebArchiveDownloadEvent event) {
        mDownloadThreadPool.shutdownNow();
        stopSelf();
    }

    private void runOnMainThreadAndWait(final Runnable runnable) throws InterruptedException {
        synchronized(runnable) {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    runnable.run();
                    synchronized (runnable) {
                        runnable.notifyAll();
                    }
                }
            });
            runnable.wait(); // unlocks runnable while waiting
        }
    }

    private void delayedRunOnMainThread(Runnable runnable, int waitMillis) {
        try {
            Thread.sleep(waitMillis);
            runOnMainThreadAndWait(runnable);
        } catch (InterruptedException e) {
            Log.e(TAG, "Error occurred..", e);
        }
    }



    private void downloadWebPages() {
        mNotificationWebPages.setProgress(0, 100, true);
        mNotificationManager.notify(NOTIFICATION_ID, mNotificationWebPages.build());

        final DatabaseConnectionOrm dbConn = new DatabaseConnectionOrm(DownloadWebPageService.this);
        final BlockingQueue<Runnable> downloadWorkQueue = new LinkedBlockingQueue<>();

        getWebPageArchiveStorage(this).mkdirs();

        for (RssItem rssItem : dbConn.getAllUnreadRssItemsForDownloadWebPageService()) {
            downloadWorkQueue.add(new DownloadWebPage(rssItem.getLink()));
        }
        //downloadWorkQueue.clear();

        /*
        List<RssItem> items = dbConn.getAllUnreadRssItemsForDownloadWebPageService();
        for (int i = 0; i < 5; i++) {
            downloadWorkQueue.add(new DownloadWebPage(items.get(i).getLink()));
        }
        */

        startDownloadingQueue(downloadWorkQueue);
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
            if (name.startsWith(WebArchiveFinalPrefix)) {
                Log.v(TAG, "Deleting file: " + name);
                //file.delete();
            }
        }
    }

    private void startDownloadingQueue(BlockingQueue<Runnable> downloadWorkQueue) {
        totalCount = downloadWorkQueue.size();

        // Creates a thread pool manager
        mDownloadThreadPool = new ThreadPoolExecutor(
                NUMBER_OF_CORES,       // Initial pool size
                NUMBER_OF_CORES,       // Max pool size
                KEEP_ALIVE_TIME,
                KEEP_ALIVE_TIME_UNIT,
                downloadWorkQueue);

        // Start all tasks in queue
        mDownloadThreadPool.prestartAllCoreThreads();

        // Tell ThreadPoolExecutor to stop once done
        mDownloadThreadPool.shutdown();

        // If no articles are present, remove notification right away. Otherwise the user has to close it manually
        if(totalCount == 0) {
            mNotificationManager.cancel(NOTIFICATION_ID);
        }
    }

    class DownloadWebPage implements Runnable {

        private String url;
        private WebView webView;
        private final Object lock;

        DownloadWebPage(String url) {
            this.url = url;
            lock = new Object();
        }

        @Override
        public void run() {
            //Log.v(TAG, "Running DownloadWebPage for url: " + url);
            synchronized (lock) {
                File webArchiveFile = getWebPageArchiveFileForUrl(DownloadWebPageService.this, url);
                if (!webArchiveFile.exists()) {
                    //Log.v(TAG, "Loading page:");
                    initWebView();
                    loadUrlInWebViewAndWait();
                } /* else {
                    Log.v(TAG, "Already cached article: " + url);
                } */
            }
            updateNotificationProgress();
        }

        private void initWebView() {
            try {
                runOnMainThreadAndWait(new Runnable() {
                    @Override
                    public void run() {
                        webView = new WebView(DownloadWebPageService.this);
                        webView.setWebViewClient(new DownloadImageWebViewClient(lock));
                        webView.setWebChromeClient(new DownloadImageWebViewChromeClient());
                    }
                });
            } catch (InterruptedException e) {
                Log.e(TAG, "Error while setting up WebView", e);
            }
        }

        private void loadUrlInWebViewAndWait() {
            try {
                runOnMainThreadAndWait(new Runnable() {
                    @Override
                    public void run() {
                        webView.loadUrl(url);
                    }
                });
                lock.wait();
            } catch (InterruptedException e) {
                Log.e(TAG, "Error while opening url", e);
            }
        }
    }

    class DownloadImageWebViewChromeClient extends WebChromeClient {
        @Override
        public boolean onConsoleMessage(ConsoleMessage cm) {
            //Log.d("TAG", cm.message() + " at " + cm.sourceId() + ":" + cm.lineNumber());
            return true;
        }
    }

    class DownloadImageWebViewClient extends WebViewClient {
        private final String TAG = DownloadImageWebViewClient.class.getName();
        private final Object lock;
        private boolean failed = false;

        DownloadImageWebViewClient(Object lock) {
            this.lock = lock;
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            //Log.e(TAG, "onReceivedError() called with: view = [" + view + "], request = [" + request + "], error = [" + error + "]");
            failed = true;
            super.onReceivedError(view, request, error);
        }

        public void onPageFinished(final WebView view, final String url) {
            //Log.e(TAG, "onPageFinished() called with: view = [" + view + "], url = [" + url + "]");

            if(failed) {
                Log.e(TAG, "Skipping onPageFinished as request failed.. " + url);
            } else {
                saveWebArchive(view, url);
            }

            // Notify waiting thread that we're done..
            synchronized (lock) {
                lock.notifyAll();
            }
        }

        private void saveWebArchive(final WebView view, final String url) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    delayedRunOnMainThread(new Runnable() {
                        @Override
                        public void run() {
                            // Can't store directly on external dir.. (workaround -> store on internal storage first and move then))
                            final File webArchive = getWebPageArchiveFileForUrl(DownloadWebPageService.this, url);
                            final File webArchiveExternalStorage = getWebPageArchiveFileForUrl(DownloadWebPageService.this, url);
                            view.saveWebArchive(webArchive.getAbsolutePath(), false, new ValueCallback<String>() {
                                @Override
                                public void onReceiveValue(String value) {
                                    // Move file to external storage once done writing
                                    webArchive.renameTo(webArchiveExternalStorage);
                                    //boolean success = webArchive.renameTo(webArchiveExternalStorage);
                                    //Log.v(TAG, "Move succeeded: " + success);
                                }
                            });
                        }
                    }, 2000);
                }
            }).start();
        }
    }

    private synchronized void updateNotificationProgress() {
        int current = doneCount.incrementAndGet();
        Log.d(TAG, String.format("updateNotificationProgress (%d/%d)", current, totalCount));

        if(current == totalCount) {
            //mNotificationManager.cancel(NOTIFICATION_ID);
            EventBus.getDefault().post(new StopWebArchiveDownloadEvent());
        } else {
            mNotificationWebPages
                    .setContentText((current) + "/" + totalCount + " - Downloading Images for offline usage")
                    .setProgress(totalCount, current, false);

            mNotificationManager.notify(NOTIFICATION_ID, mNotificationWebPages.build());
        }
    }

    public static File getWebPageArchiveStorage(Context context) {
        //return context.getFilesDir();
        //return new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "nextcloud-news/web-archive/");

        return new File(context.getExternalCacheDir(), "web-archive/");
        //return new File(Environment.getExternalStorageDirectory(), "nextcloud-news/web-archive/");
    }

    public static File getWebPageArchiveFileForUrl(Context context, String url) {
        return new File(getWebPageArchiveStorage(context), getWebPageArchiveFilename(url));
    }

    public static String getWebPageArchiveFilename(String url) {
        return WebArchiveFinalPrefix + url.hashCode() + ".mht";
    }
}
