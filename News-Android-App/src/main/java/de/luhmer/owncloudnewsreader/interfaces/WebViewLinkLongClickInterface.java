package de.luhmer.owncloudnewsreader.interfaces;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.webkit.JavascriptInterface;

/**
 * Created by David on 25.03.2014.
 */
public class WebViewLinkLongClickInterface {
    Context mContext;

    /** Instantiate the interface and set the context */
    public WebViewLinkLongClickInterface(Context c) {
        mContext = c;
    }

    /** Show a toast from the web page */
    @JavascriptInterface
    public void openLinkInBrowser(String url) {
        if (!url.startsWith("http://") && !url.startsWith("https://"))
            url = "http://" + url;

        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        mContext.startActivity(browserIntent);
    }
}
