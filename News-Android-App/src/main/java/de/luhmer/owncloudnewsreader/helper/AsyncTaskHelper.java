package de.luhmer.owncloudnewsreader.helper;

import android.os.AsyncTask;
import android.os.Build;

/**
 * Created by David on 20.07.2015.
 */
public class AsyncTaskHelper {
    @SafeVarargs
    public static <Params,Progress,Result> void StartAsyncTask(AsyncTask<Params,Progress,Result> asyncTask, Params... params) {
        asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
    }

}
