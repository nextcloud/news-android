package de.luhmer.owncloudnewsreader.helper;

import android.os.AsyncTask;

public class AsyncTaskHelper {
    @SafeVarargs
    public static <Params,Progress,Result> void StartAsyncTask(AsyncTask<Params,Progress,Result> asyncTask, Params... params) {
        asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
    }

}
