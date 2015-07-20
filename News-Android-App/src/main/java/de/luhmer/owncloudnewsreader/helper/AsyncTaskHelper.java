package de.luhmer.owncloudnewsreader.helper;

import android.os.AsyncTask;
import android.os.Build;

/**
 * Created by David on 20.07.2015.
 */
public class AsyncTaskHelper {

    public static <T> void StartAsyncTask(AsyncTask asyncTask, T... params) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
        else
            asyncTask.execute(params);
    }

    @SafeVarargs
    public static <T> void StartAsyncTask(AsyncTask asyncTask, Void... params) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
        else
            asyncTask.execute(params);
    }

}
