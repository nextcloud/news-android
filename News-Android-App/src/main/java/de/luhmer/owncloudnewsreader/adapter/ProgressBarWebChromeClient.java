package de.luhmer.owncloudnewsreader.adapter;

import android.util.Log;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.ProgressBar;

/**
 * A very simple WebChromeClient which sets the status of a given
 * ProgressBar instance while loading. The ProgressBar instance will
 * only be visible during loading.
 */
public class ProgressBarWebChromeClient extends WebChromeClient {

    public final String TAG = getClass().getCanonicalName();

    private ProgressBar mProgressBar;

    public ProgressBarWebChromeClient(ProgressBar progressBar) {
        mProgressBar = progressBar;
    }

    @Override
    public boolean onConsoleMessage(ConsoleMessage cm) {
        Log.v(TAG, cm.message() + " at " + cm.sourceId() + ":" + cm.lineNumber());
        return true;
    }

    @Override
    public void onProgressChanged(WebView view, int progress) {
        mProgressBar.setProgress(progress);

        if (progress < 100 && mProgressBar.getVisibility() == ProgressBar.GONE) {
            mProgressBar.setVisibility(ProgressBar.VISIBLE);
        } else if (progress == 100) {
            mProgressBar.setVisibility(ProgressBar.GONE);
        }
    }
}
