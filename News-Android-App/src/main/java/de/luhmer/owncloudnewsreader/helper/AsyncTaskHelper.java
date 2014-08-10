package de.luhmer.owncloudnewsreader.helper;

import android.os.AsyncTask;
import android.os.Build;

/**
 * Created by David on 09.08.2014.
 */
public class AsyncTaskHelper {

    public static <T> void StartAsyncTask(AsyncTask asyncTask, T... params) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
        else
            asyncTask.execute(params);
    }

}
