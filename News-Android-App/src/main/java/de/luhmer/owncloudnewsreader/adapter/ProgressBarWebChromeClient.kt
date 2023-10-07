package de.luhmer.owncloudnewsreader.adapter

import android.util.Log
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.ProgressBar

private const val COMPLETE = 100

/**
 * A very simple WebChromeClient which sets the status of a given
 * ProgressBar instance while loading. The ProgressBar instance will
 * only be visible during loading.
 */
class ProgressBarWebChromeClient(private val progressBar: ProgressBar) : WebChromeClient() {
    val tag = javaClass.canonicalName

    override fun onConsoleMessage(cm: ConsoleMessage): Boolean {
        Log.v(tag, cm.message() + " at " + cm.sourceId() + ":" + cm.lineNumber())
        return true
    }

    override fun onProgressChanged(
        view: WebView,
        progress: Int,
    ) {
        progressBar.progress = progress
        if (progress < COMPLETE && progressBar.visibility == ProgressBar.GONE) {
            progressBar.visibility = ProgressBar.VISIBLE
        } else if (progress == COMPLETE) {
            progressBar.visibility = ProgressBar.GONE
        }
    }
}
