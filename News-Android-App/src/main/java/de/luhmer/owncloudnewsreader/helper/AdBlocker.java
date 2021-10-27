package de.luhmer.owncloudnewsreader.helper;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import androidx.annotation.WorkerThread;
import android.text.TextUtils;
import android.webkit.WebResourceResponse;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;

/**
 * AdBlocker inspired by: http://www.hidroh.com/2016/05/19/hacking-up-ad-blocker-android/
 */

public class AdBlocker {
    private static final String AD_HOSTS_FILE = "pgl.yoyo.org.txt";
    private static final Set<String> AD_HOSTS = new HashSet<>();

    @SuppressLint("StaticFieldLeak")
    public static void init(final Context context) {
        final AsyncTask<Void, Void, Void> loadAdRulesAsyncTask = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    File file = getFile(context);

                    if (file.exists()) {
                        Date lastModified = new Date(file.lastModified());
                        long timeDifferenceMilliseconds = new Date().getTime() - lastModified.getTime();
                        long diffWeeks = timeDifferenceMilliseconds / (60 * 60 * 1000 * 24 * 7);

                        if(diffWeeks > 4) {
                            loadFromInternet(context);
                        }
                    } else {
                        loadFromInternet(context);
                    }

                    loadFromAssets(context);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        };

        AsyncTaskHelper.StartAsyncTask(loadAdRulesAsyncTask);
    }

    private static File getFile(Context context) {
        return new File(context.getFilesDir(), AD_HOSTS_FILE);
    }

    @WorkerThread
    private static void loadFromInternet(Context context) throws IOException {
        OkHttpClient client = new OkHttpClient.Builder()
                .readTimeout(20, TimeUnit.SECONDS)
                .build();
        Request request = new Request.Builder().url("https://pgl.yoyo.org/as/serverlist.php?hostformat=nohtml&showintro=0").build();
        Response response = client.newCall(request).execute();

        File downloadedFile = getFile(context);
        BufferedSink sink = Okio.buffer(Okio.sink(downloadedFile));
        sink.writeAll(response.body().source());
        sink.close();
    }

    @WorkerThread
    private static void loadFromAssets(Context context) throws IOException {
        InputStream stream = new FileInputStream(getFile(context));
        BufferedSource buffer = Okio.buffer(Okio.source(stream));
        String line;
        while ((line = buffer.readUtf8Line()) != null) {
            AD_HOSTS.add(line);
        }
        buffer.close();
        stream.close();
    }

    public static boolean isAd(String url) {
        HttpUrl httpUrl = HttpUrl.parse(url);
        return isAdHost(httpUrl != null ? httpUrl.host() : "");
    }

    private static boolean isAdHost(String host) {
        if (TextUtils.isEmpty(host)) {
            return false;
        }
        int index = host.indexOf(".");
        return index >= 0 && (AD_HOSTS.contains(host) ||
                index + 1 < host.length() && isAdHost(host.substring(index + 1)));
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static WebResourceResponse createEmptyResource() {
        return new WebResourceResponse("text/plain", "utf-8", new ByteArrayInputStream("".getBytes()));
    }
}
