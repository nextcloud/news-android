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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            // Execute in parallel
            new FillTextForTextViewAsyncTask(textView, iGetter, shouldAnimate).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, ((Void) null));
        else
            new FillTextForTextViewAsyncTask(textView, iGetter, shouldAnimate).execute((Void) null);
    }
}
