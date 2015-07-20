package de.luhmer.owncloudnewsreader.helper;

import android.os.AsyncTask;
import android.os.Build;
import android.widget.TextView;

import de.luhmer.owncloudnewsreader.async_tasks.FillTextForTextViewAsyncTask;
import de.luhmer.owncloudnewsreader.async_tasks.IGetTextForTextViewAsyncTask;

/**
 * Created by david on 23.04.14.
 */
public class FillTextForTextViewHelper {

    public static void FillTextForTextView(TextView textView, IGetTextForTextViewAsyncTask iGetter, boolean shouldAnimate) {
        textView.setText("");

        // Execute in parallel
        new FillTextForTextViewAsyncTask(textView, iGetter, shouldAnimate).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, ((Void) null));
    }
}
